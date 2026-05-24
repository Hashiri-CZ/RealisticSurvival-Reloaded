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

`bodyhealth.json` contains 40 `bitmap` providers — one per body-part ×
state. There is **no** `space` provider. Each bitmap entry MUST satisfy:

| Field   | Value                |
|---------|----------------------|
| ascent  | -14000               |
| height  | 64                   |
| PNG size| 32 × 64              |

The PNG is a transparent 32×64 canvas; the body part's visible pixels sit
at their natural (x, y) within the canvas. All 40 providers share these
values — there is no per-row differentiation.

### Advance + bbox marker invariant

Every bodyhealth PNG MUST have two anchor markers (1/255 opacity, imperceptible):

- One at **(31, 0)** — top-right corner of the 32-wide canvas. Pins the
  cursor advance AND the top-right corner of the glyph's bbox.
- One at **(0, 63)** — bottom-left corner. Pins the bottom-left corner of
  the bbox so every glyph's bbox is uniformly **(0, 0, 32, 64)** regardless
  of where its natural visible content sits.

Why both markers are needed:

1. **Advance.** Mojang's `bitmap` font provider derives a glyph's cursor
   advance from the rightmost non-transparent pixel column, not from the
   declared canvas width — then adds a 1 px trailing gap. Without a marker
   at x=31 the body parts' visible content ends at different columns
   (arm_left at x=7, foot/leg at x=15, head/torso at x=23, arm_right at
   x=31), so Mojang would assign each codepoint a different advance. The
   marker at (31, 0) pins the rightmost opaque column to 31 for every
   glyph → uniform drawn width 32 → real cursor advance 32 + 1 = **33**.

2. **Glyph bbox.** Mojang also derives the glyph's vertical bbox from the
   topmost and bottommost non-transparent rows, and the bucket-E shader
   (ui.y-76 → ui.y-12, 64 px tall) expects the quad to span the full
   canvas. Parts whose natural content doesn't reach row 0 OR row 63
   (head 0-15, arm_right 16-39, leg_right 40-55, etc.) would produce
   smaller quads that the bucket-E shader cannot position consistently —
   manifesting as parts that simply don't render in the silhouette. The
   marker at (0, 63) anchors the bottom-left of the bbox, so combined
   with the (31, 0) marker every glyph's bbox is **(0, 0, 32, 64)**.

`BossbarHUD.rebuildTitle` emits a negative-space shift between each of the
eight same-anchor parts to cancel the previous glyph's advance, using the
value passed to `BossbarHUD.setElement`. That value MUST be
`BodyHealthRenderState.GLYPH_ADVANCE_PX = 33` (32 drawn + 1 trailing). If it
is 32 each shift under-cancels by 1 px and the silhouette shears apart; and
if the deployed pack is missing the marker the advances revert to their
natural per-part values and the parts scatter across the bossbar title (the
"only one part visible" bug).

The marker is stamped at 1/255 (~0.4%) opacity — imperceptible — and does
not affect rendering: Mojang draws the full 32-wide cell at the cursor
regardless of content, so only the advance changes. This mirrors BetterHud's
model (a fixed, known per-glyph advance plus HUD-driven shifts), achieved
here via canvas-width PNGs instead of runtime width bookkeeping.

`.scripts/stamp_bodyhealth_advance.py` re-applies the marker to every PNG
and is idempotent — run it after any bodyhealth art change.
`.scripts/regen_bodyhealth_font.py` regenerates `bodyhealth.json` from the
part / state list; if you edit the part list, run both scripts and verify
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
