# Proposal

## Why

Testing the ME Demand Hatch's autocrafting path in a creative world currently requires hand-encoding a processing pattern for whichever randomized demand appears, then running a real processing chain. A creative-only provider that already knows a pattern for every possible World Eater demand makes the full demand-to-payment loop testable in seconds.

## What Changes

- Add a **Creative Demand Pattern Provider** (`gregecore:creative_demand_pattern_provider`), an AE2-connected machine block available only from the creative inventory, with no crafting recipe.
- When connected to an ME network, it exposes one native AE2 processing pattern for every item in the World Eater real-run demand pool: input one Iron Ingot, output one of the demanded item.
- It accepts every pushed pattern (never busy), voids the supplied iron, and inserts the pattern output into the connected ME storage 40 ticks (2 seconds) after the push. Pending deliveries survive unload and reload.
- No changes to the ME Demand Hatch, tamer, demand pool, deadlines, or existing fixtures; the hatch reaches the provider through ordinary AE2 autocrafting.

## Capabilities

### New Capabilities

- `world-eater-creative-demand-provider`: Creative-only AE2 pattern provider covering the full World Eater demand pool with one-iron patterns and delayed delivery into ME storage.

### Modified Capabilities

None. The ME Demand Hatch already autocrafts through any pattern provider on its network; its requirements do not change.

## Impact

- New AE2-guarded endgame machine under `gregstuff/GregMachines/parts/endgame/`, registered through `GregEStuffInit` and listed in `ModCreativeModTabs` only when AE2 is loaded. No recipe in `GregEGTAddon`.
- Reuses GTCEu's `MEBusPartMachine` grid-node lifecycle and AE2's native processing-pattern encoding/decoding, so reloaded crafting CPUs keep resolving the patterns without a custom decoder.
- Reads `WorldEaterDemands.pool()`; no changes to its contents.
- Model, overlay and localization resources for the new machine; generated machine resources as required by existing registration.
- Verification: new GameTests in `src/gameTest` using the existing real-AE2 network fixture; the build without AE2 must still load.
