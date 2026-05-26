# Harshlands Changelog

## 1.3.3 — First Aid Heal Mechanics

Wires the bandage / splint / medical_kit items shipped in 1.3.1 to actually restore HP via the BodyHealth plugin's API. Items were inventory-only before this release; right-clicking now picks the most-injured allowed body part, caps the heal at missing HP, plays a sound, and consumes one item on success. Closes the largest promise-vs-reality gap in the project.

### Added

- First Aid items now actually heal. Right-clicking a bandage, splint, or medical_kit calls into the BodyHealth plugin (`Settings/firstaid.yml` → `Items.<name>.RestoreAmount` + `AffectsParts` + `Sound`) and restores HP to the most-injured allowed body part. Items are consumed only on a successful heal. When the BodyHealth plugin is not installed, disabled, or inactive in the player's world, a chat message tells the player and the item is not consumed.
- `firstaid.use.no_injury` and `firstaid.use.bodyhealth_unavailable` translation keys for the new use-result messages.

### Changed

- `Settings/firstaid.yml` `Items.<name>` blocks extended with `RestoreAmount` (HP units), `AffectsParts` (list of BodyHealth body-part names — `HEAD`, `TORSO`, `ARM_LEFT`, `ARM_RIGHT`, `LEG_LEFT`, `LEG_RIGHT`, `FOOT_LEFT`, `FOOT_RIGHT`), and `Sound`. `ConfigId` bumped to `1.3.3-RELEASE` so user configs pick up the new keys on next load.
- First Aid item lore corrected: bandage now lists torso/arm/leg/foot (not just arm/leg); splint now lists leg/foot (not just leg); medical_kit now describes healing the most-damaged body part (was incorrectly "fully restores every damaged body part"). Stale "Healing active once BodyHealth integration lands" placeholder removed from all three.

## 1.3.2 — BodyHealth, Guide & i18n

Headline release delivering the BodyHealth HUD integration deferred from 1.3.1, a first-join Survival Guide book, and a full item-lore translation pipeline across nine module groups. A new public `harshlands-api` Maven module exposes HUD / player surfaces so the companion BodyHealth plugin (and any future third-party) can drive the Harshlands HUD, and a bossbar Sentry keeps the Harshlands HUD anchor pinned when other bossbar plugins reshuffle the stack. New resource pack required: `Harshlands_RP_1.3.2_1.zip`.

### Added

- BodyHealth HUD integration — per-body-part HP rendered in the bottom-right corner via a vendored sprite font + BetterHud bridge. Ships the display, render task, silhouette composer, and the cross-plugin hook that the companion BodyHealth plugin populates with live HP state. `BodyHealth.Enabled` defaults to `false`, so existing servers don't change behavior; flip on after installing BodyHealth + BetterHud.
- `Settings/bodyhealth.yml` for HUD configuration and `BodyHealth.Debug.Render` flag for diagnostic logging when troubleshooting render issues.
- `/hl bdh onlypart <NAME>` — per-part render-bisection helper for BodyHealth HUD debugging.
- `harshlands-api` Maven module — public, un-relocated API surface for third-party plugins. Exposes `HarshlandsAPI` singleton, `HudManager`, `PlayerManager`, `HudPlayer`, `Hud`, and `PluginReloadedEvent` (fired on `/hl reload`).
- First-Join Survival Guide book — delivered automatically on first join and on version bump. New `/hl guide`, `/hl guide give <player>`, `/hl guide reset <player>` commands. Per-(locale, version) book cache; clickable table of contents with item-detail tags. `hl_guide_seen` DB table records delivery.
- `Settings/guide.yml` content (en-US authoritative); book width validated at 114px / 14 lines per page.
- Tiered thirst effects (`Thirst.Effects` config block) — Damp / Thirsty / Dehydrated / Parched apply scaled slowness and damage tiers instead of a single hard threshold. First-thirst hint now fires at Thirsty tier entry and explains the slowness.
- `/hl baubles [player]` — players can open their own bauble bag without holding the item; admins can view another player's. Tab-completes online players. Foreign-viewer clicks and drag-deposits are cancelled to prevent moving someone else's baubles while watching them.
- `harshlands.command.baubles` (default `true`) and `harshlands.command.guide` (default `true`) permissions; wildcard `harshlands.*` covers the admin guide subcommands.
- `item_stats.*` translation keys — armor / damage / attribute lore lines now route through `Messages` so non-English locales translate cleanly.
- `BossBar.SentryMode` config + bossbar Sentry — pinning system that protects the Harshlands HUD anchor when other bossbar plugins (BetterHud, etc.) reorder the stack. `AnchorRegistry` captures per-player anchor UUIDs; `BossbarReorderScheduler` debounces re-shows. Sentry installers wired for both `spigot_impl_1_21_R11` and `spigot_impl_26_1_R1`.

