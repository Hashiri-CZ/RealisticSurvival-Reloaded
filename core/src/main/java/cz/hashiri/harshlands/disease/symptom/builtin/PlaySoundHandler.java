package cz.hashiri.harshlands.disease.symptom.builtin;

import cz.hashiri.harshlands.disease.symptom.SymptomContext;
import cz.hashiri.harshlands.disease.symptom.SymptomHandler;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class PlaySoundHandler implements SymptomHandler {
    @Override
    public void apply(Player player, SymptomContext ctx) {
        if (ctx.params() == null) return;
        String soundName = ctx.params().getString("Sound", "");
        if (soundName.isEmpty()) return;
        Sound sound;
        try {
            sound = Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return; // unknown sound -> silently skip
        }
        float volume = (float) ctx.params().getDouble("Volume", 1.0);
        float pitch = (float) ctx.params().getDouble("Pitch", 1.0);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
