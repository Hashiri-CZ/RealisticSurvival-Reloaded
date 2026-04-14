# Store Listings for Harshlands

This file contains ready-to-paste store listings for **Modrinth** and **BuiltByBit**.
Replace all `<!-- PLACEHOLDER -->` tags with actual media before publishing.

---

# PART 1: MODRINTH (GitHub Flavored Markdown)

---

<!-- PLACEHOLDER: Banner image — upload to Modrinth gallery, paste URL below -->
![Harshlands Banner](https://raw.githubusercontent.com/Hashiri-CZ/Harshlands/refs/heads/master/.github/assets/banner.png)

## Survival was never meant to be easy.

**Harshlands** brings the RLCraft experience to your Spigot or Paper server — no client mods, no modloader, no launcher setup. Just a single plugin JAR that turns vanilla Minecraft into something players won't forget. **Free and open-source.**

<!-- PLACEHOLDER: YouTube trailer embed -->
<!-- <iframe width="560" height="315" src="https://www.youtube.com/embed/YOUR_TRAILER_ID" frameborder="0" allowfullscreen></iframe> -->

---

## What Is Harshlands?

Harshlands transforms a vanilla Minecraft server into a brutal, RLCraft-inspired survival experience — entirely server-side. Temperature drains your players. Thirst kills the careless. Dragons patrol the skies. And the darkness? The darkness fights back.

This is a maintained fork of [RealisticSurvival](https://github.com/ValMobile/RealisticSurvival), rebuilt for Minecraft 1.21.11 and 26.1.2 with new modules, a proper database layer, and active development. Every feature is modular — enable what you want, disable what you don't.

> **For server owners who want RLCraft-level difficulty without forcing players to install anything.**

---

## Feature Showcase

### 🌡️ Tough As Nails — Water is scarce. The cold is lethal.

Players must manage body temperature and hydration to survive. Deserts inflict hyperthermia and sweat dehydration. Tundras bring hypothermia, frostbite visuals, and frozen breath. Water isn't infinite anymore — players need to find it, purify it, and ration it. Parasites punish the reckless.

<!-- PLACEHOLDER: Screenshot — player in a desert with heat visual overlay -->
<!-- PLACEHOLDER: GIF — temperature HUD changing as player moves between biomes -->

---

### 👻 Fear — Something watches from the dark.

The darkness isn't empty. Players who wander without light build up fear — a hidden stat that warps their perception and, at its peak, summons the Nightmare. Torches burn out over time. Placed torches have a limited lifespan stored in the database. The only way to survive the night is to keep the light alive.

This is Harshlands' biggest original module — not present in the original RealisticSurvival.

<!-- PLACEHOLDER: Screenshot — player surrounded by darkness with fear visual effects -->
<!-- PLACEHOLDER: GIF — torch burning out, fear bar rising, Nightmare entity appearing -->

---

### 🐉 Ice & Fire — Here be dragons. They mean it.

Dragons, sea serpents, and mythical creatures roam your world as multi-stage boss encounters. Slay them to harvest dragonbone, dragonscale, and materials for the most powerful gear in the game. Fire dragons scorch forests. Ice dragons freeze lakes. This isn't a reskin — it's a full creature system with AI, loot tables, and progression.

<!-- PLACEHOLDER: Screenshot — fire dragon flying over a burning village -->
<!-- PLACEHOLDER: GIF — player fighting an ice dragon, dodging breath attacks -->

---

### 🪓 No Tree Punching — Forget everything you know about Day 1.

Punching trees does nothing. Players start with nothing and must scavenge flint from gravel, craft basic stone tools, and work their way up through realistic material progression. Early game becomes a genuine survival puzzle instead of a 30-second speedrun to diamond.

<!-- PLACEHOLDER: Screenshot — player crafting flint tools on the ground -->

---

### ⚔️ Spartan Weaponry — 14+ weapon types. 6 material tiers.

Halberds, rapiers, battleaxes, throwing knives, javelins, maces, and more. Each weapon type has unique reach, speed, and damage characteristics. Six material tiers from wood to netherite ensure progression stays meaningful long after iron. Combat actually has choices.

<!-- PLACEHOLDER: Screenshot — inventory showing multiple Spartan weapons -->
<!-- PLACEHOLDER: GIF — combat showcase with different weapon types -->

---

### 🔥 Spartan & Fire — Dragon-forged arsenals.

Combine Spartan Weaponry with Ice & Fire materials. Craft dragonbone and dragonsteel variants of every weapon type. These are endgame rewards for players who've earned them — not loot crate filler.

<!-- PLACEHOLDER: Screenshot — dragonsteel weapon set in an item frame display -->

---

### 💎 Baubles & Trinkets — Gear beyond armor slots.

Rings, amulets, and charms that players can equip for passive buffs and abilities. A dedicated bauble inventory accessed through a simple command. Adds an RPG layer to the survival loop without overwhelming complexity.

<!-- PLACEHOLDER: Screenshot — bauble inventory GUI with equipped trinkets -->

---

## Why Harshlands?

| | |
|---|---|
| **Updated** | Built for Minecraft 1.21.11 and 26.1.2 — not abandoned on 1.19 |
| **Original Content** | Fear module is new — not in any upstream fork |
| **Database-Backed** | H2 (embedded) or MySQL with HikariCP connection pooling |
| **Async Everything** | All database I/O runs off the main thread — no tick lag from saves |
| **100% Server-Side** | Players join and play. No client mods. No modloader. No friction. |
| **Fully Modular** | Toggle every module independently. Your server, your rules. |
| **Multi-Language** | Ships with English. Add any language by copying `Translations/en-US/` and translating. |
| **Open Source** | GPLv3. Read the code, fork it, contribute. |
| **Actively Maintained** | Regular updates, bug fixes, and new features |

---

## Requirements & Compatibility

| Requirement | Details |
|---|---|
| **Minecraft** | 1.21.11, 26.1.2 |
| **Server** | Spigot, Paper, or Purpur |
| **Java** | 21+ |

**Optional integrations:**

| Plugin | What it does |
|---|---|
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | Expose Harshlands stats in scoreboards, tab lists, etc. |
| [WorldGuard](https://enginehub.org/worldguard) | Region-based module control |
| [RealisticSeasons](https://www.spigotmc.org/resources/realisticseasons.93275/) | Season-aware temperature calculations |
| [AuraSkills](https://www.spigotmc.org/resources/auraskills.81069/) | Skill system integration |

**Resource pack:** Included and auto-sent to players on join. Can be disabled if you integrate textures into your own server pack.

---

## Installation

1. **Download** the latest JAR from [Releases](https://github.com/Hashiri-CZ/Harshlands/releases) or this page
2. **Drop** it into your server's `plugins/` folder
3. **Restart** your server — configs generate automatically in `plugins/Harshlands/`

Module settings live in `plugins/Harshlands/Settings/`, user-facing strings in `Translations/en-US/`, and item definitions in `Items/`. Toggle modules and set your locale in `config.yml`.

---

## Commands & Permissions

<details>
<summary>Click to expand full command & permission reference</summary>

### Commands

| Command | Description | Permission |
|---|---|---|
| `/harshlands` or `/hl` | Base command | `harshlands.command.*` |
| `/hl help` | Show help | `harshlands.command.help` |
| `/hl version` | Show plugin version | `harshlands.command.version` |
| `/hl reload` | Reload plugin configuration | `harshlands.command.reload` |
| `/hl give` | Give plugin items | `harshlands.command.give` |
| `/hl spawnitem` | Spawn plugin items in the world | `harshlands.command.spawnitem` |
| `/hl summon` | Summon plugin mobs | `harshlands.command.summon` |
| `/hl thirst` | Change a player's thirst | `harshlands.command.thirst` |
| `/hl temperature` | Change a player's temperature | `harshlands.command.temperature` |
| `/hl fear` | Check your fear level | `harshlands.command.fear` |
| `/hl fear <player>` | Check another player's fear | `harshlands.command.fear.others` |
| `/hl fear set <player> <value>` | Set a player's fear level | `harshlands.command.fear.set` |
| `/hl resetitem` | Reset an item for recipe use | `harshlands.command.resetitem` |
| `/hl updateitem` | Update an item | `harshlands.command.updateitem` |

### Permissions

**Command permissions** (default: OP)

| Permission | Description |
|---|---|
| `harshlands.command.*` | All commands |
| `harshlands.command.give` | Give items |
| `harshlands.command.spawnitem` | Spawn items |
| `harshlands.command.summon` | Summon mobs |
| `harshlands.command.reload` | Reload config |
| `harshlands.command.thirst` | Manage thirst |
| `harshlands.command.temperature` | Manage temperature |
| `harshlands.command.resetitem` | Reset items |
| `harshlands.command.updateitem` | Update items |
| `harshlands.command.help` | Help command |
| `harshlands.command.version` | Version info |
| `harshlands.command.fear` | Check own fear |
| `harshlands.command.fear.others` | Check others' fear |
| `harshlands.command.fear.set` | Set fear levels |

**Resistance permissions** (default: false — grant to exempt players)

| Permission | Description |
|---|---|
| `harshlands.toughasnails.resistance.*` | All TAN resistances |
| `harshlands.toughasnails.resistance.cold.*` | All cold resistances |
| `harshlands.toughasnails.resistance.cold.damage` | No cold damage |
| `harshlands.toughasnails.resistance.cold.potioneffects` | No cold debuffs |
| `harshlands.toughasnails.resistance.cold.visual` | No freeze overlay |
| `harshlands.toughasnails.resistance.cold.breath` | No cold breath particles |
| `harshlands.toughasnails.resistance.hot.*` | All heat resistances |
| `harshlands.toughasnails.resistance.hot.damage` | No heat damage |
| `harshlands.toughasnails.resistance.hot.combustion` | No spontaneous ignition |
| `harshlands.toughasnails.resistance.hot.potioneffects` | No heat debuffs |
| `harshlands.toughasnails.resistance.hot.visual` | No heat overlay |
| `harshlands.toughasnails.resistance.hot.sweat` | No sweat particles |
| `harshlands.toughasnails.resistance.thirst.*` | All thirst resistances |
| `harshlands.toughasnails.resistance.thirst` | No thirst drain |
| `harshlands.toughasnails.resistance.thirst.damage` | No dehydration damage |
| `harshlands.toughasnails.resistance.thirst.potioneffects` | No thirst debuffs |
| `harshlands.toughasnails.resistance.thirst.visual` | No thirst overlay |
| `harshlands.toughasnails.resistance.parasite.*` | All parasite resistances |
| `harshlands.toughasnails.resistance.parasite` | No parasites |
| `harshlands.toughasnails.resistance.parasite.damage` | No parasite damage |
| `harshlands.toughasnails.resistance.parasite.potioneffects` | No parasite debuffs |
| `harshlands.iceandfire.resistance.*` | All Ice & Fire resistances |
| `harshlands.iceandfire.resistance.sirenvisual` | No siren screen effects |

</details>

---

## Support & Links

- **Discord:** [Join the community](https://discord.gg/2EkszXWxbE)
- **GitHub:** [Source code & issues](https://github.com/Hashiri-CZ/Harshlands)
- **License:** [GPLv3](https://github.com/Hashiri-CZ/Harshlands/blob/main/LICENSE) — free and open-source, forever

Found a bug? [Open an issue on GitHub.](https://github.com/Hashiri-CZ/Harshlands/issues)

---

## Credits & Acknowledgments

Harshlands is a maintained fork of [RealisticSurvival](https://github.com/ValMobile/RealisticSurvival) by **ValMobile**, with contributions from [Nik0-0's updated fork](https://github.com/Nikos-Stuff/RealisticSurvival-Updated). We gratefully acknowledge the original mod creators whose work inspired the textures and mechanics:

Azanor13 (Baubles), gr8pefish & BBoldt (Bauble Bag Texture), mfnalex (Best Tools Code), Cursed1nferno (Bountiful Baubles), TeamCoFH & Drullkus (Copper Textures), Lycanite & Shivaxi (Lycanite's Mobs), Raptorfarian & Alexthe666 (Ice and Fire), Raptorfarian & Janivire (Spartan and Fire 1.12.2), Kreloxcz & ChoglixVT (Spartan and Fire 1.16.5), ObliviousSpartan (Spartan Shields), ObliviousSpartan & xwerffx (Spartan Weaponry), CreativeMD, fonnymunkey & ariafreeze (Tint Textures), XzeroAir & sonicx8000 (Trinkets and Baubles), Tmtravlr (Quality Tools), AlcatrazEscapee (No Tree Punching), TheAdubbz (Tough As Nails), FN-FAL113 (Throwable Weapons), BlayTheNinth & CFGrafanaStats (Waystones), Shivaxi (RLCraft), Chaosyr (Realistic Torches).

---

## Disclaimer

Harshlands is an independent, community-driven project. It is **not affiliated with, endorsed by, or connected to** Mojang Studios, Microsoft, Minecraft, or Shivaxi (RLCraft). All trademarks are the property of their respective owners.

---

Made by **Hashiri_** and the Harshlands community.

---
---
---

# PART 2: BUILTBYBIT (BBCode — Standard Tags Only)

Copy everything between the `=== START BBCODE ===` and `=== END BBCODE ===` markers.

```
=== START BBCODE ===
```

```bbcode
[CENTER]
[IMG]https://raw.githubusercontent.com/Hashiri-CZ/Harshlands/refs/heads/master/.github/assets/banner.png[/IMG]

[SIZE=7][B][COLOR=#c9302c]Survival was never meant to be easy.[/COLOR][/B][/SIZE]

[SIZE=4][B]The RLCraft experience for Spigot & Paper — no client mods, no modloader, no setup.[/B]
Free and open-source. One JAR. Drop it in. Watch them suffer.[/SIZE]
[/CENTER]

[SIZE=1] [/SIZE]

[SIZE=6][B][COLOR=#5cb85c]What Is Harshlands?[/COLOR][/B][/SIZE]

[SIZE=3]Harshlands transforms a vanilla Minecraft server into a brutal, RLCraft-inspired survival experience — entirely server-side. Temperature drains your players. Thirst kills the careless. Dragons patrol the skies. And the darkness? The darkness fights back.

This is a maintained fork of [URL='https://github.com/ValMobile/RealisticSurvival']RealisticSurvival[/URL], rebuilt for Minecraft 1.21.11 and 26.1.2 with new modules, a proper database layer, and active development. Every feature is modular — enable what you want, disable what you don't.

[B]For server owners who want RLCraft-level difficulty without forcing players to install anything.[/B][/SIZE]

[SIZE=1] [/SIZE]

[SIZE=6][B][COLOR=#f0ad4e]Feature Showcase[/COLOR][/B][/SIZE]

[SIZE=1] [/SIZE]

[SIZE=5][B][COLOR=#e74c3c]Tough As Nails — Water is scarce. The cold is lethal.[/COLOR][/B][/SIZE]

[SIZE=3]Players must manage body temperature and hydration to survive. Deserts inflict hyperthermia and sweat dehydration. Tundras bring hypothermia, frostbite visuals, and frozen breath. Water isn't infinite anymore — players need to find it, purify it, and ration it. Parasites punish the reckless.[/SIZE]

[IMG]PLACEHOLDER_SCREENSHOT_TEMPERATURE[/IMG]

[SIZE=1] [/SIZE]

[SIZE=5][B][COLOR=#9b59b6]Fear — Something watches from the dark.[/COLOR][/B][/SIZE]

[SIZE=3]The darkness isn't empty. Players who wander without light build up fear — a hidden stat that warps their perception and, at its peak, summons the Nightmare. Torches burn out over time. Placed torches have a limited lifespan. The only way to survive the night is to keep the light alive.

[B]This is Harshlands' biggest original module — not present in the original RealisticSurvival.[/B][/SIZE]

[IMG]PLACEHOLDER_SCREENSHOT_FEAR[/IMG]

[SIZE=1] [/SIZE]

[SIZE=5][B][COLOR=#e67e22]Ice & Fire — Here be dragons. They mean it.[/COLOR][/B][/SIZE]

[SIZE=3]Dragons, sea serpents, and mythical creatures roam your world as multi-stage boss encounters. Slay them to harvest dragonbone, dragonscale, and materials for the most powerful gear in the game. Fire dragons scorch forests. Ice dragons freeze lakes. Full creature system with AI, loot tables, and progression.[/SIZE]

[IMG]PLACEHOLDER_SCREENSHOT_DRAGONS[/IMG]

[SIZE=1] [/SIZE]

[SIZE=5][B][COLOR=#95a5a6]No Tree Punching — Forget everything you know about Day 1.[/COLOR][/B][/SIZE]

[SIZE=3]Punching trees does nothing. Players start with nothing and must scavenge flint from gravel, craft basic stone tools, and work their way up through realistic material progression. Early game becomes a genuine survival puzzle instead of a 30-second speedrun to diamond.[/SIZE]

[IMG]PLACEHOLDER_SCREENSHOT_NTP[/IMG]

[SIZE=1] [/SIZE]

[SIZE=5][B][COLOR=#3498db]Spartan Weaponry — 14+ weapon types. 6 material tiers.[/COLOR][/B][/SIZE]

[SIZE=3]Halberds, rapiers, battleaxes, throwing knives, javelins, maces, and more. Each weapon type has unique reach, speed, and damage characteristics. Six material tiers from wood to netherite. Combat actually has choices.[/SIZE]

[IMG]PLACEHOLDER_SCREENSHOT_WEAPONS[/IMG]

[SIZE=1] [/SIZE]

[SIZE=5][B][COLOR=#e74c3c]Spartan & Fire — Dragon-forged arsenals.[/COLOR][/B][/SIZE]

[SIZE=3]Combine Spartan Weaponry with Ice & Fire materials. Craft dragonbone and dragonsteel variants of every weapon type. Endgame rewards for players who've earned them.[/SIZE]

[IMG]PLACEHOLDER_SCREENSHOT_DRAGONWEAPONS[/IMG]

[SIZE=1] [/SIZE]

[SIZE=5][B][COLOR=#1abc9c]Baubles & Trinkets — Gear beyond armor slots.[/COLOR][/B][/SIZE]

[SIZE=3]Rings, amulets, and charms that players can equip for passive buffs and abilities. A dedicated bauble inventory accessed through a simple command. Adds an RPG layer without overwhelming complexity.[/SIZE]

[IMG]PLACEHOLDER_SCREENSHOT_BAUBLES[/IMG]

[SIZE=1] [/SIZE]

[SIZE=6][B][COLOR=#5cb85c]Why Harshlands?[/COLOR][/B][/SIZE]

[LIST]
[*][B]Updated[/B] — Built for Minecraft 1.21.11 and 26.1.2, not abandoned on 1.19
[*][B]Original content[/B] — Fear module is new, not in any upstream fork
[*][B]Database-backed[/B] — H2 (embedded) or MySQL with HikariCP connection pooling
[*][B]Async saves[/B] — All database I/O runs off the main thread, no tick lag
[*][B]100% server-side[/B] — Players join and play. No client mods. No modloader. No friction.
[*][B]Fully modular[/B] — Toggle every module independently
[*][B]Multi-language[/B] — Ships with English. Add any language by copying [I]Translations/en-US/[/I] and translating.
[*][B]Open source[/B] — GPLv3. Read the code, fork it, contribute.
[*][B]Actively maintained[/B] — Regular updates, bug fixes, and new features
[/LIST]

[SIZE=1] [/SIZE]

[SIZE=6][B][COLOR=#5cb85c]Requirements & Compatibility[/COLOR][/B][/SIZE]

[LIST]
[*][B]Minecraft:[/B] 1.21.11, 26.1.2
[*][B]Server:[/B] Spigot, Paper, or Purpur
[*][B]Java:[/B] 21+
[/LIST]

[B]Optional integrations:[/B]
[LIST]
[*][URL='https://www.spigotmc.org/resources/placeholderapi.6245/']PlaceholderAPI[/URL] — Expose stats in scoreboards, tab lists, etc.
[*][URL='https://enginehub.org/worldguard']WorldGuard[/URL] — Region-based module control
[*][URL='https://www.spigotmc.org/resources/realisticseasons.93275/']RealisticSeasons[/URL] — Season-aware temperature
[*][URL='https://www.spigotmc.org/resources/auraskills.81069/']AuraSkills[/URL] — Skill system integration
[/LIST]

[B]Resource pack:[/B] Included and auto-sent to players on join. Can be disabled if you integrate textures into your own server pack.

[SIZE=1] [/SIZE]

[SIZE=6][B][COLOR=#5cb85c]Installation[/COLOR][/B][/SIZE]

[LIST=1]
[*][B]Download[/B] the latest JAR from [URL='https://github.com/Hashiri-CZ/Harshlands/releases']GitHub Releases[/URL] or this page
[*][B]Drop[/B] it into your server's [I]plugins/[/I] folder
[*][B]Restart[/B] your server — configs generate automatically in [I]plugins/Harshlands/[/I]
[/LIST]

Module settings live in [I]plugins/Harshlands/Settings/[/I], user-facing strings in [I]Translations/en-US/[/I], and item definitions in [I]Items/[/I]. Toggle modules and set your locale in [I]config.yml[/I].

[SIZE=1] [/SIZE]

[SIZE=6][B][COLOR=#5cb85c]Commands & Permissions[/COLOR][/B][/SIZE]

[SPOILER=Commands]
[B]/harshlands[/B] or [B]/hl[/B] — Base command ([I]harshlands.command.*[/I])
[B]/hl help[/B] — Show help ([I]harshlands.command.help[/I])
[B]/hl version[/B] — Plugin version ([I]harshlands.command.version[/I])
[B]/hl reload[/B] — Reload config ([I]harshlands.command.reload[/I])
[B]/hl give[/B] — Give plugin items ([I]harshlands.command.give[/I])
[B]/hl spawnitem[/B] — Spawn items in world ([I]harshlands.command.spawnitem[/I])
[B]/hl summon[/B] — Summon plugin mobs ([I]harshlands.command.summon[/I])
[B]/hl thirst[/B] — Change player thirst ([I]harshlands.command.thirst[/I])
[B]/hl temperature[/B] — Change player temp ([I]harshlands.command.temperature[/I])
[B]/hl fear[/B] — Check your fear ([I]harshlands.command.fear[/I])
[B]/hl fear <player>[/B] — Check other's fear ([I]harshlands.command.fear.others[/I])
[B]/hl fear set <player> <value>[/B] — Set fear level ([I]harshlands.command.fear.set[/I])
[B]/hl resetitem[/B] — Reset item for recipes ([I]harshlands.command.resetitem[/I])
[B]/hl updateitem[/B] — Update an item ([I]harshlands.command.updateitem[/I])
[/SPOILER]

[SPOILER=Resistance Permissions (grant to exempt players)]
[B]Tough As Nails:[/B]
[I]harshlands.toughasnails.resistance.*[/I] — All TAN resistances
[I]harshlands.toughasnails.resistance.cold.*[/I] — All cold resistances
[I]harshlands.toughasnails.resistance.cold.damage[/I] — No cold damage
[I]harshlands.toughasnails.resistance.cold.potioneffects[/I] — No cold debuffs
[I]harshlands.toughasnails.resistance.cold.visual[/I] — No freeze overlay
[I]harshlands.toughasnails.resistance.cold.breath[/I] — No cold breath
[I]harshlands.toughasnails.resistance.hot.*[/I] — All heat resistances
[I]harshlands.toughasnails.resistance.hot.damage[/I] — No heat damage
[I]harshlands.toughasnails.resistance.hot.combustion[/I] — No spontaneous ignition
[I]harshlands.toughasnails.resistance.hot.potioneffects[/I] — No heat debuffs
[I]harshlands.toughasnails.resistance.hot.visual[/I] — No heat overlay
[I]harshlands.toughasnails.resistance.hot.sweat[/I] — No sweat particles
[I]harshlands.toughasnails.resistance.thirst.*[/I] — All thirst resistances
[I]harshlands.toughasnails.resistance.thirst[/I] — No thirst drain
[I]harshlands.toughasnails.resistance.thirst.damage[/I] — No dehydration damage
[I]harshlands.toughasnails.resistance.thirst.potioneffects[/I] — No thirst debuffs
[I]harshlands.toughasnails.resistance.thirst.visual[/I] — No thirst overlay
[I]harshlands.toughasnails.resistance.parasite.*[/I] — All parasite resistances
[I]harshlands.toughasnails.resistance.parasite[/I] — No parasites
[I]harshlands.toughasnails.resistance.parasite.damage[/I] — No parasite damage
[I]harshlands.toughasnails.resistance.parasite.potioneffects[/I] — No parasite debuffs

[B]Ice & Fire:[/B]
[I]harshlands.iceandfire.resistance.*[/I] — All I&F resistances
[I]harshlands.iceandfire.resistance.sirenvisual[/I] — No siren screen effects
[/SPOILER]

[SIZE=1] [/SIZE]

[SIZE=6][B][COLOR=#5cb85c]Support & Links[/COLOR][/B][/SIZE]

[LIST]
[*][B]Discord:[/B] [URL='https://discord.gg/2EkszXWxbE']Join the community[/URL]
[*][B]GitHub:[/B] [URL='https://github.com/Hashiri-CZ/Harshlands']Source code & issues[/URL]
[*][B]License:[/B] [URL='https://github.com/Hashiri-CZ/Harshlands/blob/main/LICENSE']GPLv3[/URL] — free and open-source, forever
[/LIST]

Found a bug? [URL='https://github.com/Hashiri-CZ/Harshlands/issues']Open an issue on GitHub.[/URL]

[SIZE=1] [/SIZE]

[SIZE=6][B][COLOR=#5cb85c]Credits & Acknowledgments[/COLOR][/B][/SIZE]

[SIZE=3]Harshlands is a maintained fork of [URL='https://github.com/ValMobile/RealisticSurvival']RealisticSurvival[/URL] by [B]ValMobile[/B], with contributions from [URL='https://github.com/Nikos-Stuff/RealisticSurvival-Updated']Nik0-0's updated fork[/URL]. We gratefully acknowledge the original mod creators whose work inspired the textures and mechanics:

Azanor13 (Baubles), gr8pefish & BBoldt (Bauble Bag Texture), mfnalex (Best Tools Code), Cursed1nferno (Bountiful Baubles), TeamCoFH & Drullkus (Copper Textures), Lycanite & Shivaxi (Lycanite's Mobs), Raptorfarian & Alexthe666 (Ice and Fire), Raptorfarian & Janivire (Spartan and Fire 1.12.2), Kreloxcz & ChoglixVT (Spartan and Fire 1.16.5), ObliviousSpartan (Spartan Shields), ObliviousSpartan & xwerffx (Spartan Weaponry), CreativeMD, fonnymunkey & ariafreeze (Tint Textures), XzeroAir & sonicx8000 (Trinkets and Baubles), Tmtravlr (Quality Tools), AlcatrazEscapee (No Tree Punching), TheAdubbz (Tough As Nails), FN-FAL113 (Throwable Weapons), BlayTheNinth & CFGrafanaStats (Waystones), Shivaxi (RLCraft), Chaosyr (Realistic Torches).[/SIZE]

[SIZE=1] [/SIZE]

[SIZE=6][B]Disclaimer[/B][/SIZE]

[SIZE=3]Harshlands is an independent, community-driven project. It is [B]not affiliated with, endorsed by, or connected to[/B] Mojang Studios, Microsoft, Minecraft, or Shivaxi (RLCraft). All trademarks are the property of their respective owners.[/SIZE]

[CENTER]
[SIZE=3]Made by [B]Hashiri_[/B] and the Harshlands community.[/SIZE]
[/CENTER]
```

```
=== END BBCODE ===
```

---
---
---

# PART 3: NOTES — Media Asset Suggestions

## Screenshots (10 suggestions)

1. **Hero banner** — Player silhouette against a sunset with Harshlands logo overlay
2. **Temperature HUD** — Player in a desert biome with the heat overlay active, thirst bar visible
3. **Frozen tundra** — Player in snow biome with frost overlay, cold breath particles
4. **Fear darkness** — Player in a cave with minimal torchlight, fear visual effects creeping in
5. **Nightmare encounter** — The Nightmare entity appearing in darkness (dramatic angle)
6. **Fire dragon** — Dragon mid-flight over a burning forest, player in foreground with shield
7. **Ice dragon battle** — Player fighting an ice dragon, frozen terrain visible
8. **Flint crafting** — Player on Day 1 with No Tree Punching, crafting primitive tools
9. **Weapon display** — Item frames showing the full Spartan Weaponry + Spartan & Fire arsenal
10. **Bauble inventory** — The bauble GUI open with equipped rings/amulets and their tooltip descriptions

## GIFs (7 suggestions)

1. **Biome temperature transition** — Player walking from plains into desert, HUD changing in real-time
2. **Torch burnout** — Placed torch gradually burning down and extinguishing
3. **Fear escalation** — Fear bar rising as player stands in darkness, culminating in Nightmare spawn
4. **Dragon combat** — 10-second clip of fighting a fire dragon (dodge, attack, loot)
5. **Weapon variety** — Quick montage of different Spartan weapons being used in combat
6. **No Tree Punching progression** — Flint knapping → stone tools → first tree broken
7. **Bauble equip** — Opening bauble inventory, equipping a ring, visual buff activating

## Videos (2 suggestions)

1. **Trailer** (60-90 seconds) — Cinematic montage: peaceful vanilla → Harshlands chaos. Temperature, fear, dragons, combat. End with tagline: "Survival was never meant to be easy."
2. **Setup guide** (3-5 minutes) — Download, install, first config walkthrough, module toggles, first join experience.

## Banner / Logo

- Use the existing banner at: `https://raw.githubusercontent.com/Hashiri-CZ/Harshlands/refs/heads/master/.github/assets/banner.png`
- Modrinth icon: crop or create a square version (512x512) from the banner
- BuiltByBit icon: same square crop, ensure it reads well at small sizes
