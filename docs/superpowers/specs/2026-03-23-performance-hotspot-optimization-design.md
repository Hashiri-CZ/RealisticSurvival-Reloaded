# Performance Hotspot Optimization Design

**Date:** 2026-03-23
**Goal:** Fix the ~8 highest-impact runtime performance bottlenecks to reduce TPS impact and memory pressure.
**Scope:** Surgical fixes to existing files only. No architectural changes, no new classes, fully backwards compatible.

---

## 1. TemperatureEnvironmentTask — Block Scanning Loop

**File:** `core/src/main/java/cz/hashiri/harshlands/tan/TemperatureEnvironmentTask.java`

**Problem:** The triple-nested block scan (lines 68-83) creates 343 `new Location()` objects, calls `getBlock()` 343 times, and performs string concatenation + YAML config lookups per block via `willAffectTemperature()` and `add()`.

**Fix:**
- Cache `cubeLength` as a final field in the constructor.
- Replace `new Location(world, x, y, z).getBlock()` with ChunkSnapshot-based material lookup (see Section 7).
- Pre-parse the `Temperature.Environment.Blocks` config section into a `Map<Material, BlockTempEntry>` at construction:

```java
private record BlockTempEntry(double value, boolean isRegulatory) {}
private final Map<Material, BlockTempEntry> blockTempMap;
```

- The inner loop becomes a simple `Map.get()` — no string ops, no config reads:

```java
Material mat = getBlockTypeFromSnapshots(snapshots, ix, iy, iz);
if (mat.isAir()) continue;
BlockTempEntry entry = blockTempMap.get(mat);
if (entry == null) continue;
if (entry.isRegulatory()) regulate += entry.value();
else change += entry.value();
```

- The static `willAffectTemperature()`, `isRegulatory()`, `getValue()` methods that take `ConfigurationSection` are still called externally (audit callers before modifying). Keep backward-compatible wrappers that delegate to the pre-parsed map, or expose the map via a static accessor. Only remove the old methods once all callers are migrated.

**Impact:** Eliminates ~343 allocations + ~1000 string ops + ~1000 YAML lookups per player per trigger.

---

## 2. TemperatureCalculateTask — Config Value Caching

**File:** `core/src/main/java/cz/hashiri/harshlands/tan/TemperatureCalculateTask.java`

**Problem:** ~25+ `config.getDouble()`/`config.getBoolean()` calls per tick per player in `run()` (lines 101-289) and `add()` (lines 316-337). Each call walks a nested `Map<String, Object>` tree by splitting dot-separated path strings.

**Fix:**
Cache all config values as final fields in the constructor:

```java
// Biome temperature thresholds (6 cutoffs + 6 multipliers)
private final double hotCutoff, hotMultiplier;
private final double warmCutoff, warmMultiplier;
private final double moderateCutoff, moderateMultiplier;
private final double coolCutoff, coolMultiplier;
private final double coldCutoff, coldMultiplier;
private final double frigidMultiplier;

// Other per-tick values
private final double tempMaxChange;
private final double daylightCycleMultiplier;

// Effect thresholds
private final boolean hypothermiaEnabled;
private final double hypothermiaTemp;
private final boolean coldBreathEnabled;
private final double coldBreathMaxTemp;
private final boolean hyperthermiaEnabled;
private final double hyperthermiaTemp;
private final boolean sweatingEnabled;
private final double sweatingMinTemp;
```

Pre-parse `add()` config paths into a map. Populate by iterating all keys under `Temperature.Environment` and `Temperature.Armor` config sections (including dynamic armor material/item name keys like `Temperature.Armor.IRON_CHESTPLATE`, `Temperature.Armor.wool_hood`):

```java
private record AddEntry(double value, boolean isRegulatory, boolean hasEnabledFlag, boolean enabled) {}
private final Map<String, AddEntry> addEntries; // keyed by full config path

// Constructor: pre-iterate all config subsections
private Map<String, AddEntry> buildAddEntries(FileConfiguration config) {
    Map<String, AddEntry> map = new HashMap<>();
    for (String prefix : List.of("Temperature.Environment", "Temperature.Armor", "Temperature.Enchantments")) {
        ConfigurationSection section = config.getConfigurationSection(prefix);
        if (section == null) continue;
        for (String key : section.getKeys(false)) {
            String path = prefix + "." + key;
            if (!config.contains(path + ".Value")) continue;
            double value = config.getDouble(path + ".Value");
            boolean isReg = config.getBoolean(path + ".IsRegulatory", false);
            boolean hasEnabled = config.contains(path + ".Enabled");
            boolean enabled = config.getBoolean(path + ".Enabled", true);
            map.put(path, new AddEntry(value, isReg, hasEnabled, enabled));
        }
    }
    return map;
}
```

`add()` becomes a single map lookup:

