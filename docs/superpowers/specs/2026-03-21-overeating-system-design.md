# Overeating System Design

## Problem

The FoodExpansion nutrition system slows vanilla hunger drain (`DrainMultiplier: 0.5`), keeping the hunger bar full for extended periods. Minecraft prevents eating when hunger is at 20/20. This means players' macro nutrients (protein, carbs, fats) deplete over time but they physically cannot eat to replenish them — the core gameplay loop is broken.

## Solution

Allow players to eat food even when their hunger bar is full, with a **per-food-item satiation counter** that causes diminishing macro returns on repeated consumption of the same food. This solves the "can't eat" problem while encouraging dietary variety.

## Design

### Hunger Nudge — Enabling Vanilla Eating

Instead of bypassing the vanilla consume pipeline, we **nudge the hunger bar down by 1 point** when the player right-clicks food while full. This lets vanilla handle the entire eating flow naturally:

1. **`PlayerInteractEvent`** — Player right-clicks with edible food while hunger >= threshold (default: 20).
   - **Hand filtering**: Only process `event.getHand()` that holds the food item. Prevents double-fire (Bukkit fires once per hand per click).
   - **Satiation check**: If satiation is at hard cap (0% multiplier), send blocked chat message and return without nudging — eating is denied.
   - **Cooldown check**: Skip if within cooldown window (default 500ms) from last nudge.
   - **Nudge**: Set `player.setFoodLevel(player.getFoodLevel() - 1)`. Hunger drops to 19.
   - **Flag**: Add player UUID to a `Set<UUID> forceEatingPlayers` so the consume handler knows this was a force-eat.
2. **Vanilla takes over** — Hunger is now 19, so Minecraft allows eating. The full vanilla eating animation plays (arm movement, crunch particles, eat duration).
3. **`PlayerItemConsumeEvent` fires** — Our existing `onPlayerConsume` handler runs:
   - Check if player is in `forceEatingPlayers` set.
   - If yes: apply satiation multiplier to the nutrient gains, increment satiation counter, send chat message if applicable, remove from set.
   - If no: normal eating, no satiation penalty (unchanged behavior).
4. **Vanilla restores hunger** — The food's hunger value restores the bar. Most foods restore >= 1 point, so the bar returns to 20. Net effect: hunger bar stays full, macros gained, animation played.

### Why This Works

- Full vanilla eating animation — no server-side hacks
- All special food effects work naturally (Golden Apple absorption, Chorus Fruit teleport, Suspicious Stew potion effects, Honey Bottle poison cure)
- Container items handled by vanilla (stews return bowls, honey returns glass bottle)
- Existing `onPlayerConsume` handler is reused — satiation is just an additional multiplier layer
- The 1-point hunger drop is invisible in practice since the food immediately restores it

### FoodLevelChangeEvent Interaction

The hunger nudge (`setFoodLevel(19)`) fires a `FoodLevelChangeEvent`. The existing `onFoodLevelChange` handler would see this as a hunger decrease and apply drain multiplier logic, which would cancel the event and nullify the nudge. This must be bypassed.

**Guard**: At the top of `onFoodLevelChange`, check `if (forceEatingPlayers.contains(player.getUniqueId())) return;` — skip all drain-slowing logic for force-eat nudges.

**Event sequence for a force-eat cycle:**
1. `PlayerInteractEvent` → nudge hunger to 19, add to `forceEatingPlayers`
2. `FoodLevelChangeEvent` (decrease, 20→19) → skipped by forceEatingPlayers guard
3. Vanilla eating animation plays over ~1.6 seconds
4. `PlayerItemConsumeEvent` → our handler applies macros with satiation multiplier, removes from `forceEatingPlayers`
5. `FoodLevelChangeEvent` (increase, 19→restored) → skipped by existing `newLevel >= oldLevel` guard
6. Subsequent natural hunger drain → handled normally (player no longer in forceEatingPlayers)

Note: `setFoodLevel()` is called from within `PlayerInteractEvent`, not from within `FoodLevelChangeEvent`, so there is no re-entrant event loop risk.

### Edge Case: Player Cancels Eating

If the player starts eating (hunger was nudged to 19) but cancels before finishing (releases right-click), `PlayerItemConsumeEvent` never fires. To handle cleanup:
- Schedule a 100-tick (5 second) timeout task when nudging
- On timeout, if the player is still in `forceEatingPlayers`: remove from set, restore hunger to the stored pre-nudge level (only if current food level still equals pre-nudge - 1, to avoid overwriting unrelated changes)
- Store the pre-nudge food level alongside the UUID: `Map<UUID, Integer> forceEatingPreNudgeLevel`

### Per-Food Satiation Counter

Each player tracks a `Map<String, Integer>` of food item key (Material name) to satiation count.

- **Increment**: +1 each time that specific food is eaten via force-eat (hunger was nudged)
- **No increment for normal eating**: When hunger < threshold, no satiation penalty
- **Decay**: A single global timer registered in `FoodExpansionModule.initialize()` iterates all online players with nutrition data and calls `decaySatiationCounters()`. Runs every N minutes (configurable, default: 3 min).
- **Reset on death**: All counters clear (fresh start on respawn)
- **Not persisted to DB**: Satiation is transient session state. Resets on quit/rejoin. This is intentional — it's a short-term anti-spam mechanic, not long-term progression.

### Diminishing Returns

The satiation count determines a multiplier applied to macro gains from force-eating:

