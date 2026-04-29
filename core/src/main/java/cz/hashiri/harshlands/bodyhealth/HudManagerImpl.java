package cz.hashiri.harshlands.bodyhealth;

import cz.hashiri.harshlands.api.hud.Hud;
import cz.hashiri.harshlands.api.hud.HudManager;

import java.util.Collection;
import java.util.Map;

final class HudManagerImpl implements HudManager {

    private final Map<String, Hud> huds;

    HudManagerImpl(Map<String, Hud> huds) {
        // Defensive immutable copy: the documented "snapshot at call time" contract
        // must hold even if the constructor caller mutates their map afterwards.
        this.huds = Map.copyOf(huds);
    }

    @Override public Hud getHud(String id) {
        if (id == null) return null;
        return huds.get(id);
    }

    @Override public Collection<Hud> getHuds() { return huds.values(); }
}