```java
public void add(String configPath) {
    AddEntry entry = addEntries.get(configPath);
    if (entry == null) return;
    if (entry.hasEnabledFlag() && !entry.enabled()) return;
    if (entry.isRegulatory()) regulate += entry.value();
    else change += entry.value();
}
```

**Impact:** Eliminates ~25 YAML lookups per tick per player.

---

## 3. Thread-Unsafe Static HashMaps + Double Lookups

**Files (~30 task classes).** All classes with `static Map<UUID, ...> tasks = new HashMap<>()` or similar patterns, including but not limited to:
- **TAN:** `TemperatureCalculateTask`, `ThirstCalculateTask`, `HypothermiaTask`, `HyperthermiaTask`, `ColdBreathTask`, `SweatTask`, `DehydrationTask`, `ParasiteTask`, `ThermometerTask`, `DisplayTask`
- **Baubles:** `EnderCrownTask`, `BrokenHeartRepairTask`, `PotionBaubleTask`, `PolarizedStoneTask`, `MagicMirrorTask`, `StoneSeaTask`, `StoneGreaterInertiaTask`, `StoneNegativeGravityTask`, `ScarliteRingTask`, `WormholeMirrorTask`, `TickableBaubleManager`
- **Ice & Fire:** `TideGuardianTask`, `FreezeTask`, `BurnTask`, `ElectrocuteTask`
- **Spartan:** `TwoHandedTask`, `ThrowWeaponTask`, `EntityPrepareThrowTask`, `EntityLongAttackTask`
- **Fear:** `NightmareManager` (note: uses **instance** field, not static — still needs ConcurrentHashMap if accessed from async)
- **NTP:** `FireStarterTask`, `CeramicBucketMeltTask`
- **Spartan & Fire:** `UnfreezeTask`

**Problem:**
- Static `HashMap<UUID, TaskClass> tasks` registries are written from main thread and read from async contexts. `HashMap` is not thread-safe — concurrent access can cause infinite loops or lost entries.
- `hasTask()` does `containsKey() && get() != null` — double hash lookup.

**Fix:**
- Replace `new HashMap<>()` with `new ConcurrentHashMap<>()` in all task registries (static and instance).
- Replace `hasTask()` / `hasActiveNightmare()` implementations:

```java
// Before:
return tasks.containsKey(id) && tasks.get(id) != null;
// After:
return tasks.get(id) != null;
```

Mechanical find-and-replace. Grep for `new HashMap<>` in all task classes to ensure full coverage. No behavioral change.

**Impact:** Eliminates potential race condition crashes under load.

---

## 4. FootstepHandler — Config Reads Per Step

**File:** `core/src/main/java/cz/hashiri/harshlands/dynamicsurroundings/FootstepHandler.java`

**Problem:** 5 YAML `config.getDouble()` calls per player footstep (lines 332, 364-366, 381, 390). `PlayerMoveEvent` is one of the highest-frequency events in Bukkit.

**Fix:**
Cache all values as final fields in the constructor:

```java
private final double stepThreshold;
private final float footstepVolume;
private final float pitchVariance;
private final float armorOverlayVolume;
```

Reference cached fields in `handleMove()`, `playFootstepSound()`, `playLandSound()`, `playArmorOverlay()`.

**Impact:** Eliminates ~5 YAML lookups per player step.

---

## 5. ComfortScoreCalculator — Tier Resolution

**File:** `core/src/main/java/cz/hashiri/harshlands/comfort/ComfortScoreCalculator.java`

**Problem:** `resolveTier()` (lines 187-215) re-reads config tier definitions on every call — `getConfigurationSection()`, `getKeys()`, `getInt()` x2 per tier, `ComfortTier.valueOf()` string parse.

**Fix:**
Pre-build a list of tier thresholds in the constructor:

```java
private record TierRange(int minScore, int maxScore, ComfortTier tier) {}
private final List<TierRange> tierRanges;
```

`resolveTier()` becomes a simple loop over pre-built records — zero config reads, zero string parsing:

```java
private ComfortTier resolveTier(int score) {
    if (score <= 0) return ComfortTier.NONE;
    for (TierRange range : tierRanges) {
        if (score >= range.minScore && score <= range.maxScore) return range.tier;
    }
    return ComfortTier.NONE;
}
```

**Impact:** Eliminates config reads + string parsing per comfort evaluation.

---

## 6. NoiseEvaluationTask — Entity Scan Filtering

**File:** `core/src/main/java/cz/hashiri/harshlands/soundecology/NoiseEvaluationTask.java`

**Problem:**
- `world.getNearbyEntities()` (line 86) returns ALL entity types, then filters to `Monster` in the loop.
- Debug line (128) re-scans the entire collection via `.stream().filter().count()`.

**Fix:**
- Use the predicate overload to filter during scan:

```java
Collection<Entity> nearby = world.getNearbyEntities(loc, scanRadius, yRadius, scanRadius,
        entity -> entity instanceof Monster);
```

