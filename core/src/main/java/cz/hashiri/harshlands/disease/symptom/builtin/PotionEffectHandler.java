package cz.hashiri.harshlands.disease.symptom.builtin;

import cz.hashiri.harshlands.disease.symptom.SymptomContext;
import cz.hashiri.harshlands.disease.symptom.SymptomHandler;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class PotionEffectHandler implements SymptomHandler {
    @Override
    public void apply(Player player, SymptomContext ctx) {
        if (ctx.params() == null) return;
        PotionEffectType type = resolve(ctx.params().getString("Effect", ""));
        if (type == null) return;
        int amplifier = ctx.params().getInt("Amplifier", 0);
        int duration = ctx.params().getInt("DurationTicks", 100);
        player.addPotionEffect(new PotionEffect(type, duration, amplifier, true, false, true));
    }

    @Override
    public void clear(Player player, SymptomContext ctx) {
        if (ctx.params() == null) return;
        PotionEffectType type = resolve(ctx.params().getString("Effect", ""));
        if (type != null) player.removePotionEffect(type);
    }

    private static PotionEffectType resolve(String name) {
        if (name == null || name.isEmpty()) return null;
        return PotionEffectType.getByName(name.toUpperCase());
    }
}