### Changed

- Food preview moved from a custom bossbar slot to the action bar. `DisplayTask` now suppresses its own action-bar send while preview is active. `AboveActionBarHUD` and the `harshlands:preview_text` font cells removed from the preview path; `preview_text` font reworked to `ascent -13000` + Bucket D for the surviving fallback uses.
- `Thirst.Dehydration` config block replaced by `Thirst.Effects`; `DehydrationTask` superseded by `ThirstEffectsTask` (cancels on player quit, points DisplayTask screen-tinting at the new key).
- Item `DisplayName` and `Lore` for nine module groups migrated to locale keys — baubles, firstaid, fear, toughasnails, notreepunching, iceandfire, spartanandfire, spartanweaponry, canteen. Lore presets moved into translations; the `i18n:` prefix is resolved at item-build time. Canteen runtime updater rewritten locale-safe so re-fills don't blow away translated lore. Damage-lore lines now matched by template rather than English literal.
- Default `ResourcePack.Url` bumped to `Harshlands_RP_1.3.2_1.zip` — required for BodyHealth HUD sprites and the `preview_text` unifont fallback.
- Locale loader translates `&`-codes once at load instead of per `get()` call; placeholder substitution now single-pass. Translation files and embedded-resource YAML readers force UTF-8 (fixes mojibake on Windows servers and non-Latin locales).
- `Messages` gains `getKeys(prefix)` for enumerating immediate child segments; `valueTemplateParts` extended to a multi-placeholder `TemplateParts` API. `Messages.getList` list-expansion path pinned by tests.
- Bossbar HUD title rebuild now short-circuited when `setElement` is a no-op; sentry inspection skips the full-packet encode path. Dead `first-call` branch in rate-warn removed.
- Food preview signature bitpacked into a `long` (cheaper equality + dirty-check) and `FoodPreviewState` shared predicate added so isCustomFood is called once per evaluation.
- `ThirstEffectsTask` reads thirst once per tick instead of multiple times per effect application.
- HUD config cached on the foodexpansion module rather than constructed per `DisplayTask` invocation.
- Guide delivery book built once at module load and cloned per `openBook` call.
- Bauble bag inventory title and Nightmare custom mob name now pulled from locale keys instead of hardcoded English.

### Fixed

- Empty thirst bar showed in worlds where the TAN module was disabled.
- Baubles slot counter counted bauble-bag slots toward the equipped count; slot names showed an incorrect identifier; JSON parsing emitted a warning for legacy player data.
- `CustomFoodRecipes` registration broke for items whose result was a Harshlands custom food.
- Guide book pages mis-measured non-ASCII characters at default-font width — now measured at unifont width so multi-byte locales fit the 114px line limit.
- Guide book `&`-codes in page content and tag templates were left raw — now translated; clickable tags restyled for the parchment book texture; "Pure Water" entry shortened to fit the 14-line book limit; trailing YAML newline stripped from page content.
- `/hl guide` tab-completion missing; guide module enabled by default; wildcard permission and help entry added.
- 1.3.1 → 1.3.2 translation migration (locale keys renamed / repathed) now applies on first load instead of silently leaving old keys in place. `lore.yml` `ConfigId` kept pinned at 1.3.1 until the explicit translation bump so user customizations aren't regenerated.
- `BodyHealthHook` initialized in `onLoad` so the API is exposed before dependent plugins call into it.
- Incorrect book checks producing false-positives on non-guide books.

