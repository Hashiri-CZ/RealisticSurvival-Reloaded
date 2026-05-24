"""
Stamp a 1-px advance marker at (31, 0) into every bodyhealth part PNG.

Why a single (31, 0) marker is sufficient:

  Mojang's bitmap font provider derives a glyph's CURSOR ADVANCE from the
  rightmost non-transparent pixel column of the cell, then adds a 1 px
  trailing gap (verified against `BitmapFont.Loader.load` in Mojang's 1.21.1
  source). Our 32x64 part canvases have visible content ending at different
  columns (arm_left at x=7, foot/leg_left at x=15, head/torso at x=23,
  arm_right at x=31), so without a uniform anchor at x=31 Mojang would
  assign each codepoint a different advance and the eight same-anchor
  parts would scatter across the bossbar title.

  The fix: stamp a near-invisible (alpha=1) pixel at (31, 0). Mojang's
  findCharacterStartX then measures a uniform drawn width of 32; with the
  1 px trailing gap the real cursor advance is 33 (matches
  BodyHealthRenderState.GLYPH_ADVANCE_PX).

Why NO bbox-uniformity marker is needed:

  Mojang's BakedGlyph uses width=cellW and height=cellH (the FULL PNG cell
  dimensions, 32x64), NOT a trimmed bbox of opaque pixels. The rendered
  quad always covers the entire cell. The prior dual-marker scheme
  (additional pixel at (0, 63)) was based on a misreading of the source
  and was actively harmful: it triggered glyph-atlas re-packs without
  fixing any real problem. The atlas-position-dependent rendering issue
  it appeared to address is actually a shader bug (see
  rendertype_text.vsh bucket E for the corresponding offset-based fix).

Idempotency:

  Checks the exact (31, 0) pixel (not the whole column). The previous
  column-wide check `column_has_opaque(x=31)` was too lax for arm_right
  (whose natural arm content fills column 31 at rows 16-39), so the script
  skipped stamping arm_right and left it without the advance anchor. The
  pixel-specific check below correctly stamps all 40 PNGs uniformly.

(0, 63) rollback:

  Any PNG with non-transparent alpha at (0, 63) is from the prior
  dual-marker era. This script clears that pixel as part of every run, so
  re-running on a dual-marker pack restores the single-marker invariant.

Run from repo root:
    python .scripts/stamp_bodyhealth_advance.py
    python .scripts/stamp_bodyhealth_advance.py <deployed-pack-textures-dir>
"""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

REPO = Path(__file__).resolve().parents[1]
DEFAULT_PNG_DIR = REPO / "core" / "src" / "main" / "resources" / "assets" / "harshlands" / "textures" / "bodyhealth"
PNG_DIR = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_PNG_DIR

CANVAS_W = 32     # must match BodyHealthRenderState.CANVAS_WIDTH_PX
CANVAS_H = 64
MARKER_XY = (31, 0)
MARKER_RGBA = (255, 255, 255, 1)  # alpha=1 -> counted by Mojang, ~0.4% opacity
CLEAR_XY = (0, 63)                # rollback from prior dual-marker era
CLEAR_RGBA = (0, 0, 0, 0)


def stamp() -> None:
    pngs = sorted(PNG_DIR.glob("bodyhealth_*.png"))
    if len(pngs) != 40:
        raise SystemExit(f"expected 40 bodyhealth PNGs in {PNG_DIR}, found {len(pngs)}")
    stamped = cleared = already = 0
    for path in pngs:
        img = Image.open(path).convert("RGBA")
        if img.size != (CANVAS_W, CANVAS_H):
            raise SystemExit(f"{path.name}: expected {CANVAS_W}x{CANVAS_H}, got {img.size[0]}x{img.size[1]}")
        needs_stamp = img.getpixel(MARKER_XY)[3] == 0
        needs_clear = img.getpixel(CLEAR_XY)[3] != 0
        if not needs_stamp and not needs_clear:
            already += 1
            continue
        if needs_stamp:
            img.putpixel(MARKER_XY, MARKER_RGBA)
            stamped += 1
        if needs_clear:
            img.putpixel(CLEAR_XY, CLEAR_RGBA)
            cleared += 1
        img.save(path)
    print(f"[stamp] {stamped} stamped, {cleared} (0,63)-cleared, {already} already correct ({len(pngs)} total)")


if __name__ == "__main__":
    stamp()
