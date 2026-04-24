# preview_text font fallback

Harshlands 1.4+ supports non-ASCII translation labels (Chinese, Cyrillic,
Greek, Arabic, etc.) for the Protein/Carbs/Fat bossbar preview. To render
these correctly with the above-action-bar ascent, the resource pack's
`assets/harshlands/font/preview_text.json` needs a unifont fallback
provider appended to its `providers` array.

## Preferred: reference provider

```json
{
  "type": "reference",
  "id": "minecraft:include/unifont"
}
```

Test it: if the preview text still sits above the action bar for CJK
labels, you're done. If it drops to the default font baseline, the
ascent shader shift isn't propagating through the reference — use the
fallback below.

## Fallback: explicit unihex provider with matching ascent

Use the same `ascent` / `height` you set on the existing ASCII provider
of `preview_text.json`:

```json
{
  "type": "unihex",
  "hex_file": "minecraft:font/unifont_all_no_pua.zip",
  "size_overrides": [],
  "ascent": <same-as-ASCII-provider>,
  "height": <same-as-ASCII-provider>
}
```

## Last resort

If neither provider preserves the ascent shift, the plugin still
renders CJK labels correctly - they fall back to the vanilla default
font at the normal bossbar baseline (lower than the custom ascent).
No RP change strictly required; the visual result just differs.

## Advance width

The plugin measures CJK / non-Latin BMP glyphs as 12 px wide (8 px
glyph + 4 px trailing space), matching Minecraft's unifont. If your RP
substitutes a different glyph width for these codepoints, file an issue
- `NutritionPreviewLayout.measureTextAdvance` will need a matching
constant.