### Known backlog (deferred to later releases)

- First Aid `bandage` / `splint` / `medical_kit` heal mechanics and `firstaid.damage.*` chat messages — still un-wired, waiting on the consumer side that calls into the BodyHealth API. The items remain inventory-only in 1.3.2; the HUD displays HP but the items don't yet restore it.
- Pet-origin tracking for `FIRST_PET_EATEN` hint — requires stamping an NBT flag on tamed-origin meat drops.
- `#17` IsLethal on legs / arms — needs design playtest.
- `#14` Golden Feast cost / benefit rebalance.
- `#25` Nightmare recovery panic ledge.
- `#49-50` Dynamic Surroundings hosting + enablement decision.
- Translation extraction for Ice and Fire, Spartan and Fire, Dynamic Surroundings beyond DisplayNames (Lore + ability text still partially English).
- Text-mode HUD fallback — not planned; the resource pack remains required.

## 1.3.1 — Polish Update

Player-experience polish pass addressing 130 findings from the 1.3.0 review. Focus on new-player onboarding, translation coverage, balance tuning, and tone consistency. No new gameplay systems. Resource pack remains required.

### Added

- `/hl help` now works for regular players and lists every 1.3.1 command, split into Survival / Self-service / Admin sections.
- 16 new progressive hints for first-time moments: cold exposure, heat exposure, shivering-as-noise, thirst warning, parasite onset/cured, low macro, well-nourished tier-up, overeating, pet eating (deferred wiring), broken limb, fear climb past 50, nightmare spawn, bauble first equip, first comfort buff, first cabin fever restless.
- 12 new `/hl obtain` item guides: campfire, fire_starter, jelled_slime, purified_water_bottle, charcoal_filter, canteen_empty, bandage, splint, medical_kit, thermometer, bauble_bag.
- First Aid bandage, splint, and medical_kit items with recipes (lore currently notes healing ships with the upcoming BodyHealth integration).
- First Aid injury chat messages (`firstaid.damage.*` translations) for head_critical / torso_critical / arm_broken / leg_broken / both_legs_broken / foot_broken — ready to wire when BodyHealth integration lands.
- `CabinFever.RequireMaterialRoof` + `NaturalRoofBlocks` config to exempt forest canopies from cabin-fever counting.
- `WarningLore` key on trap foods (`jelly`, `bat_wing`, `cooked_bat_wing`) — red warning lines now appear in the item tooltip.

### Changed

- `harshlands.command.help`, `command.version`, `command.fear`, `command.debug` defaults flipped from `op` to `true` so regular players can self-query.
- New `harshlands.command.hints.reset.self` (default `true`) — players can replay hints without an admin. `harshlands.admin.hints` continues to gate resetting other players. Admin perm is treated as a superset in the self path.
- NTP `PlankDrops` / `StickDrops` default chance raised from `0.4` to `0.6`, range tightened from `2-4` to `2-3` — less feast-or-famine for early-game wood.
- Ice and Fire `Dragon.SpawnChance` default dropped from `0.8` to `0.3` — gentler first contact for new servers; admins can raise for RLCraft-grade danger.

### Fixed

