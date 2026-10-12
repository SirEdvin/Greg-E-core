# World Eater tamer verification

Verified October 5, 2026 against the unchanged Minecraft 1.20.1 / Forge 47.4.10 / Java 17 / GTCEu 7.5.2 / LDLib 1.0.40.b dependencies.

## Executed checks

- `bash ./gradlew --no-daemon compileJava build runGameTestServer` passed in a fresh disposable GameTest world. Previous disposable worlds were archived into the profile scratch directory, not deleted.
- The fresh JUnit-like XML contains exactly 14 named tests and zero failures. The server log independently reports all 14 required tests passed.
- `runData` was executed successfully earlier in implementation. Generated controller blockstate, block/item models and translations were inspected; unrelated translation/cache churn was restored.
- The addon-owned recipe is emitted through `GregEGTAddon.addRecipes` and verified in the native recipe manager at `gregecore:world_eater_tamer/information_collection`. Its recipe type follows the project's existing registration convention, `gtceu:world_eater_tamer`. A native recipe search, not only direct fixture setup, verifies power-free startup, exactly one IV sensor consumed, and unchanged 2400-tick duration. No standalone recipe JSON is packaged: the compiled addon provider emits the runtime recipe.
- A fresh native client run under private Xvfb passed with `passed=true`, `pagesChecked=103`, `nativeUI=true`, and an empty failure list. It uses test-only code and an explicitly opted-in, prefix-guarded isolated world copy. Checks cover native book layout, resolved models, server/client DORMANT and ACTIVE state, and retry feedback.
- The built production JAR was inspected and contains the four feature implementations but no `/gametest/` classes or structure-generator script.
- `git diff --check` and OpenSpec validation pass. No dependency upgrades, modifications to existing machine implementations, larger variant, acquisition recipe or ACTIVE-state recipe were introduced.

## Acceptance-to-evidence mapping

| Tasks | Native evidence |
| --- | --- |
| 1.1, 1.2 | `registration`: exact registries, item/fluid/EU IO, name, initial state; `rotated_structure_and_creative_tab`: creative registration |
| 1.3 | `structure_limits`: arbitrary filled interior, five of every permitted part type, rejection of each sixth part and of energy hatches; rotated fixture |
| 2.1 | `registration` plus `loaded_thresholds_and_blocked_delivery`: both state round trips, defaults and ACTIVE preservation through invalidation/reformation |
| 2.2 | `metadata_gates_and_cancellation`, `resumed_metadata_fails_closed`, ACTIVE new/cached searches in threshold fixture |
| 2.3 | `registration` inspects emitted recipe; `metadata_gates_and_cancellation` starts it through native search without energy and checks one sensor and 2400 ticks |
| 2.4 | malformed persisted metadata in `registration`; native NBT round trips in lifecycle fixtures; real controller eviction/reload; mutation paths inspected for dirty marking |
| 3.1 | `deterministic_coverage`: contiguous long-arithmetic sections, negative origin, multiple valid heights, final inclusion and ceiling budget |
| 3.2 | `eligibility_and_boundary_coverage` and `structure_limits`: identity grouping, ordinary/non-GregTech exclusion, standalone/other controllers, own/other parts and outside controller with an inside part |
| 3.3 | boundary/final-position fixture; disabled/own-part-unavailable fixture; loaded threshold scans; production reads inspected to use non-loading chunk cache then loaded chunk references, with no final catch-up |
| 4.1 | `recovery_on_every_allowed_attempt`: attempts 1 through 5, exact delays, unchanged pending section, 2400 scanning ticks plus only elapsed delays; retry exhaustion fixture forbids a sixth retry |
| 4.2 | `retry_exhaustion_and_native_persistence`, `failure_after_high_partial_count`: failure on tick 101 for an initially absent chunk, early termination, coordinate report, no activation with 501 partial machines |
| 4.3 | `native_controller_eviction_and_reload`: actual unload events, absent chunks, new controller instances, persisted counts/progress/delay/allowance/book, no second sensor; stages cover scanning, delay, retry boundary and blocked report. Cancellation and part unavailability are separately exercised |
| 5.1 | `report_serialization`: vanilla validation, exact retained long-name entries, totals, block coordinates, native item packet round trip below its NBT limit; client checks 103 readable pages including zero, failure and long-name reports |
| 5.2 | `loaded_thresholds_and_blocked_delivery`: all output slots blocked, exact pending output round trip, no restart, one book delivered, shared recipe unchanged, fresh consecutive run; real eviction preserves pending report; mandatory no-void policy and simulated/checked real IO inspected |
| 5.3 | native totals 0, 499, 500 and 501, blocked activation at 500, consecutive below-threshold runs, high-partial-count failure |
| 5.4 | native integrated-client UI checks and screenshots, recipe metadata assertions, generated model inspection; production imports contain no client-only execution dependency |
| 6.1, 6.2 | discovered Forge tasks, configured dedicated GameTest source set, fresh 14-name XML/server log, compile/build, prior datagen, native client result, production JAR inspection |
| 6.3 | dense native-machine fixture measurements below, target unload fixture, final-tick and per-attempt recovery/exhaustion assertions |
| 6.4 | final production diff and new feature files reviewed against proposal/design/spec; only additive registrations, resources, feature implementations and opt-in verification infrastructure |

