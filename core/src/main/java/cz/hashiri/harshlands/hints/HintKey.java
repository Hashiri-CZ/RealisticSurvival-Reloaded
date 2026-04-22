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
    FIRST_SAW_CRAFTED("FirstSawCrafted", false, 0L, Sound.UI_TOAST_CHALLENGE_COMPLETE),
    // Task 1.4 — temperature hints
    FIRST_COLD_EXPOSURE("FirstColdExposure", false, 0L, Sound.UI_TOAST_IN),
    FIRST_HEAT_EXPOSURE("FirstHeatExposure", false, 0L, Sound.UI_TOAST_IN),
    FIRST_SHIVERING("FirstShivering", true, 120_000L, Sound.ENTITY_PLAYER_HURT_FREEZE),
    // Task 1.5 — thirst & parasite hints
    FIRST_THIRST_WARNING("FirstThirstWarning", false, 0L, Sound.BLOCK_WATER_AMBIENT),
    FIRST_PARASITE("FirstParasite", true, 300_000L, Sound.ENTITY_SPIDER_AMBIENT),
    PARASITE_CURED("ParasiteCured", true, 300_000L, Sound.ENTITY_PLAYER_BURP);

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
