/*
    Copyright (C) 2026  Hashiri_
    GNU GPL v3.
 */
package cz.hashiri.harshlands.hints;

import org.bukkit.Sound;

public enum HintKey {
    FIST_ON_LOG("FistOnLog", true,  30_000L, Sound.BLOCK_NOTE_BLOCK_BASS),
    FIRST_FLINT_SHARD("FirstFlintShard", false, 0L, Sound.UI_TOAST_CHALLENGE_COMPLETE),
    FIRST_FLINT_HATCHET_CRAFTED("FirstFlintHatchetCrafted", false, 0L, Sound.UI_TOAST_CHALLENGE_COMPLETE),
    FIRST_LOG("FirstLog", false, 0L, Sound.UI_TOAST_CHALLENGE_COMPLETE),
    FIRST_PLANK_AND_STICK("FirstPlankAndStick", false, 0L, Sound.UI_TOAST_CHALLENGE_COMPLETE),
    FIRST_SAW_CRAFTED("FirstSawCrafted", false, 0L, Sound.UI_TOAST_CHALLENGE_COMPLETE);

    private final String translationKey;
    private final boolean repeating;
    private final long defaultCooldownMs;
    private final Sound defaultSound;

    HintKey(String translationKey, boolean repeating, long defaultCooldownMs, Sound defaultSound) {
        this.translationKey = translationKey;
        this.repeating = repeating;
        this.defaultCooldownMs = defaultCooldownMs;
        this.defaultSound = defaultSound;
    }

    public String translationKey()    { return translationKey; }
    public boolean isRepeating()      { return repeating; }
    public long defaultCooldownMs()   { return defaultCooldownMs; }
    public Sound defaultSound()       { return defaultSound; }
}
