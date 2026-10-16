# World Eater casings

The incomplete World Eater tamer now requires black World Eater Tamer Casing.
Old UHV casing and the three colored variants are not accepted in its frame.
The 5×5×5 edge skeleton and controller position are unchanged; up to five of
each item/fluid import/export part and at most one optional standard GTCEu
parallel hatch may replace casing. No energy hatch is needed or accepted.

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
references its own 32px four-tile CTM sheet; these have the same frame sequence
and duration as the original 16px sprite, so connections do not desynchronize
shimmer or moving shadows, including after resource reload.

Regenerate from the project root:

    python src/gameTest/generate_world_eater_casings.py

Check frame timing, bounds, fixed geometry, connected sheets, full shimmer sweep
and emissive mask separation:

    python -m unittest discover -s src/gameTest -p 'test_*.py'

## Existing worlds

Back up before updating. Replace old UHV frame casings with the new black casing;
no blocks are automatically converted. Keep the existing controller: its identity
and valid DORMANT/ACTIVE progression data are retained. A completed pending survey
report waits for the rebuilt structure and usable output capacity rather than
being re-created. Downgrading a world after placing new casing IDs is not a safe
rollback; restore the pre-update world backup alongside the old mod JAR.
