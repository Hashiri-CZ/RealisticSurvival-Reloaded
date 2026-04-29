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

    static final String ELEMENT_ID = "bodyhealth";

    private final BodyHealthModule module;
    private final int anchorX;
    private final Function<Player, BossbarHUD> hudResolver;

    BodyHealthRenderTask(BodyHealthModule module, int anchorX, Function<Player, BossbarHUD> hudResolver) {
        this.module = module;
        this.anchorX = anchorX;
        this.hudResolver = hudResolver;
    }

    @Override
    public void run() {
        // Snapshot to allow concurrent add()/remove() during iteration.
        Set<UUID> shownNow = new HashSet<>(module.shownPlayers());

        for (UUID uuid : shownNow) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                // markHidden also schedules lastRenderedStates.remove() on the main thread
                // — no separate clearLastRendered() call needed here.
                module.markHidden(uuid);
                continue;
            }

            Map<BodyPart, BodyPartState> states = readPlaceholders(player);
            Map<BodyPart, BodyPartState> last = module.lastRendered(uuid);
            if (states.equals(last)) {
                continue;
            }

            Component composite = BodyHealthRenderState.compose(states);
            BossbarHUD hud = hudResolver.apply(player);
            hud.setElement(ELEMENT_ID, anchorX, composite, BodyHealthRenderState.totalAdvance());
            module.putLastRendered(uuid, states);
        }
    }

    private Map<BodyPart, BodyPartState> readPlaceholders(Player p) {
        Map<BodyPart, BodyPartState> out = new EnumMap<>(BodyPart.class);
        for (BodyPart part : BodyPart.values()) {
            String key = "%bodyhealth_state_" + part.placeholderSuffix() + "%";
            String raw = PlaceholderAPI.setPlaceholders(p, key);
            // PAPI returns the placeholder unchanged if no expansion handles it.
            if (raw == null || raw.startsWith("%")) {
                out.put(part, BodyPartState.FULL);
            } else {
                out.put(part, BodyPartState.fromPlaceholder(raw));
            }
        }
        return out;
    }
}
