# World Eater casings

The incomplete World Eater tamer accepts any mixture of black, dark purple,
dark green, and very dark orange World Eater Tamer Casings in its frame.
Old UHV casing and unrelated blocks are not accepted.
The 5×5×5 edge skeleton and controller position are unchanged; up to five of
each item/fluid import/export part and at most one optional standard GTCEu
parallel hatch may replace casing. No energy hatch is needed or accepted.

## Reversible casing tiers

Any actual black frame casing makes the formed machine tier 1. Replacing the
last black casing with any colored variant makes it tier 2; different colors
may be freely mixed. Putting black back downgrades it to tier 1. Only actual
required frame casings count: the controller, permitted parts (even with black
appearances), unrestricted face panels, and interior do not contribute.

Tier is derived from the current validated frame, not a permanent unlock or
saved upgrade. A broken/unvalidated frame has no usable tier; reformation and
reload recompute it. Existing surveys and conversions work identically at both
tiers, with no extra payment, recipes, or tier-based gates.

Keep the existing controller to retain its valid DORMANT/ACTIVE progression
through swaps, upgrades, downgrades, dismantling, repairs, and save/load. Colored
casings do not awaken a DORMANT controller; a fresh controller starts DORMANT.
Only normal successful delivery of the existing qualifying survey activates it.
Retained ACTIVE is **not** recovery of unfinished work: a structural break still
cancels an unfinished paid survey or casing craft. Replace casings while idle.
Completed pending survey reports retain their existing protection and deliver
once when the frame and output capacity are available, without another sensor
or scan. Registry IDs, version-1 progression saves, and acquisition are unchanged.

The controller, formed hatches and buses use black casing at tier 1. At tier 2
they use the most common actual frame casing color, breaking ties in purple,
green, orange order.
Controller, hatch, face-panel, and interior blocks do not vote. Replacing casings
and reloading recompute the appearance. The controller's native `casing_color`
model state selects matching base and emissive textures while preserving its
front/status overlays and connected-texture appearance. Unformed controllers
and inventory items use black; old saves without this property default to black
until formation recomputes the color. Progression and block identity are unchanged.

## Acquisition and recipes

Black casing is currently creative-only (the mod creative tab or `/give`). No
survival recipe produces it. All four variants appear in the creative tab.

After the existing survey successfully delivers a report of at least 500
machines, the tamer becomes ACTIVE and can make these variants without EU:

| Inputs | Output | Duration |
| --- | --- | --- |
| 1 black casing + 16 purple terracotta | 1 dark purple casing | 5 seconds |
| 1 black casing + 16 green terracotta | 1 dark green casing | 5 seconds |
| 1 black casing + 16 orange terracotta | 1 very dark orange casing | 5 seconds |

Only ordinary matching vanilla terracotta is used, not glazed terracotta.
The optional hatch parallelizes these ACTIVE conversions at its current setting,
limited by complete available inputs and usable output capacity. Duration stays
5 seconds. Without a hatch, processing is single-recipe. The DORMANT activation
survey always consumes one sensor, takes its unchanged 120 seconds plus any
chunk-retry delays, and produces one report, even with a parallel hatch installed.
ACTIVE UI progress describes crafting rather than a scan.

## Artwork and optional bloom

The 16×16 textures are original, individually palette/pixel-authored raster art
under the project's MIT license. GTCEu 7.5.2's Industrial Steam Casing is a visual
reference for its framed industrial plate and four corner bolts; no upstream
PNG pixels are copied. The editable source is
`src/gameTest/generate_world_eater_casings.py` (requires Pillow).

Adjacent casings of the same color use native LDLib connected textures like
Industrial Steam Casing: internal frame seams disappear, while outside borders
and concave corners remain. Different colors retain their boundary. The
controller joins surrounding matching casing; formed buses/hatches join casing
matching their current tier-derived appearance.

All four variants are static: no sweeping highlight, moving shadows, or texture
animation metadata. Base plates stay normally shaded. A separate, thin emissive
outline follows only the outside edges of connected panels, without glowing
internal seams. The original 16px sprites and their 32px CTM sheets each contain
one frame.

The outline uses the same optional Shimmer bloom metadata as native GTCEu coils,
on both standalone and connected sprites. With Shimmer installed and bloom enabled,
its post-processing adds a steady glow; without Shimmer, the same detail remains
fullbright with no bloom halo. This does not emit world light or add a required
dependency. There is no timed pulse or custom per-frame renderer.

Half-connected submaps extend only the surviving straight edge, without copying
standalone corner caps or bolts onto every block boundary. Connected shadows use
a 16px repeating field so their diagonals align across adjacent blocks. The four
concave submaps remain populated for the pinned LDLib's diagonal-aware selection.

Regenerate from the project root:

    python src/gameTest/generate_world_eater_casings.py

Check single-frame assets, absent animation, continuous connected perimeters,
concave corners, seamless static shadow fields, optional bloom metadata,
connected glow outlines and emissive mask separation:

    python -m unittest discover -s src/gameTest -p 'test_*.py'

## Existing worlds

Back up before updating. Replace old UHV frame casings with accepted tamer casing;
no blocks are automatically converted. Keep the existing controller: its identity
and valid DORMANT/ACTIVE progression data are retained. A completed pending survey
report waits for the rebuilt structure and usable output capacity rather than
being re-created. Rolling back only casing-tier support requires replacing colored
frame casings with black so the older build can form them. Downgrading past the
release that registered these casing IDs is not safe; restore the pre-update
world backup alongside that older mod JAR.
