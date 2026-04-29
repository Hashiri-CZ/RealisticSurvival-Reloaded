package cz.hashiri.harshlands.bodyhealth;

import cz.hashiri.harshlands.api.hud.Hud;
import cz.hashiri.harshlands.api.hud.HudManager;

import java.util.Collection;
import java.util.Map;
import java.util.Collections;

final class HudManagerImpl implements HudManager {

    private final Map<String, Hud> huds;

    HudManagerImpl(Map<String, Hud> huds) {
        this.huds = Collections.unmodifiableMap(huds);
    }

    @Override public Hud getHud(String id) { return huds.get(id); }
    @Override public Collection<Hud> getHuds() { return huds.values(); }
}
