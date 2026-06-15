package cz.hashiri.harshlands.disease.trigger;

import org.bukkit.entity.Player;

public interface DiseaseTrigger {
    /** Disease id this trigger can cause. */
    String diseaseId();

    /** Contraction probability [0..1] for this player this check; 0 = no risk now. */
    double chance(Player player);
}
