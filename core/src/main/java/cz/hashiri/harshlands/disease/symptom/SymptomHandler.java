package cz.hashiri.harshlands.disease.symptom;

import org.bukkit.entity.Player;

public interface SymptomHandler {
    /** Applied once per progression check while the stage is active. */
    void apply(Player player, SymptomContext ctx);

    /** Called when the stage is left or the disease is cured. Default: nothing to undo. */
    default void clear(Player player, SymptomContext ctx) {}
}
