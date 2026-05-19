# `rendertype_text.vsh` bucket layout

Harshlands ships a custom `rendertype_text.vsh` in its ItemsAdder resource pack
(`assets/minecraft/shaders/core/rendertype_text.vsh` + the `overlay_84/...`
copy for Minecraft 26.1.2). The shader lives in the deployed pack, not in this
Maven module — but the **bucket layout it depends on is the contract** between
the plugin's font assets and the renderer, so it's documented here under
version control.

Without the matching shader version, the bodyhealth ascents in
`assets/harshlands/font/bodyhealth.json` are meaningless. Update both in
lockstep.

## How buckets work

The shader has two paths for GUI text:

1. **Top-anchored**: `ascent = -(shaderY + 4095)`. The shader strips
   `ADD_HEIGHT_TOP = 4095` to recover the intended Y. Glyph is positioned
   `shaderY` pixels from the screen top. Used for transient elements where
   resolution dependence is acceptable.

2. **Bottom-anchored**: `pos.y >= ADD_HEIGHT_BOTTOM (8192)`. The shader hard-
   pins the quad's top and bottom vertices to fixed screen-bottom-relative
   Y values regardless of GUI scale or screen height. Bucket boundaries are
   spaced > 200 px apart so Mojang's per-slot Y bake-in (~19 px / slot)
   cannot cross them. Each bucket also pins the **height** of the glyph
   quad so the texture lands in a known screen rect.

## Bucket allocations

| Bucket | `pos.y` range    | Pinned `pos.y` (top → bottom) | Quad height | Used by |
|--------|------------------|-------------------------------|-------------|---------|
| A      | `[8192, 9000)`   | slot-relative legacy math     | varies      | preview_text atlas (drifts with slot Y; transient) |
| B      | `[9000, 10000)`  | `ui.y-75` → `ui.y-66`         | 9 px        | blank-space placeholder `U+E8B0` |
| C      | `[10000, 11000)` | `ui.y-105` → `ui.y-73`        | 32 px       | macronutrient icons `U+E8B1..E8B3` |
| D      | `[11000, 14000)` | `ui.y-105` → `ui.y-97`        | 8 px        | preview_text single-row atlas |
| E      | `[14000, 18000)` | `ui.y-76` → `ui.y-12`         | 64 px       | bodyhealth silhouette `U+E000..E027` |

The bodyhealth bucket E occupies `ui.y-76` to `ui.y-12` — a 64-px-tall
silhouette region pinned 12 px from the screen bottom.

## `bodyhealth.json` contract

For a bodyhealth glyph to render, its `font/bodyhealth.json` entry MUST
satisfy:

| Field   | Value                |
|---------|----------------------|
| ascent  | -14000               |
| height  | 64                   |
| PNG size| 32 × 64              |

The PNG is a transparent 32×64 canvas; the body part's visible pixels sit
at their natural (x, y) within the canvas. All 40 providers share these
values — there is no per-row differentiation.

`.scripts/regen_bodyhealth_font.py` regenerates `bodyhealth.json` from the
part / state list; if you edit the part list, run the script and verify
with `BodyHealthRenderStateTest`.

## Why single bucket E

This mirrors BetterHud's proven pattern: per-part PNG canvases pre-position
the visible pixels in both X and Y. The shader only needs to pin **where
the bodyhealth strip lives on screen** (ui.y-76 to ui.y-12). Per-row
sub-buckets and per-part canvas-X math (the prior design) added complexity
without buying anything that the BetterHud canvas approach doesn't get
intrinsically — and the failure mode of pixel-off bucket boundaries was
hard to debug.

## Updating the shader

When changing buckets:

1. Edit the deployed `.vsh` files (base + `overlay_84/`). Mirror by hand —
   they are not auto-generated.
2. Update this doc and the matching ascents in `bodyhealth.json` (via
   `.scripts/regen_bodyhealth_font.py` for bodyhealth, by hand for B/C/D).
3. `/iazip` and reload the pack on the client (or rebuild via the manual
   ritual in `docs/resource_pack/DEPLOY.md`).
