# Proposal

## Why

The World Eater tamer's controller panel shows several lines of internal state that do not tell the player what to do next, and the tamer, intention hatches, ME Demand Hatch and Creative Demand Pattern Provider carry long hard-coded English tooltips. The ME blocks only accept AE2 cables on their front face, and the Safety Hatch requires a full Assembly Line.

## What Changes

- Below the native GTCEu lines, the formed tamer panel shows exactly one localized hint naming the next step toward real runs (survey, casing upgrade, intention hatch, preparation test, demand delivery, real run). It drops the tier line, the state/scan/retry/parallel line and the current-chunk line.
- Tamer, intention hatch, ME Demand Hatch and Creative Demand Pattern Provider tooltips and panels use lang keys and are trimmed to their essentials.
- The ME Demand Hatch and the Creative Demand Pattern Provider connect to AE2 from every side.
- **BREAKING** The Safety Hatch is crafted in the standard Assembler from eight each of the first eight previously listed LuV machines; the Assembly Line recipe is removed.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `world-eater-tamer`: live scan feedback no longer shows chunk coordinates in the UI; the panel shows one next-step hint.
- `world-eater-demand-runs`: the UI reports demand through the hint; help is trimmed.
- `world-eater-intention-hatches`: Safety moves to the Assembler with eight inputs; help is trimmed.
- `world-eater-me-demand-hatch`: any-side connection; trimmed help and panel.
- `world-eater-creative-demand-provider`: any-side connection.

## Impact

- `IncompleteWorldEaterTamer`, `WorldEaterTamerRecipeLogic`, `IntentionHatchPartMachine`, `MEDemandHatchPartMachine`, `CreativeDemandPatternProviderMachine`, `GregEGTAddon` and `src/generated/resources/assets/gregecore/lang/en_us.json`.
- GameTests asserting the old texts, the Safety recipe and the Assembly Line fixture.
