# Design

## Context

See `proposal.md` for motivation and `specs/world-eater-tamer/spec.md` for the behavior contract.

Observed local integration points:

- `build.gradle:159-173` and `gradle.properties:10-38` pin Minecraft 1.20.1 / Forge 47.4.10 / Java 17 / GTCEu 7.5.2 / LDLib 1.0.40.b.
- Machines are registered in their own classes, then initialized through `GregEStuffInit.initGregEMulti()` (`GregEStuffInit.java:37-70`); `GregECore.java:81-90` wires the recipe-type and machine registration events.
- `GregERecipeTypes.java` centralizes types and already uses recipe data for machine-specific metadata. Its current registrations use `GTRecipeTypes.register`; the implementation must verify the resulting registry namespace rather than silently imposing an unrelated convention.
- `GregEGTAddon.addRecipes` (`GregEGTAddon.java:35-36`) is currently empty and is the available compiled-addon recipe provider hook. No controller acquisition recipe was requested.
- `MultiThreadedRecipeLogic.java:186-230` demonstrates the existing custom recipe-logic persistence hooks. Reuse that persistence convention, not its parallel execution logic.
- `PressureChamber.java:71-78` demonstrates managed-field inheritance; `PrecisionFabricationArray.java:106-154` demonstrates registration, casing, predicates, and part limits.
- No test classes were found under `src`; `build.gradle:89-104` already declares Forge client/server/GameTestServer runs. Add focused fixtures rather than assuming an existing test suite or Spotless task.
- Inspection of the locally cached, mapped GTCEu 7.5.2 artifact confirms `WorkableMultiblockMachine.createRecipeLogic`, the protected recipe progress/duration fields, recipe custom persistence methods, `MetaMachine.getMachine(BlockGetter, BlockPos)`, controller parts access, `GTRecipe.data`, and `GTBlocks.MACHINE_CASING_UHV`.

Pinned upstream lifecycle review: GTCEu 7.5.2 persists recipe progress, duration, and current recipes. Its default finish path emits output and immediately considers restarting; it does not branch on output success. Working-condition waits can regress progress. The new logic must protect its scan cursor, explicitly gate output delivery and restart, and keep progression state separate from upstream `isActive`. Source reviewed: `api/machine/trait/RecipeLogic.java` at tag `v7.5.2-1.20.1`. Controller lifecycle source at that tag distinguishes structure invalidation, which resets logic, from part unload, which preserves it.

## Goals / Non-Goals

**Goals:**

- Keep authoritative ticking, state, counts, retries, and completion inside one dedicated recipe-logic trait.
- Define scan position from persisted recipe progress, not a second independently advancing cursor or repeated full-world enumeration.
- Bound world work per scanning tick and preserve exact coverage through save/load and one-second delays.
- Use existing registration, item IO, and persistence mechanisms; isolate interface eligibility as one small policy boundary.
- Deliver books once before transitioning state or starting another run.

**Non-Goals:**

- Implementing the 15×15×15 version, new ACTIVE-state recipes, a general state-machine framework, new casing assets, custom renderer animations, or controller crafting progression.
- Modifying existing machines or threaded recipe logic, upgrading dependencies, introducing a global scan service, using worker threads for world access, or force-loading scan chunks.
- Producing a frozen snapshot of the whole area: the report reflects each block when its scheduled section is visited.

## Decisions

### 1. Non-electric controller with temporary native casing

Add `IncompleteWorldEaterTamer` under `gregstuff/GregMachines/machines/endgame/`, extending the non-electric `WorkableMultiblockMachine`, and override `createRecipeLogic` to create `WorldEaterTamerRecipeLogic` under `gregstuff/GregRecipeLogic/`. These names are proposed new classes, not existing symbols.

Use `GTBlocks.MACHINE_CASING_UHV` as the temporary high-progression casing; it exists in the pinned artifact. Put the controller at the center of the front face, using the project's non-Y-axis rotation convention. This placement and casing choice are implementation-level choices within the user's delegated temporary-casing scope.

Build five 5×5 aisles, with casing on all outer faces and `Predicates.any()` for the interior. Explicitly allow IMPORT_ITEMS, EXPORT_ITEMS, IMPORT_FLUIDS, and EXPORT_FLUIDS with independent maximum-global limits of five. Do not use an automatic ability helper that could silently allow energy, maintenance, muffler, or parallel hatches. Allow zero to five of each type structurally; processing still requires the relevant input/output capability. No maintenance requirement is introduced. Reuse native casing/overlay assets and verify creative availability and display name through the existing registration path.

Alternative rejected: subclass an electric multiblock and bypass voltage checks. It would introduce unnecessary power-related behavior and lifecycle assumptions.

### 2. Explicit state metadata and recipe-logic-owned persistence

