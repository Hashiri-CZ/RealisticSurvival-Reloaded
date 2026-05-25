package cz.hashiri.harshlands.bodyhealth;

import cz.hashiri.harshlands.utils.BossbarHUD;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

final class BodyHealthRenderTask extends BukkitRunnable {

    /** Legacy element-id prefix kept for backwards-compatible removeElement scrubs. */
    static final String ELEMENT_ID = BodyHealthRenderState.ELEMENT_ID_PREFIX;

    /**
     * Diagnostic: when non-null, only this body part's glyph is emitted per
     * render tick; the other seven elements are removed from the bossbar.
     * {@code null} = normal multi-part rendering.
     */
    static volatile BodyPart debugOnlyPart = null;

    private final BodyHealthModule module;
    private final int anchorX;
    private final Function<Player, BossbarHUD> hudResolver;

    BodyHealthRenderTask(BodyHealthModule module, int anchorX, Function<Player, BossbarHUD> hudResolver) {
        this.module = module;
        this.anchorX = anchorX;
        this.hudResolver = hudResolver;
    }

    /**
     * Diagnostic helper for /hl bdh onlypart — public so the command class in
     * another package can invoke it via {@link BodyHealthModule#setDebugOnlyPart}.
     * <p>
     * Emit only one body part's glyph per render, removing the other seven from
     * each shown player's bossbar. Pass {@code null} to restore normal multi-part
     * rendering.
     * <p>
     * Side-effect: clears per-player last-rendered state so the change takes
     * effect within one tick period.
     */
    static void setDebugOnlyPart(BodyPart only, BodyHealthModule module) {
        BodyPart prev = debugOnlyPart;
        debugOnlyPart = only;
        BHDLogger.logf("debugOnlyPart %s -> %s", prev, only);
        // Force the next render to re-evaluate by clearing per-player state.
        for (java.util.UUID uuid : module.shownPlayers()) {
            module.clearLastRendered(uuid);
        }
    }

    @Override
    public void run() {
        // Snapshot to allow concurrent add()/remove() during iteration.
        Set<UUID> shownNow = new HashSet<>(module.shownPlayers());

        for (UUID uuid : shownNow) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                module.markHidden(uuid);
                continue;
            }

            try {
                Map<BodyPart, BodyPartState> states = readPlaceholders(player);
                Map<BodyPart, BodyPartState> last = module.lastRendered(uuid);
                boolean shortCircuit = states.equals(last);

                BossbarHUD hud = hudResolver.apply(player);

                if (BHDLogger.isEnabled()) {
                    BHDLogger.logf("tick player=%s hudId=%d mapSize=%d short_circuit=%s states=%s last=%s",
                                   player.getName(),
                                   System.identityHashCode(hud),
                                   hud.elementCount(),
                                   shortCircuit,
                                   compactStates(states),
                                   last == null ? "null" : compactStates(last));
                }

                if (shortCircuit) {
                    continue;
                }

                // Emit one BossbarHUD element per body part. (Phase 1 keeps the existing
                // 8-element shape — Phase 2 collapses to a single element.)
                for (BodyPart part : BodyPart.values()) {
                    if (debugOnlyPart != null && part != debugOnlyPart) {
                        // Filter is active and this part is excluded — remove its element.
                        boolean removed = hud.removeElement(BodyHealthRenderState.elementId(part));
                        if (BHDLogger.isEnabled() && removed) {
                            BHDLogger.logf("debugOnlyPart filter removed id=%s",
                                           BodyHealthRenderState.elementId(part));
                        }
                        continue;
                    }
                    BodyPartState st = states.getOrDefault(part, BodyPartState.FULL);
                    Component glyph = BodyHealthRenderState.glyphFor(part, st);
                    BossbarHUD.SetElementOutcome outcome =
                        hud.setElement(BodyHealthRenderState.elementId(part), anchorX, glyph,
                                       BodyHealthRenderState.GLYPH_ADVANCE_PX);
                    if (BHDLogger.isEnabled()) {
                        BHDLogger.logf("setElement player=%s id=%s x=%d advance=%d outcome=%s mapSize=%d",
                                       player.getName(),
                                       BodyHealthRenderState.elementId(part),
                                       anchorX,
                                       BodyHealthRenderState.GLYPH_ADVANCE_PX,
                                       outcome,
                                       hud.elementCount());
                    }
                }

                module.putLastRendered(uuid, states);
            } catch (Throwable t) {
                cz.hashiri.harshlands.HLPlugin.getPlugin().getLogger()
                        .warning("BodyHealth render failed for " + player.getName() + ": " + t);
                t.printStackTrace();
            }
        }
    }

    /** Compact one-line representation of a states map for diagnostic logging. */
    private static String compactStates(Map<BodyPart, BodyPartState> states) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (BodyPart part : BodyPart.values()) {
            if (!first) sb.append(',');
            sb.append(part.name(), 0, 1)
              .append(part.name().length() > 4 ? part.name().substring(part.name().length() - 1) : "")
              .append(':')
              .append(states.getOrDefault(part, BodyPartState.FULL).name().charAt(0));
            first = false;
        }
        return sb.append('}').toString();
    }

    private Map<BodyPart, BodyPartState> readPlaceholders(Player p) {
        Map<BodyPart, BodyPartState> out = new EnumMap<>(BodyPart.class);
        for (BodyPart part : BodyPart.values()) {
            String key = "%bodyhealth_state_" + part.placeholderSuffix() + "%";
            String raw = PlaceholderAPI.setPlaceholders(p, key);
            if (raw == null || raw.startsWith("%")) {
                out.put(part, BodyPartState.FULL);
            } else {
                out.put(part, BodyPartState.fromPlaceholder(raw));
            }
        }
        return out;
    }
}