- Replace the debug stream with `nearby.size()` (already filtered):

```java
+ " hostileNearby=" + nearby.size();
```

**Impact:** Reduces entity collection size. Eliminates redundant stream scan.

---

## 7. TemperatureEnvironmentTask — ChunkSnapshot for Async Safety

**File:** `core/src/main/java/cz/hashiri/harshlands/tan/TemperatureEnvironmentTask.java`
**Also touches:** `TemperatureCalculateTask.java` (snapshot capture)

**Problem:** The environment task runs async (`runTaskAsynchronously`, line 207) but accesses blocks via `world.getBlockAt()` — not thread-safe in Bukkit. Can cause `ConcurrentModificationException`, stale data, or rare crashes during chunk load/unload.

**Fix:**
Use ChunkSnapshots — the canonical Bukkit pattern for async block reading:

1. In `TemperatureCalculateTask.run()` (main thread), capture snapshots before triggering the environment task:

```java
Map<Long, ChunkSnapshot> snapshots = getRelevantChunkSnapshots(player, cubeLength);
new TemperatureEnvironmentTask(module, plugin, this.player, snapshots).start();
```

2. The snapshot capture covers 1-4 chunks (cubeLength=4). Uses a `Map<Long, ChunkSnapshot>` keyed by packed chunk coordinates for O(1) lookup:

```java
private Map<Long, ChunkSnapshot> getRelevantChunkSnapshots(Player player, int cubeLength) {
    Location loc = player.getLocation();
    int minCX = (loc.getBlockX() - cubeLength) >> 4;
    int maxCX = (loc.getBlockX() + cubeLength) >> 4;
    int minCZ = (loc.getBlockZ() - cubeLength) >> 4;
    int maxCZ = (loc.getBlockZ() + cubeLength) >> 4;
    Map<Long, ChunkSnapshot> snapshots = new HashMap<>();
    World world = player.getWorld();
    for (int cx = minCX; cx <= maxCX; cx++) {
        for (int cz = minCZ; cz <= maxCZ; cz++) {
            if (world.isChunkLoaded(cx, cz)) {
                long key = ((long) cx << 32) | (cz & 0xFFFFFFFFL);
                snapshots.put(key, world.getChunkAt(cx, cz).getChunkSnapshot());
            }
        }
    }
    return snapshots;
}
```

3. In the environment task, look up block types from snapshots with O(1) map access. **Clamp Y to world bounds** to prevent `IllegalArgumentException`:

```java
private final int minY; // = world.getMinHeight(), captured in constructor
private final int maxY; // = world.getMaxHeight() - 1, captured in constructor

private Material getBlockTypeFromSnapshots(Map<Long, ChunkSnapshot> snapshots, int x, int y, int z) {
    if (y < minY || y > maxY) return Material.AIR;
    int cx = x >> 4;
    int cz = z >> 4;
    long key = ((long) cx << 32) | (cz & 0xFFFFFFFFL);
    ChunkSnapshot snap = snapshots.get(key);
    if (snap == null) return Material.AIR;
    return snap.getBlockType(x & 0xF, y, z & 0xF);
}
```

4. Use Material-only matching (no Lightable/Levelled BlockData checks). The pre-parsed `blockTempMap` from Section 1 already maps `Material -> BlockTempEntry`. **Known behavioral change:** Unlit campfires/furnaces will be treated as heat sources since ChunkSnapshot does not expose BlockData. This is an accepted tradeoff — the temperature contribution from a few unlit blocks is small relative to biome temperature, and the performance gain from keeping the scan async far outweighs the minor gameplay difference.

5. Keep `runTaskAsynchronously` — the scanning work stays off the main thread.

6. Mark `changeEnv` and `regulateEnv` fields in `TemperatureCalculateTask` as `volatile` to ensure correct cross-thread visibility (environment task writes from async, calculate task reads from main thread):

```java
private volatile double regulateEnv = 0D;
private volatile double changeEnv = 0D;
```

**Impact:** Eliminates async block access race conditions while keeping the scan off the main thread. Snapshot capture on the main thread is fast (1-4 chunk copies).

---

## Files Changed Summary

| File | Sections | Type of Change |
|---|---|---|
| `TemperatureEnvironmentTask.java` | 1, 7 | Major rewrite of scanning loop + ChunkSnapshot |
| `TemperatureCalculateTask.java` | 2, 7 | Add cached fields + snapshot capture helper |
| `FootstepHandler.java` | 4 | Cache 4 config values |
| `ComfortScoreCalculator.java` | 5 | Pre-build tier ranges |
| `NoiseEvaluationTask.java` | 6 | Add entity predicate filter |
| ~30 task classes (Section 3) | 3 | `HashMap` -> `ConcurrentHashMap`, fix `hasTask()` |

**No new files. No config format changes. Fully backwards compatible.**
