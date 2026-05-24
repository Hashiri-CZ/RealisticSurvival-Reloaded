"""
Stamp 1-px markers into every bodyhealth part PNG to pin both the cursor
advance AND the rendered glyph's bounding box to a uniform 32x64 rect.

Two distinct problems this script solves:

1. **Cursor advance.** Mojang's `bitmap` font provider derives a glyph's
   advance from the rightmost non-transparent pixel column of the bitmap
   (NOT the declared width), then adds a 1 px trailing gap. Our 32x64 part
   canvases have visible content ending at different x columns (arm_left at
   x=7, foot/leg_left at x=15, head/torso at x=23, arm_right at x=31), so
   without an anchor at x=31 Mojang assigns each codepoint a different
   advance and the eight same-anchor parts scatter across the bossbar title
   -- the "only one part visible" bug.

   Fix: stamp a near-invisible (alpha=1) pixel at (31, 0). Mojang then
   measures a uniform drawn width of 32; with the 1 px trailing gap the
   real cursor advance is 33 (matches BodyHealthRenderState.GLYPH_ADVANCE_PX).

2. **Glyph bounding box.** Mojang's bitmap glyph also has a vertical bbox
   derived from the topmost and bottommost non-transparent rows. The shader
   in bucket E (ui.y-76 to ui.y-12) expects the quad to span the full 64 rows
   of the canvas. Parts whose natural content doesn't reach row 0 or row 63
   (arm_right at y=16-39, leg_right at y=40-55, head at y=0-15, etc.) end up
   with smaller quads that the bucket-E shader can't position consistently --
   some parts render off-screen or at the wrong Y.

   Fix: stamp a second alpha=1 marker at (0, 63). Combined with the (31, 0)
   marker this pins the bbox to the full (0, 0) -> (31, 63) rect for every
   glyph, regardless of where its natural content sits.

Both markers are at 1/255 (~0.4%) opacity -- imperceptible visually.

Idempotency: the script checks each exact marker pixel (not the whole row or
column). If both (31, 0) and (0, 63) are already non-transparent in a given
PNG, the file is left byte-identical and re-running produces no spurious git
diff. This is stricter than the previous "any opaque pixel in column 31"
check, which incorrectly skipped arm_right (whose natural arm content fills
column 31 at rows 16-39, so the previous check left arm_right with no marker
at row 0 -- producing a 8x24 bbox instead of 32x64).

Run from repo root: python .scripts/stamp_bodyhealth_advance.py
"""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

REPO = Path(__file__).resolve().parents[1]
# Default target: the in-repo asset source (used by Java JAR resources).
# Pass an explicit directory as argv[1] to stamp the deployed ItemsAdder pack
# (which is what actually ships to the client — the deployed pack is built from
# `…\Server\plugins\ItemsAdder\contents\Harshlands\assets\harshlands\textures\bodyhealth\`,
# NOT from the repo source).
DEFAULT_PNG_DIR = REPO / "core" / "src" / "main" / "resources" / "assets" / "harshlands" / "textures" / "bodyhealth"
PNG_DIR = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_PNG_DIR

CANVAS_W = 32     # must match BodyHealthRenderState.CANVAS_WIDTH_PX
CANVAS_H = 64
MARKER_RGBA = (255, 255, 255, 1)  # alpha=1 -> counted by Mojang, ~0.4% opacity

# Two anchor markers per PNG. (31, 0) pins the advance + top-right bbox corner;
# (0, 63) pins the bottom-left bbox corner. Together they force bbox=(0,0,32,64).
MARKERS = ((31, 0), (0, 63))


def stamp() -> None:
    pngs = sorted(PNG_DIR.glob("bodyhealth_*.png"))
    if len(pngs) != 40:
        raise SystemExit(f"expected 40 bodyhealth PNGs in {PNG_DIR}, found {len(pngs)}")
    stamped, already = 0, 0
    for path in pngs:
        img = Image.open(path).convert("RGBA")
        if img.size != (CANVAS_W, CANVAS_H):
            raise SystemExit(f"{path.name}: expected {CANVAS_W}x{CANVAS_H}, got {img.size[0]}x{img.size[1]}")
        missing = [(x, y) for (x, y) in MARKERS if img.getpixel((x, y))[3] == 0]
        if not missing:
            already += 1
            continue
        for (x, y) in missing:
            img.putpixel((x, y), MARKER_RGBA)
        img.save(path)
        stamped += 1
    print(f"[stamp] {stamped} stamped, {already} already had both markers ({len(pngs)} total)")


if __name__ == "__main__":
    stamp()
