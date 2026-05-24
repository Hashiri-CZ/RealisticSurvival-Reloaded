package cz.hashiri.harshlands.bodyhealth;

import cz.hashiri.harshlands.utils.BossbarHUD;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
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

    private final BodyHealthModule module;
    private final int anchorX;
    private final Function<Player, BossbarHUD> hudResolver;
    private final Set<UUID> firstFrameLogged = new HashSet<>();

    BodyHealthRenderTask(BodyHealthModule module, int anchorX, Function<Player, BossbarHUD> hudResolver) {
        this.module = module;
        this.anchorX = anchorX;
        this.hudResolver = hudResolver;
    }

    /** Forget that we've logged the first frame for this UUID, so the next
     *  fresh render emits the diagnostic again (used on respawn). */
    void forgetFirstFrame(java.util.UUID uuid) {
        firstFrameLogged.remove(uuid);
    }

    @Override
    public void run() {
        // Snapshot to allow concurrent add()/remove() during iteration.
        Set<UUID> shownNow = new HashSet<>(module.shownPlayers());

        for (UUID uuid : shownNow) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                module.markHidden(uuid);
                firstFrameLogged.remove(uuid);
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

                boolean firstFrame = (last == null) && firstFrameLogged.add(uuid);
                module.putLastRendered(uuid, states);
                if (firstFrame) {
                    String titleJson = GsonComponentSerializer.gson().serialize(hud.currentTitle());
                    cz.hashiri.harshlands.HLPlugin.getPlugin().getLogger()
                            .info("BodyHealth HUD first frame emitted for " + player.getName()
                                  + " (anchorX=" + anchorX
                                  + ", parts=" + BodyPart.values().length
                                  + ", hud=" + hud.getClass().getSimpleName()
                                  + ", title-len=" + titleJson.length() + ")");
                    cz.hashiri.harshlands.HLPlugin.getPlugin().getLogger()
                            .info("BodyHealth full title JSON: " + titleJson);
                }
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