| Satiation Count | Multiplier | Chat Message |
|---|---|---|
| 0 | 1.0 (100%) | *(none)* |
| 1 | 0.7 (70%) | *(none)* |
| 2 | 0.4 (40%) | `"You're getting tired of <food>..."` |
| 3 | 0.15 (15%) | `"You can barely stomach more <food>."` |
| 4+ | 0.0 (hard cap) | `"You can't eat any more <food> right now."` |

- The tier lookup iterates from highest configured tier to lowest, returning the multiplier for the first tier whose count is <= the player's satiation count. Implementation: load tiers into a `TreeMap<Integer, Double>` and iterate `descendingMap()`.
- Chat messages use the food's display name (e.g., "Cooked Beef" not "COOKED_BEEF"). Derive via capitalization of `Material.name().replace("_", " ").toLowerCase()`.
- At hard cap (0% multiplier), the hunger is NOT nudged — the eat is fully blocked with the chat message. The player cannot start eating.

### What This Does NOT Change

- Normal eating (hunger < threshold) — no penalties, no counter increment, existing `onPlayerConsume` handler unchanged
- Vanilla hunger slowdown system — unchanged
- Death penalty, decay, effects, HUD — all untouched

## Config Schema

```yaml
FoodExpansion:
  Overeating:
    Enabled: true
    # Hunger level at or above which the nudge activates
    HungerThreshold: 20
    # Minutes between satiation counter decay ticks (each tick decrements by 1)
    DecayIntervalMinutes: 3
    # Cooldown in milliseconds between hunger nudges per player
    CooldownMs: 500
    # Satiation count -> multiplier mapping
    # Implementation iterates from highest to lowest; first count <= player's satiation wins
    Tiers:
      0: 1.0
      1: 0.7
      2: 0.4
      3: 0.15
      4: 0.0
    Messages:
      Warning: 2
      WarningText: "&7You're getting tired of &f{food}&7..."
      Severe: 3
      SevereText: "&7You can barely stomach more &f{food}&7."
      Blocked: 4
      BlockedText: "&cYou can't eat any more &f{food}&c right now."
```

## Data Model Changes

### PlayerNutritionData

Add to existing class:

- `Map<String, Integer> satiationCounters` — per-food satiation count (default: empty `HashMap`)
- `int getSatiation(String foodKey)` — returns `satiationCounters.getOrDefault(foodKey, 0)`
- `void incrementSatiation(String foodKey)` — `satiationCounters.merge(foodKey, 1, Integer::sum)`
- `void decaySatiationCounters()` — decrements all values by 1, removes entries that reach 0 via `Iterator.remove()`
- `void clearSatiationCounters()` — `satiationCounters.clear()`

No DB persistence. No dirty flag interaction (satiation is transient).

### FoodExpansionEvents

Add to existing class:

- `Set<UUID> forceEatingPlayers` — tracks players currently in force-eat flow
- `Map<UUID, Integer> forceEatingPreNudgeLevel` — stores pre-nudge food level for timeout cleanup
- `Map<UUID, Long> forceEatCooldowns` — tracks last nudge timestamp per player
- New `onPlayerInteract` handler (see Design section)
- Modify existing `onPlayerConsume` to check `forceEatingPlayers` and apply satiation multiplier. The combined multiplier is `overeatMultiplier * comfortMultiplier`, passed as a single value to `addNutrients(profile, finalMultiplier)`. No signature change needed.
- Modify existing `onFoodLevelChange` to skip drain logic when player is in `forceEatingPlayers`
- Add `clearSatiationCounters()` call in `onPlayerDeath`
- Cleanup: remove from `forceEatingPlayers`, `forceEatingPreNudgeLevel`, and `forceEatCooldowns` on quit

### No New Classes

All logic fits into existing classes. No new classes needed.

## Implementation Touchpoints

| File | Change |
|---|---|
| `PlayerNutritionData.java` | Add satiation counter map and methods |
| `FoodExpansionEvents.java` | Add `onPlayerInteract` handler for hunger nudge; modify `onPlayerConsume` for satiation multiplier; modify `onFoodLevelChange` to skip drain logic for force-eaters; add force-eat tracking maps; cleanup on quit/death |
| `FoodExpansionModule.java` | Register global satiation decay timer in `initialize()`; store and cancel its `BukkitTask` handle in `shutdown()` |
| `foodexpansion.yml` | Add `Overeating` config section |

## Edge Cases

- **Double-fire per click**: Filter on `event.getHand()` — only process the hand holding food.
- **Cancelled eating**: Covered in "Edge Case: Player Cancels Eating" section above — timeout cleanup with pre-nudge level restore.
- **Creative/spectator mode**: Skip entirely (already handled by module-level gamemode checks).
- **Non-food right-clicks**: Only trigger on `Material.isEdible()` items.
- **Comfort bonus**: Applied in `onPlayerConsume` as before. Combined with overeating multiplier: `finalMultiplier = overeatMultiplier * comfortMultiplier`.
- **0% multiplier (blocked)**: Don't nudge hunger, send chat message. Player cannot start eating.
- **Foods that restore 0 hunger**: Rare edge case. If a food restores 0 hunger points, the bar stays at 19 after eating. Self-corrects on next eat or hunger event.
- **Milk Bucket**: `Material.isEdible()` may include milk on some API versions. If so, it participates in the force-eat system like any other consumable.
- **Cooldown map cleanup**: Remove entries from `forceEatCooldowns` on player quit to prevent memory leaks.