- `FIRST_SHIVERING` hint originally wired to the cold-breath particle visual; now correctly fires on the SoundEcology shivering-noise event (matches the hint's text and review intent).
- Fear triggers now clamp per-check via `MaxGainPerCheck` (default 3.0) so Darkness + Cave + Night + Enemies can no longer spike fear to 100 in <10s. `AllowPartialDecayUnderTriggers` lets PassiveDecay apply at half-rate when trigger load is light.
- Fear's `EatCookedFood` list now accepts Harshlands custom foods (cooked bacon, soups, bat soup, etc.) — not just vanilla cooked items.
- FakeMobSounds rate dropped from 0.05 → 0.012 per effects tick and `ENTITY_CREEPER_PRIMED` removed from the pool (it was driving players backward into actual traps). Heartbeat now has a 60-tick cooldown and quieter volume.
- Comfort tier thresholds raised: SHELTER 2-5, HOME 6-10, COZY 11-16, LUXURY 17+. New `DiminishingReturns` scoring (Factor 0.5, Cap 2×) makes spamming one category less rewarding. The easy 14-point LUXURY loadout now lands in COZY.
- Cabin fever no longer triggers under forest canopies (leaves/vines/glow-lichen/snow/mangrove-roots are excluded when `RequireMaterialRoof: true`).
- Macro starvation damage gated behind vanilla hunger threshold — players no longer take HP damage with a full hunger bar. Action-bar warning fires instead.
- Only water-type potions (WATER / MUNDANE / AWKWARD / THICK) now restore thirst. The brewing-stand-as-water-fountain loophole is closed.
- Thrown weapons returning to a full inventory are now owner-locked for 10 seconds with extended despawn (180s), so a throwable dropped over lava or in a contested area is recoverable.

### Added (Phase 2)

- Bauble drop-on-death chat notice on respawn (`baubles.death.baubles_dropped`) — tells the victim how many baubles dropped.
- `/hl comfort` now shows the delta to the next tier (`next_tier_hint`) or confirms max tier (`at_max_tier`).
- Tough As Nails death messages rewritten to match the Harshlands tone (dehydration / parasites / hyperthermia / hypothermia).
- Macro decay values now documented inline (units per minute of idle).

### Changed (Phase 3 — veteran balance)

- Golden Crown armor value dropped from 13 to 4 — single head-slot no longer contributes 65% of a full netherite set.
- Potion rings (resistance / regeneration / haste / strength / speed / jump_boost) now cap at `MaxStackedAmplifier: 2` per ring type. 10 speed rings no longer equals Speed XI.
- Sin pendants (wrath / pride / gluttony / sin) now have distinct mechanics — wrath = +damage on hit + Strength, pride = Resistance + 15% damage reflect (capped at 50% stacked), gluttony = Saturation + 0.75× hunger exhaustion, sin = combines all three without double-stacking.
- Ice & Fire dragons now show stage in their name (`"%VARIANT% Dragon — Stage %STAGE%"`). Brand-prefix dropped.
- Stage-1 dragons now drop 1-3 dragonscales (was 0 — killing a baby dragon was previously unrewarded).
- Rapier `UnarmoredDamageMultiplier` now tier-scales: netherite 2.0, diamond 2.25, gold/iron 2.5, copper 2.75, stone/wooden stay at 3.0. High-tier rapiers no longer one-shot unarmored targets.
- `dough` custom food now uses `BROWN_DYE` base material instead of `PAPER` — dough is no longer visually identical to paper without the resource pack.

### Fixed (Phase 3)

- Freeze ability (`dragonbone_iced_rapier`, `dragonsteel_ice_rapier`, 43 freeze weapons total) now only spawns encase ice in air/water positions. Player bases are no longer griefable via freeze.
- Ender Queen's Crown enforces `MaxConcurrentAllies: 5` per wearer — no more infinite enderman farms via tanking damage.

### Added (Phase 3 — content)

- 24 previously undocumented baubles now have meaningful lore in `Items/baubles/items.yml` — effects described honestly, items with no handler flagged as "mechanic pending" rather than given made-up descriptions.

### Known backlog (deferred to later releases)

- Book-based new-player guide (in lieu of a first-join chat greeting).
- BodyHealth plugin integration for body-part HP visibility, damage tracking, and bandage/splint heal mechanics. The 1.3.1 bandage / splint / medical_kit items are inventory-only until this lands.
- Pet-origin tracking for `FIRST_PET_EATEN` hint — requires stamping an NBT flag on tamed-origin meat drops.
- Text-mode HUD fallback — not planned; the resource pack is required.
- `#17` IsLethal on legs/arms — needs design playtest.
- `#14` Golden Feast cost/benefit rebalance.
- `#25` Nightmare recovery panic ledge.
- `#49-50` Dynamic Surroundings hosting + enablement decision.
- Translation extraction for Ice and Fire, Spartan and Fire, Dynamic Surroundings, Baubles modules (beyond the minimum in 1.3.1).

