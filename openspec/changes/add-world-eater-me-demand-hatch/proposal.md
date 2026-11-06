# Proposal

## Why

Real World Eater runs already produce Tamed world eater essense, but each randomized demand still needs external or manual delivery. An essence-funded ME Demand Hatch makes those runs automatable without continuously stocking every possible demand or repeatedly submitting the same AE2 craft.

## What Changes

- Add an optional **ME Demand Hatch**, connected to AE2, which mirrors only the attached tamer's current paid real-run demand and exposes matching network stock as item input.
- Allow zero or one hatch anywhere an ordinary item/fluid I/O part may occupy the frame; a second prevents formation. Preserve existing part limits, casing tiers, intention placement, and power-free tamer processing.
- At demand creation, consider autocrafting once: use available supply first, skip scheduling if a matching craft is already scheduled on the connected network, otherwise attempt one craft for the missing quantity. Do not retry later, including after reconnect or reload.
- Submit successful jobs into normal ME storage and forget them. Do not own, monitor, cancel, or resubmit fired jobs. Continue checking storage for demand payment; clear the request on payment or run termination. Late products remain network inventory.
- Add a shapeless crafting recipe consuming one GTCEu ME Stocking Input Bus and **four** Tamed world eater essense ingots to produce one hatch.
- Preserve manual delivery, exact full-demand payment, existing deadlines, rewards, progression, and timeout consequences. Add placement, recipe, presentation, AE2 compatibility, and lifecycle regressions.

## Capabilities

### New Capabilities

- `world-eater-me-demand-hatch`: Optional AE2-connected demand input, four-ingot acquisition, one-shot duplicate-aware autocrafting, fire-and-forget job ownership, and understandable player presentation.

### Modified Capabilities

- `world-eater-tamer`: Permit one dedicated demand hatch at ordinary frame I/O positions without weakening existing structure or part-limit rules.
- `world-eater-demand-runs`: Integrate demand-driven ME supply with exact payment and persist the single scheduling decision without changing run gates or consequences.

## Impact

- Registration and resources: a new endgame part, `GregEStuffInit`, `ModCreativeModTabs`, `GregEGTAddon`, hatch model/overlay/localization, and generated machine resources as required by existing registration.
- Controller and lifecycle: `IncompleteWorldEaterTamer` frame predicates and `WorldEaterTamerRecipeLogic` paid startup, payment, cleanup, and persistence.
- Dependencies: add the version-matched AE2 API/development runtime to the existing Forge 1.20.1 / Java 17 / GTCEu 7.5.2 build. Keep AE2 optional at runtime and gate only the new integration when absent; do not upgrade unrelated dependencies.
- Verification: extend the existing `src/gameTest` structure, demand, unload, material/recipe, and client fixtures with real AE2 integration coverage and a no-AE2 loading check.
- Existing worlds require no frame migration and old paid runs must not gain a retrospective autocrafting attempt. Implementation and verification are recorded in `tasks.md` and `verification.md`; canonical specs remain unchanged until explicit synchronization or archival.