Represent progression as a small `DORMANT` / `ACTIVE` enum serialized by stable names in the custom logic. It is not the recipe's WORKING/WAITING/SUSPEND status. Use an inherited managed-field holder where synchronization needs it, and the existing custom save/load hooks for structured run data. Call superclass persistence hooks.

Use recipe data key `required_state`, accepting explicit `DORMANT` or `ACTIVE`; the initial recipe also identifies its operation with `operation = information_collection`. Validate metadata before consuming input, on cached restart paths, and when resuming a persisted recipe. Missing or unknown metadata fails closed. Show required state in recipe data info and actual progression state in a native progress/display line rather than a new custom GUI.

Register the `world_eater_tamer` type in `GregERecipeTypes`; use an explicit `GregECore.id("information_collection")` recipe ID rather than a string builder overload that selects another namespace. Wire recipe creation into `GregEGTAddon.addRecipes`, with one IV sensor input, a vanilla written-book output representative, `required_state = DORMANT`, a base duration of 2400 ticks, and no EU capabilities. Do not add overclocking or parallel modifiers. Verify the IV sensor symbol in the pinned artifact before authoring the recipe.

Custom persisted data includes a format version, progression state, fixed scan origin/dimension/build-height bounds, counts keyed by stable machine-definition identity, elapsed non-scanning ticks, pending unavailable chunk, retry attempt/delay bookkeeping, and any finalized pending book/result. Mark the holder dirty after mutations. Missing state in legacy/new data defaults to DORMANT; malformed state or run metadata must not activate a machine or reuse inconsistent counts.

A normal unload or part unload retains the run. Actual structure invalidation follows the existing recipe cancellation behavior, clearing unfinished scan bookkeeping while preserving progression state; it must not deliver a partial success report. A completed pending report is retained separately across output blockage and reformation until it can be delivered. This uses existing cancellation semantics rather than introducing structural-break recovery rules.

Alternative rejected: storing only progression state on the controller or inferring ACTIVE from current recipe status. Neither satisfies recipe-logic persistence and they conflate permanent progression with temporary execution.

### 3. Deterministic, fixed-volume tick sections derived from progress

At successful recipe setup, capture controller chunk origin, dimension, minimum build height, and maximum exclusive build height. These define a stable scan domain across reload. Use chunk-major traversal in a documented fixed order, then local X/Z and Y, covering exactly 81 chunks × 16 × 16 × height positions. Keep section arithmetic in a small testable production helper if needed, not a scanning framework.

Let `B = 2400` be the fixed base scanning duration, `W` the total position count, `p` the inherited elapsed recipe progress, and `d` the actual elapsed non-scanning retry ticks. Effective scan progress is `s = p - d`, clamped to `[0, B]`. The next successful scanning tick visits the half-open range:

    [floor(W * s / B), floor(W * (s + 1) / B))

Use long intermediate multiplication. On a completed batch, increment inherited progress once. The batch for `s = B - 1` includes the final position and completes the scan immediately before report finalization on that tick. The batch bound is `ceil(W / B)`; no final catch-up pass is allowed. For the normal 384-block height, the inspected calculation gives 7,962,624 positions and at most 3,318 positions per scanning tick. Build-height variation changes that budget proportionally and must be measured in runtime tests.

Do NOT derive scan sections from `getProgressPercent()` after extending duration: its changing denominator would move boundaries. Instead use the same saved progress with the fixed base and persisted elapsed delay adjustment. Do not use the upstream generic wait/regression path for chunk retries.

Before visiting positions, preflight all loaded chunks touched by that batch using non-loading server chunk-cache access. If any is unavailable, scan none of the batch and keep its derived position unchanged. This avoids partial batch checkpoints and duplicate counting on retry; in the server-thread callback the loaded chunk references can then serve the entire bounded batch. Read block entities through loaded chunk access/`MetaMachine.getMachine(chunk, pos)`, never world-level access that may load or retain chunks. Avoid building whole-chunk block-entity lists or scanning all block entities at recipe setup.

Alternative rejected: scan an entire chunk each tick, enumerate every machine up front, or divide by a mutable extended duration. These respectively create tick spikes, front-load all work, or corrupt coverage.

### 4. Retry time is elapsed recipe time but not scanning progress

At the first failed preflight, remember the first unavailable chunk in deterministic traversal order and schedule a 20-tick delay. Increase the instance's persisted duration by 20 immediately; do not mutate the global recipe definition. The failed-preflight tick is the FIRST delay tick: increment both inherited progress and elapsed non-scanning ticks and leave 19 delay ticks remaining. During the remaining delay ticks, increment both counters, scan nothing, and decrement the saved delay. Retry the same pending batch after exactly 20 non-scanning ticks. This avoids adding an unaccounted detection tick to each one-second extension.

