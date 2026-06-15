package cz.hashiri.harshlands.disease.trigger;

import cz.hashiri.harshlands.data.HLPlayer;
import org.bukkit.entity.Player;

public final class ColdExposureTrigger implements DiseaseTrigger {
    private final String diseaseId;
    private final double temperatureBelow;
    private final boolean requireWet;
    private final double chancePerCheck;

    public ColdExposureTrigger(String diseaseId, double temperatureBelow,
                               boolean requireWet, double chancePerCheck) {
        this.diseaseId = diseaseId;
        this.temperatureBelow = temperatureBelow;
        this.requireWet = requireWet;
        this.chancePerCheck = chancePerCheck;
    }

    @Override public String diseaseId() { return diseaseId; }

    @Override
    public double chance(Player player) {
        HLPlayer hlPlayer = HLPlayer.getPlayers().get(player.getUniqueId());
        if (hlPlayer == null || hlPlayer.getTanDataModule() == null) return 0.0;
        double temperature = hlPlayer.getTanDataModule().getTemperature();
        if (temperature >= temperatureBelow) return 0.0;
        if (requireWet && !player.isInWater() && !player.getWorld().hasStorm()) return 0.0;
        return chancePerCheck;
    }
}
