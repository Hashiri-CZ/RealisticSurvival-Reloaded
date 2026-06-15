package cz.hashiri.harshlands.disease.symptom.builtin;

import cz.hashiri.harshlands.disease.symptom.SymptomContext;
import cz.hashiri.harshlands.disease.symptom.SymptomHandler;
import org.bukkit.entity.Player;

public final class DamageOverTimeHandler implements SymptomHandler {
    @Override
    public void apply(Player player, SymptomContext ctx) {
        if (ctx.params() == null) return;
        double amount = ctx.params().getDouble("Amount", 1.0);
        if (amount <= 0) return;
        player.damage(amount);
    }
}
