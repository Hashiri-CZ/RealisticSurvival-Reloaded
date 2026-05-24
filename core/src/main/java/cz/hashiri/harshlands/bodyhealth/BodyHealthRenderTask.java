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
                if (states.equals(last)) {
                    continue;
                }

                BossbarHUD hud = hudResolver.apply(player);
                // Emit one BossbarHUD element per body part. Each element is a single
                // glyph wrapped in an empty-styled parent (so BossbarHUD's root-font
                // replacement step does not strip the bodyhealth font from the child).
                // BossbarHUD.rebuildTitle sorts elements by X and emits the negative-
                // space shifts between them — we do NOT pre-bake shifts here. This
                // avoids the multi-glyph nested-Component path that turned out to
                // render only one part on the client.
                for (BodyPart part : BodyPart.values()) {
                    BodyPartState st = states.getOrDefault(part, BodyPartState.FULL);
                    Component glyph = BodyHealthRenderState.glyphFor(part, st);
                    // BetterHud-mirror: every part anchors at the same X.
                    // Transparent padding inside each PNG positions the visible pixels.
                    hud.setElement(BodyHealthRenderState.elementId(part), anchorX, glyph,
                                   BodyHealthRenderState.GLYPH_ADVANCE_PX);
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