Allow five additional preflight attempts after the initial miss. Each is preceded by its own 20-tick delay and 20-tick duration extension. If the fifth retry fails, finalize failure immediately; do not schedule a sixth delay or keep running until the nominal 120-second remainder elapses. Recovery clears that unavailable-chunk episode's attempt bookkeeping and executes the unchanged pending batch without discarding past counts. A later unavailable episode receives its own allowance; total elapsed delays still persist for schedule normalization.

The final retry must still validate the whole batch. If another chunk in that batch is now unavailable, do not scan partially; associate retry bookkeeping with the actually pending unavailable episode. Persist enough identification to distinguish that case. Suspension and unavailable own structure/parts stop recipe ticks and therefore do not consume retry delay time.

Encode the failure location as the missing chunk's block-space X/Z area (minimum and maximum inclusive block coordinates), not its chunk indices. The failure book states that the scan is incomplete and does not imply its partial count is the complete total. Failure never changes progression state from DORMANT.

Alternative rejected: skip unloaded chunks, force-load them, treat absence as zero, use real wall-clock timers, or let WAITING reset scan progress.

### 5. One eligibility function and stable count identities

Add one machine/controller policy method, for example `isCountableInterface`, taking the already available loaded-block/machine context. Exclude the current controller by position first. Count other multiblock controllers and standalone GregTech machines; reject `IMultiPart` buses/hatches/parts, ordinary blocks, and non-GregTech block entities. Keep explicit self/attached-part exclusions so broadening the predicate later cannot accidentally count the tamer's own interfaces. Controller recognition takes precedence over generic part rejection if a supported controller also implements a part interface.

Accumulate by stable machine-definition registry identity, not a localized display string or ItemStack NBT. Use that definition's display name in the final report and deterministic identity ordering for stable pages. No formed-only or working-only restriction is added for other controllers: the user asked to count controllers, not only active infrastructure.

Alternative rejected: follow every part to its controller and deduplicate controller positions. Visiting controllers directly is simpler and correctly excludes a controller outside the scan square even when one of its parts lies inside it.

### 6. Freeze the dynamic book before output and state transition

Generate an instance-owned `Items.WRITTEN_BOOK` after the last successful batch or retry exhaustion. Serialize vanilla JSON text components in its pages with title/author and valid resolved-book metadata. Paginate measured text safely; emit the exact name/count format, total, and a success or failure summary. A zero-count run still yields a readable report. Do not mutate a recipe-manager-owned recipe or reuse a previous run's book as the next run's static output.

Integrate output through the custom logic's finish path using the existing recipe item IO/capability handlers, substituting the finalized stack into a copied run recipe as needed. Simulate output capacity against the real finalized stack, then deliver it; check the actual IO result. Disable voiding for this mandatory report. A blocked delivery retains the book and completion outcome persistently; scanning is already finished and no new input may be consumed. Delivery attempts run only when the controller can process its outputs.

Only after successful delivery does a successful total of at least 500 set ACTIVE. Below-threshold successes and failures remain DORMANT. Clear the completed run once, then permit fresh recipe search, rechecking state before any cached restart. Preserve expected `afterWorking`/suspend lifecycle behavior explicitly rather than calling the default finish routine and risking an immediate second run or ignored output failure.

Alternative rejected: directly spawning a book entity or blindly calling the superclass finish path. Both weaken output-capacity, exactly-once, and state-transition control.

## Risks / Trade-offs

- [Many controllers or unusual world height increase tick cost] → Fixed position budget, no world-wide allocations, cached loaded chunks for the bounded batch, and real tick-duration measurements before accepting performance. Do not promise an arbitrary millisecond ceiling without measurements.
- [Progress and retry bookkeeping drift after reload] → Persist all counters together, derive the cursor exclusively from adjusted inherited progress, and test every delay boundary and the last scanning tick.
- [World changes during the scan] → Counts reflect scan-time observations; deliberately do not rescan already completed sections or claim a point-in-time snapshot.
- [Upstream finish, invalidation, or cached-recipe behavior loses/duplicates books] → Dedicated completion state, actual output IO checks, state-gated restart, and unload/reformation/output-blockage fixtures.
- [Names or many machine types produce large book data] → Test vanilla readable-page and payload bounds, escaped/long names, and representative maximum type diversity; do not silently drop entries or emit malformed book data. An extreme dataset exceeding vanilla single-book representability must be raised before shipping rather than inventing a new product policy.
- [Mapped artifact confirms signatures but not complete runtime correctness] → Compile against the unchanged pinned dependencies and run Forge-world assertions; source inspection is not a gameplay test.

## Migration Plan

This is additive: register the controller, type, recipe, and resources, with DORMANT as the default for absent custom data. Existing machine and recipe IDs remain unchanged. Generate resources with the configured Forge data run and inspect the exact produced recipe/model/name assets. Verify in a disposable world before release.

Rollback by reverting only this feature's files and registrations before release. After players place the new controller, removing its registration can leave missing blocks/items in worlds; back up saves before downgrading. No automatic world migration or downgrade conversion is proposed.
