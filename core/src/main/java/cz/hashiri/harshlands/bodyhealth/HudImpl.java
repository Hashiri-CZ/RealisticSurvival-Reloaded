package cz.hashiri.harshlands.bodyhealth;

import cz.hashiri.harshlands.api.hud.Hud;
import cz.hashiri.harshlands.api.player.HudPlayer;

import java.util.UUID;

final class HudImpl implements Hud {

    /** Backing state — implemented by BodyHealthModule. */
    interface State {
        boolean markShown(UUID uuid);
        boolean markHidden(UUID uuid);
        boolean isShown(UUID uuid);
    }

    private final String id;
    private final State state;

    HudImpl(String id, State state) {
        this.id = id;
        this.state = state;
    }

    @Override public String id() { return id; }

    @Override public boolean add(HudPlayer player) {
        return state.markShown(player.uuid());
    }

    @Override public boolean remove(HudPlayer player) {
        return state.markHidden(player.uuid());
    }

    @Override public boolean isShownTo(HudPlayer player) {
        return state.isShown(player.uuid());
    }
}