## Dense-scan and eviction measurements

The normal 384-block-height scan has a ceiling of 3318 positions per scanning batch. Real LV macerators are packed into the first batch, not spread thinly across the square. The final server run measured the maximum elapsed `serverTick()` time over each complete fixture run:

| Eligible machines | Worst observed callback, nanoseconds |
| --- | ---: |
| 0 | 1623540 |
| 499 | 919260 |
| 500 | 591767 |
| 501 | 566579 |

These are observations from this host, not a guaranteed latency ceiling. The preceding approved dense run also passed and observed a maximum of 2023892 nanoseconds. Measured callbacks include completion/output attempts where applicable; fixture placement/bootstrap is outside the measured callbacks.

The final run observed controller eviction across all four persisted lifecycle stages after 108 waiting ticks. Its target-chunk test observed an actual Forge unload event and absent non-loading cache result after 299 waiting ticks; retries completed while the target remained absent and had no forced ticket. Test setup itself loads/forces chunks deliberately, then releases those tickets before checking native eviction. Production scanning never creates those fixture tickets.

## Evidence locations

Current execution evidence is outside the repository at `/home/siredvin/.hermes/profiles/albina/cache/scratch/`:

- `world-eater-verification.log`
- `world-eater-gametest.xml`
- `world-eater-datagen.log`
- `world-eater-client.log`
- `world-eater-client/result.json`
- `world-eater-client/screenshots/` (native books, controller and UI)

Scratch evidence may be pruned; this checked-in-ready document retains the acceptance mapping and measured results. The deliverable is `/home/siredvin/projects/Greg-E-core/build/libs/gregecore-1.12.7-1.20.1.jar`.

No remaining execution blocker was encountered for the original implementation. It was subsequently committed to `feat/add-incomplete-world-eater-tamer`.

## Skeleton and live-feedback revision — October 11, 2026

- The edge-only fixture was executed against the old shell pattern first: `structure_limits` rejected it as expected. The original shell-dependent fixtures failed too; no false-green exit status was accepted.
- `bash gradlew build runGameTestServer --no-daemon` passed against the revised pattern. Fresh XML contains exactly 15 named tests and zero failures; the server independently reports all 15 required tests passed. This physical-server run verifies the particle registration does not load client-only providers.
- `structure_limits` now verifies a 44-block minimum skeleton (43 casing/part positions plus the controller), arbitrary filled face panels/interior, empty faces/interior, rejection of every missing frame edge/corner, all independent hatch limits and energy-hatch rejection. Rotated formation also verifies the geometric center.
- New `current_chunk_tracks_scan_and_retry` exercises every scan tick, negative chunk coordinates, the unavailable chunk during retry, suspension, active/retry native NBT reload, and clearing at completed-but-blocked report delivery.
- Native integrated-client verification passed in an isolated `albina-world-eater-verification-*` world copy with `passed=true`, `nativeUI=true`, `pagesChecked=103`, and an empty failure list. The UI screenshot visibly contains the localized current chunk X/Z label alongside retry status.
- A real server scan containing one standalone macerator, an excluded hatch and an ordinary chest emitted exactly one observed native particle packet. Source `(8186.5, 83.5, 8192.5)` and destination `(8192.5, 82.5, 8194.5)` match the found block and the geometric center, not the controller. The unmodified production particle provider passed source, 20-tick midpoint, 40-tick endpoint, expiry, long-displacement and zero-displacement checks.
- Native rendered-frame capture visibly shows the purple particle moving toward the skeleton. Capturing from RenderTick END avoids stale framebuffers during client tick catch-up. The effect uses the vanilla `generic_5` sprite with a purple full-bright tint.
- The rebuilt production JAR contains the registered particle, client-only provider and matching particle JSON, and excludes all GameTest code. Dependency versions remain unchanged.
- Evidence is in profile scratch: `world-eater-update-red.xml`, `world-eater-update-green.xml`, `world-eater-update-green.log`, `world-eater-update-client.log`, and `world-eater-client/result.json` plus `screenshots/ui-2.png` and `screenshots/scan-particle.png`.

Previously built front-face-center controllers must be moved to the middle of the lower front edge. Face panels are optional; no automatic world-block relocation is performed.
