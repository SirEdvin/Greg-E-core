# Tasks

## 1. Registration and structure

- [x] 1.1 Add the `world_eater_tamer` recipe type to `src/main/java/net/cu5tmtp/GregECore/gregstuff/GregUtils/notCoreStuff/GregERecipeTypes.java`, with state/operation data display and no EU capability; verify its exact registry identity and item/fluid IO sizes in a Forge runtime registry assertion and compile against GTCEu 7.5.2.
- [x] 1.2 Add the proposed non-electric `IncompleteWorldEaterTamer` class in `gregstuff/GregMachines/machines/endgame/` and initialize it in `GregEStuffInit.initGregEMulti()`; verify controller lookup succeeds, its display name is `Incomplete World Eater tamer`, and it is accessible through the intended creative registration.
- [x] 1.3 Implement the five-aisle 5×5×5 pattern using UHV machine casing, a front-center controller, unrestricted interior, and separate maximum-five predicates for the four item/fluid part types without energy/maintenance/parallel abilities; verify formation with filled interior and five of each allowed type, rejection of a sixth of each type, energy-hatch rejection, and rotated formation.

## 2. Persistent state and recipe setup

- [x] 2.1 Add the proposed `WorldEaterTamerRecipeLogic` in `gregstuff/GregRecipeLogic/`, created by the controller, with DORMANT/ACTIVE progression state and inherited custom persistence hooks; verify default DORMANT, both states' save/load round trips, and preservation of ACTIVE across structure invalidation/reformation.
- [x] 2.2 Gate new, cached, and resumed recipes on explicit valid `required_state` data and the supported operation; verify mismatched/missing/unknown state prevents input consumption and an ACTIVE tamer cannot restart information collection through the cached-recipe path.
- [x] 2.3 Populate `GregEGTAddon.addRecipes` with the initial addon-owned information collection recipe using the pinned artifact's verified IV sensor item, one consumed sensor, 2400 base ticks, DORMANT metadata, a representative written-book output, and no power/parallel/overclock behavior; verify emitted recipe data, one-sensor consumption, and startup without an energy hatch in a Forge fixture.
- [x] 2.4 Persist the fixed scan domain, identity-keyed counts, elapsed retry ticks, pending retry/delay/attempt data, and completed pending report; verify NBT round trips including missing defaults and malformed metadata cannot activate or resume inconsistent counts, and all run mutations mark persistence dirty.

## 3. Bounded scanning and eligibility

- [x] 3.1 Implement long-arithmetic section ranges derived from inherited progress minus elapsed retry ticks over the fixed 2400 scanning ticks; verify deterministic contiguous coverage, no overlaps/gaps, last-position inclusion on tick 2400, negative chunk origins, arbitrary valid dimension build heights, and a per-tick range no larger than the computed ceiling budget.
- [x] 3.2 Add one interface-eligibility method that counts standalone GregTech machines and other multiblock controllers while excluding ordinary/non-GregTech blocks, all multiblock parts, the current controller, and its attached parts; verify mixed-machine fixtures and stable machine-definition grouping, including a multiblock whose controller lies outside the square but a part lies inside it.
- [x] 3.3 Connect bounded scan sections to enabled recipe ticks, preflight batch chunks via non-loading server chunk-cache access, and read machines through loaded chunk references; verify full-height 9×9 coverage, boundary exclusions, no work during suspension/own-part unavailability, no world-level loading reads, and no final bulk catch-up pass.

## 4. Retry timing and reload lifecycle

- [x] 4.1 Implement missing-chunk handling with five additional attempts, one persisted 20-tick duration extension and non-scanning delay per retry, counting the detection tick as the first delay tick; verify recovery after each allowed attempt, exactly 20 skipped scanning ticks per extension, unchanged pending section, and no sixth retry.
- [x] 4.2 Implement early retry-exhaustion completion with the failed chunk's block-space X/Z area and DORMANT outcome; verify a failure completes before the nominal remaining run time, does not force-load the chunk, and cannot activate even after a partial count of at least 500.
- [x] 4.3 Preserve scan progress/counts/delays on controller and part unload; clear unfinished run data on true structure cancellation without clearing progression state or completed pending reports; verify real save/unload/reload during scanning, during a retry delay, at the retry boundary, and during output blockage without another sensor or repeated/skipped section.

## 5. Report delivery and player feedback

- [x] 5.1 Build vanilla written-book reports with valid title/author/pages, escaped components, deterministic `<machine name> - <count>` entries, totals, and explicit failure status/block coordinates; verify readable zero-count, multi-page, long-name, and failure books in the client and validate vanilla serialization/payload bounds without silently dropping entries.
- [x] 5.2 Integrate finalized instance-owned report stacks into copied run recipe outputs using simulated and checked real item IO, disabling report voiding and persisting blocked delivery; verify exactly one output, no scan/input restart while blocked, delivery after reload, and no mutation of shared recipe definitions or reuse of the previous report in a new run.
- [x] 5.3 Commit the DORMANT-to-ACTIVE transition only after successful delivery for complete totals of at least 500 and reset per-run data before any new search; verify totals 0, 499, 500, and 501, failure after a high partial count, blocked delivery at 500, and fresh below-threshold consecutive runs.
- [x] 5.4 Add native state/progress/retry feedback and required-state recipe information plus controller name/model resources using existing registration conventions; verify DORMANT and ACTIVE remain visibly distinct from upstream working status, retry feedback updates, native UI still works, and generated model references resolve without introducing client-only classes into server logic.

## 6. End-to-end verification

- [x] 6.1 Add focused production-boundary tests and Forge GameTest fixtures using the existing configured run support, discovering the exact runnable task names with `./gradlew --no-daemon tasks --all`; verify assertions cover the specified structure, scheduling, eligibility, retries, persistence, output blockage, and state-transition scenarios, and that world/bootstrap-dependent checks run in Forge rather than a plain JVM.
- [x] 6.2 Run `./gradlew --no-daemon compileJava`, the discovered tests/GameTest run, and the configured data-generation/build tasks; verify fresh named test results rather than exit status alone, inspect generated recipe/model/name assets, and document any actual dedicated-server or client validation blocker without claiming unexecuted checks passed.
- [x] 6.3 Exercise a dense loaded scan in a disposable world and record worst batch size and tick-time measurements, then let a target chunk actually unload and verify scans/retries neither create tickets nor prevent eviction; verify successful scanning still ends on its final 2400th scanning tick, recovery extends elapsed run ticks only by its delays, and exhaustion emits the failure report without waiting out the base recipe remainder.
- [x] 6.4 Review the final feature diff against the proposal and delta specification; verify unrelated machines/dependencies remain unchanged, the 15×15×15 version and unrequested acquisition/ACTIVE recipes are absent, test-only artifacts are excluded from production packaging, and all implemented behaviors have recorded evidence.
