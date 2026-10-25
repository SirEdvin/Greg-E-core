# Proposal

## Why

Greg-E-core needs the first World Eater tamer progression machine: a persistent, state-gated multiblock that collects information about surrounding GregTech infrastructure. Its initial implementation should establish the mechanics without committing to the future larger structure or overwhelming the server tick thread.

## What Changes

- Introduce the **Incomplete World Eater tamer**, a 5×5×5 edge-only skeleton cube with unrestricted face panels and interior blocks, temporary high-progression casing, and up to five each of item input buses, item output buses, fluid input hatches, and fluid output hatches; exclude energy hatches. The controller occupies the middle of the lower front edge.
- Add persistent `DORMANT` and `ACTIVE` states owned by a dedicated recipe-logic subclass, initially `DORMANT`.
- Introduce recipe type `world_eater_tamer`; every recipe explicitly declares its required machine state.
- Add DORMANT-only information collection: consume one IV sensor, require no power, and spread a full-height scan of the controller-centered 9×9 chunk square over exactly 120 seconds of scanning ticks.
- Count standalone GregTech machines and other multiblock controllers, never multiblock parts or the tamer itself; isolate eligibility in an extensible interface-counting predicate.
- Show the currently scanned chunk's X/Z coordinates in the native machine UI, including during retries and suspension. Send one purple traveling particle from each counted machine to the geometric center of the skeleton cube.
- Resume scans from persistent recipe progress with persistent accumulated results and retry bookkeeping. Never force-load chunks.
- For an unavailable chunk, extend the run by one second without scanning, then retry the same work, up to five retries. Exhaustion ends the run early with a written failure book identifying the failed area in ordinary block coordinates, keeping the machine DORMANT.
- On successful completion, output a vanilla written book with `<machine name> - <count>` lines. A total of **500 or more** changes the machine to ACTIVE; smaller totals leave it DORMANT.
- Keep the future 15×15×15 variant out of scope while keeping state and scan mechanics independent of the initial structure size.

## Capabilities

### New Capabilities

- `world-eater-tamer`: Initial multiblock structure, persistent state-gated recipes, deterministic incremental infrastructure scanning, bounded chunk retries, and report-book completion.

### Modified Capabilities

None. The project's existing OpenSpec capability inventory is empty.

## Impact

- New machine registration in `gregstuff/GregMachines/machines/endgame/`, wired through `GregEStuffInit.initGregEMulti()`.
- New recipe logic under `gregstuff/GregRecipeLogic/`, using the existing recipe-logic persistence hooks without changing `MultiThreadedRecipeLogic`.
- Recipe type registration in `GregERecipeTypes` and the initial recipe through the presently empty `GregEGTAddon.addRecipes` hook.
- Machine model/name, player-facing state/scan information, and recipe resources following existing registration and datagen conventions.
- Focused scheduling/persistence tests plus Forge runtime fixtures for formation, counting, retries, reports, and non-loading reads.
- Existing Minecraft 1.20.1, Forge 47.4.10, Java 17, GTCEu 7.5.2, and LDLib 1.0.40.b dependencies remain unchanged. No global tick scanner, forced chunk tickets, mixins, or external worker threads are introduced.
