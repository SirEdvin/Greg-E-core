# Proposal

## Why

Future World Eater tamer recipes need an explicit player choice between safety and bravery when selecting failure consequences. Introduce the craftable intention hatches and their structural selection now, without adding those recipes or implementing any failure effects.

## What Changes

- Add distinct Safety Hatch and Bravery Hatch multiblock parts, with obtainable item forms, readable presentation, and creative inventory entries.
- Permit zero or one intention hatch at the center of the tamer's lower rear edge, directly opposite the controller at the same Y level. Retain all existing alternatives at that position when no intention hatch is installed.
- Derive the formed tamer's intention from the installed variant; absence means no declared intention, not an implicit Safety or Bravery selection. No existing recipe changes behavior.
- Safety has one standard Assembly Line recipe producing one hatch in 60 seconds at LuV recipe power, with no fluids or research requirement.
- Safety consumes eight each of a fixed, selected set of sixteen LuV single-block processing machines. The capability spec lists the exact machine IDs; this is not an automatically expanding registry scan.
- Bravery has one power-free World Eater tamer recipe, available only in ACTIVE state at casing tier 2. It consumes one signed written book whose first-page text is exactly `I know what I am doing` and produces one hatch in 60 seconds, with no fluids or research requirement. Titles and authors are unrestricted and copies are accepted.
- Preserve casing tiers, DORMANT/ACTIVE progression, existing item/fluid and parallel-hatch limits, and the tamer's power-free processing.

## Capabilities

### New Capabilities

- `world-eater-intention-hatches`: Safety/Bravery part identity, presentation, machine-specific acquisition, and content-sensitive book matching.

### Modified Capabilities

- `world-eater-tamer`: Optional fixed-position intention part, live selection lifecycle, ACTIVE tier-2 Bravery crafting, and compatibility with existing structures and processing.

## Impact

- Machine parts and initialization under `gregstuff/GregMachines/parts/endgame/` and `GregEStuffInit`, plus creative inventory entries in `ModCreativeModTabs`.
- The structure predicate and derived formed-state accessor in `IncompleteWorldEaterTamer`, plus state/tier-gated ordinary crafting in `WorldEaterTamerRecipeLogic`; no new persisted progression or failure state.
- Recipe registration through `GregEGTAddon.addRecipes`, a narrowly scoped serializable book ingredient, and its registration at the existing Forge lifecycle boundary.
- Machine names, models/overlays, tooltips, generated assets, native structure preview and JEI recipe presentation.
- Existing World Eater GameTests and client fixtures, plus native Safety Assembly Line, Bravery tamer, and ingredient serialization regression coverage.
- Uses the pinned Minecraft 1.20.1 / Forge / GTCEu 7.5.2 dependencies. No dependency upgrades, new libraries, mixins, or global Assembly Line changes are planned.

## Non-goals

- Tamer recipes beyond Bravery acquisition, failure probabilities, consequence definitions, explosions, damage, protection guarantees, or a default consequence policy when no hatch is present.
- Hatch inventories, configurable modes, new screens, additional tamer tiers or sizes, and changes to existing recipe interruption/recovery behavior.
