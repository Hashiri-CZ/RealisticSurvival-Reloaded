"""
Stamp a 1-px advance marker into every bodyhealth part PNG.

Mojang's `bitmap` font provider computes a glyph's cursor advance from the
rightmost non-transparent pixel column of the bitmap, NOT from the bitmap's
declared width. Our 32x64 part canvases have visible content ending at
different x columns (arm_left at x=7, foot_left/leg_left at x=15,
head/torso at x=23, arm_right at x=31), so Mojang assigns each codepoint a
different advance (8, 16, 24, 32).

BossbarHUD.rebuildTitle assumes every bodyhealth element advances exactly
BodyHealthRenderState.CANVAS_WIDTH_PX = 32 px and emits negative-space
shifts between the eight parts on that assumption. When Mojang's real
advance is smaller, each shift overshoots by (32 - real_advance) per glyph
and the parts scatter across the bossbar title -- the "only one part
visible" bug.

Fix: stamp one near-invisible pixel (alpha = 1) into column x = 31 (the
last column of the 32-wide canvas) of every part PNG. Mojang then computes
advance = 31 + 1 = 32 for every glyph, which is exactly what BossbarHUD
already assumes. The pixel is at 1/255 (~0.4%) opacity -- imperceptible --
and does not change rendering: Mojang draws the full 32-wide cell at the
cursor regardless of content, so only the advance changes.

Idempotent: if column 31 already has a non-transparent pixel, the file is
left byte-identical (not re-encoded), so re-running produces no spurious
git diff.

Run from repo root: python .scripts/stamp_bodyhealth_advance.py
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image

REPO = Path(__file__).resolve().parents[1]
PNG_DIR = REPO / "core" / "src" / "main" / "resources" / "assets" / "harshlands" / "textures" / "bodyhealth"

CANVAS_W = 32     # must match BodyHealthRenderState.CANVAS_WIDTH_PX
CANVAS_H = 64
MARKER_X = 31     # last column -> Mojang advance = MARKER_X + 1 = 32
MARKER_Y = 0      # any row works; the pixel is invisible at alpha=1
MARKER_RGBA = (255, 255, 255, 1)  # alpha=1 -> counted by Mojang, ~0.4% opacity


def column_has_opaque(img: Image.Image, x: int) -> bool:
    px = img.load()
    _, h = img.size
    for y in range(h):
        if px[x, y][3] != 0:
            return True
    return False


def stamp() -> None:
    pngs = sorted(PNG_DIR.glob("bodyhealth_*.png"))
    if len(pngs) != 40:
        raise SystemExit(f"expected 40 bodyhealth PNGs in {PNG_DIR}, found {len(pngs)}")
    stamped, already = 0, 0
    for path in pngs:
        img = Image.open(path).convert("RGBA")
        if img.size != (CANVAS_W, CANVAS_H):
            raise SystemExit(f"{path.name}: expected {CANVAS_W}x{CANVAS_H}, got {img.size[0]}x{img.size[1]}")
        if column_has_opaque(img, MARKER_X):
            already += 1
            continue
        img.putpixel((MARKER_X, MARKER_Y), MARKER_RGBA)
        img.save(path)
        stamped += 1
    print(f"[stamp] {stamped} stamped, {already} already had an x={MARKER_X} marker ({len(pngs)} total)")


if __name__ == "__main__":
    stamp()
