"""
Regenerate font/bodyhealth.json for the BetterHud-mirror layout.

All 40 providers share ascent=-14000 and height=64. Each PNG is a full
32×64 canvas with its body-part pixels positioned naturally inside;
positioning is intrinsic to the bitmap.

Run from repo root: python .scripts/regen_bodyhealth_font.py
"""
from __future__ import annotations

from pathlib import Path

REPO = Path(__file__).resolve().parents[1]
SRC_FONT_FILE = REPO / "core" / "src" / "main" / "resources" / "assets" / "harshlands" / "font" / "bodyhealth.json"

STATES = ["full", "nearly_full", "intermediate", "damaged", "broken"]
# Iteration order must match BodyPart.values() so codepoints line up.
PART_ORDER = ["head", "torso", "arm_left", "arm_right", "leg_left", "leg_right", "foot_left", "foot_right"]

ASCENT = -14000  # Bucket E (single)
HEIGHT = 64      # full canvas height


def regenerate_font() -> None:
    base_cp = 0xE000  # HEAD/FULL
    providers = []
    for part_idx, part in enumerate(PART_ORDER):
        for state_idx, state in enumerate(STATES):
            cp = base_cp + part_idx * len(STATES) + state_idx
            providers.append({
                "file": f"harshlands:bodyhealth/bodyhealth_{state}_{part}.png",
                "cp_lit": f"\\u{cp:04X}",
            })

    out_lines = ['{ "providers": [']
    for i, p in enumerate(providers):
        block = (
            "    {\n"
            f"      \"type\": \"bitmap\",\n"
            f"      \"file\": \"{p['file']}\",\n"
            f"      \"ascent\": {ASCENT},\n"
            f"      \"height\": {HEIGHT},\n"
            f"      \"chars\": [\"{p['cp_lit']}\"]\n"
            "    }"
        )
        out_lines.append(block)
        if i < len(providers) - 1:
            out_lines.append(",")
    out_lines.append("]}\n")
    SRC_FONT_FILE.write_text("\n".join(out_lines), encoding="utf-8")
    print(f"[font] wrote {SRC_FONT_FILE} with {len(providers)} providers")


if __name__ == "__main__":
    regenerate_font()
