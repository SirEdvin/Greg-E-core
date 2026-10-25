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
or scan. Registry IDs, version-1 progression saves, acquisition, and appearance
are unchanged by casing tiers.

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

## Artwork and animation

The 16×16 textures are original, individually palette/pixel-authored raster art
under the project's MIT license. GTCEu 7.5.2's Industrial Steam Casing is a visual
reference for its framed industrial plate and four corner bolts; no upstream
PNG pixels are copied. The editable source is
`src/gameTest/generate_world_eater_casings.py` (requires Pillow).

Adjacent casings of the same color use native LDLib connected textures like
Industrial Steam Casing: internal frame seams disappear, while outside borders
and concave corners remain. Different colors retain their boundary. The black
controller and formed buses/hatch join the surrounding black casing as well.

Base plates stay normally shaded; a separate transparent emissive highlight
sweeps all the way across them to the far corner before disappearing, rather
than resetting at the center. Black repeats every 200 texture ticks (10 seconds), colored
casings every 40 (2 seconds), at the normal tick rate. Colored interior shadow
pixels shift by one pixel across the loop, while borders/bolts stay fixed.
This is texture animation and fullbright surface detail, not world-light emission
or a requirement for optional shaders. Compact 8-frame PNG strips and explicit
frame durations live under `textures/block/casings/world_eater/`. Each layer
references its own 32px CTM sheet containing sixteen 8px corner submaps; these have the same frame sequence
and duration as the original 16px sprite, so connections do not desynchronize
shimmer or moving shadows, including after resource reload.

Half-connected submaps extend only the surviving straight edge, without copying
standalone corner caps or bolts onto every block boundary. Connected shadows use
a 16px repeating field so their diagonals align across adjacent blocks. The four
concave submaps remain populated for the pinned LDLib's diagonal-aware selection.

Regenerate from the project root:

    python src/gameTest/generate_world_eater_casings.py

Check frame timing, bounds, fixed geometry, continuous connected perimeters,
concave corners, seamless shadow fields, connected sheets, full shimmer sweep
and emissive mask separation:

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
