# Design

## Context

See `proposal.md` for motivation and the capability deltas for player-visible requirements. This design is necessary because the change crosses recipe dispatch, persisted progression, native item handling, client rendering, and destructive world mutation.

The project pins Minecraft 1.20.1, Forge 47.4.10, Java 17, GTCEu 7.5.2, and LDLib 1.0.40.b. The existing tamer owns its behavior in `WorldEaterTamerRecipeLogic`; ordinary casing/Bravery crafts delegate to native recipe logic, while surveys have their own tick/completion/persistence path. Currently every paid non-crafting startup initializes a survey, so adding recipe registration alone would select the wrong execution path. The controller derives its casing tier and intention from the formed structure and already exposes its interior center. `WorldEaterPartRender` handles part appearance but does not yet render an interior demand.

Native GTCEu completion does not retain output when output handling fails. This project already preflights ordinary craft outputs and retains a completed survey report. Follow those patterns instead of treating an unmet challenge as native successful recipe completion. The implemented `add-world-eater-intention-hatches` change is the specification predecessor even though its specs are not yet archived.

## Goals / Non-Goals

**Goals:**
- Keep earned progression, derived hardware, and transient recipe outcome separate.
- Reuse native generated materials, registered components, input handlers, output handling, persistence, and rendering primitives.
- Make the successful-payment and normal-timeout paths mutually exclusive, with no destructive behavior in generic cancellation callbacks.
- Keep the change local to the tamer, its registrations/presentation, and focused regression fixtures.

**Non-Goals:**
- No configurable challenge engine, extensible demand-provider API, new dependencies, mixins, global explosion hooks, or new recipe framework.
- No token acquisition balance, UEV material guess, forced high-tier content, parallel demand processing, offline countdown, anti-cancellation measures, or deferred attacks.
- No unrelated appearance-spec cleanup, predecessor archival, or changes to existing survey/ordinary craft economics.

## Decisions

### 1. Explicit operation dispatch, not an ordered-state shortcut

Add PREPARED to persisted progression, but do not interpret all ACTIVE recipes as automatically valid in later states. Centralize compatibility around the existing operation metadata:

| Operation | Progression | Additional requirements | Execution |
| --- | --- | --- | --- |
| Information collection | DORMANT | Existing survey requirements | Existing survey path |
| Casing conversion | ACTIVE or PREPARED | Existing requirements | Existing native craft/parallel path |
| Bravery hatch crafting | ACTIVE or PREPARED | Tier 2, existing inputs | Existing native single craft |
| Preparation test | ACTIVE only | Tier 2 and an intention hatch | One demand run |
| Essence production | PREPARED only | Tier 2 and an intention hatch | One demand run |

Keep existing operation IDs unchanged; add separate test/real IDs. Update acceptance, diagnostics, startup, working, finishing, modifiers, and display routing together. In particular, only an explicitly recognized survey initializes `WorldEaterScanDomain`. The controller's parallel modifier remains restricted to casing recipes, now in ACTIVE or PREPARED. Native numeric recipe metadata validation must continue accepting numeric NBT types produced by recipe serialization, not only an int tag.

Alternative rejected: `state >= requiredState` would incorrectly allow repeating the preparation test or survey after their unlocks. Treating demands as ordinary recipes would also make the random input a fixed startup ingredient or grant outputs on timeout.

### 2. Native materials and a small, resolved demand pool

Register addon-owned gem materials in `GreggyItems` using the existing material event wiring, and the essence as an ingot material. Use generated forms through `ChemicalHelper`, not standalone entries in `ModItems`. Add their primary forms to the existing creative listing and language/resources. Concrete palette defaults are Desarium `0x9B59B6` with DIAMOND, Implementium `0x38A65A` with EMERALD, and essence `0x3B1656` with SHINY; these RGB values are design defaults for the agreed purple/green/dark-purple appearance. Preserve `tamed_world_eater_essense` and its requested display spelling. No formula, fluid, ore, acquisition recipe, or custom chemistry is needed.

Build the fixed pool after registrations are usable from `(item, tier)` entries: the eight confirmed component families at LV through UEV, plus the three confirmed material forms at LV through UHV. Resolve optional component entries individually, checking both null/empty handles and actual item registration. Do not override `requiresHighTier`, modify configuration, or assume a development runtime proves production availability. GTCEu 7.5.2 can enable high-tier registration in development even when the config says false.

The pinned native material flags do not supply every requested form. Add only the missing flags through the existing mod-bus material lifecycle's `PostMaterialEvent`:
- Aluminium and Neutronium: `GENERATE_ROTOR`.
- RhodiumPlatedPalladium and Darmstadtium: `GENERATE_GEAR`.

Use native generated forms and normal processing routes. The material mapping is the casing-tier mapping in the spec, not GTCEu's different component-rotor recipe mapping. Verify all requested material forms rather than quietly omitting a failed required registration. Omit only unavailable optional components, and reject an empty usable pool before token payment. A small tamer-local pool helper/record is sufficient; no public registry or plugin system.

Alternative rejected: a raw registry-name sweep admits unrelated items and loses tier identity; unconditional optional item dereferences crash without high-tier registration. Substituting other materials or generating custom items would violate the agreed pool.

### 3. Paid startup chooses once; the timer measures a deadline

Register two real tamer recipes in `GregEGTAddon`: one normal Desarium gem with no item output, and one normal Implementium gem with one essence ingot output. Neither has power/fluid tick capabilities. Keep a baseline minimum duration for recipe construction, but explain in recipe-viewer text that the actual deadline depends on the selected tier.

Before native input payment, validate state, formation, tier, intention, unmodified single-run recipe shape, pool availability, and ordinary output capacity for a real run. Copy the candidate recipe and its data as the existing startup does. Only after native startup confirms payment, select one pool entry and one quantity from 1 through 5 using server randomness. Set this run's duration to `600 * tier` processing ticks. Do not mutate the registered recipe or reroll during candidate matching.

Keep the run data beside existing recipe progress: run kind, demanded stack/count, mapped tier, and an earned-reward marker. Native paid `lastRecipe` and progress/duration remain the authoritative recipe/timer context; avoid storing a second ticking clock. The conceptual phases are:

    idle --> token paid / waiting for demand
                       |              |
             exact demand paid   valid normal expiry
                       |              |
                    success        timeout
                       |              |
             test: PREPARED    test: harmless end
             real: reward     real: one consequence
                       |
                output blocked
                       |
                reward pending --> delivered

Suspension and ordinary unload preserve remaining processing ticks. Invalid structure, removed intention, explicit cancellation, or another failed run prerequisite ends unfinished work harmlessly. A condition that merely waits/pauses does not advance the deadline or cause a destructive callback. There is no wall-clock deadline and no attempt to prevent the agreed escape route.

### 4. Exact mid-run payment across any attached input bus

On each enabled demand-processing tick, validate the run, then check demand payment before incrementing/expiring the deadline. The demand remains outside the registered recipe's startup ingredients, so players can supply it later.

Use the controller's flattened `IO.IN` handlers for `ItemRecipeCapability`, visiting each native input handler once. Feed a counted, exact-item ingredient through `IRecipeHandler.handleRecipe(..., simulate)` with the remaining requirement passed from handler to handler. First simulate across all attached inputs; only if the full requirement is satisfied, execute with fresh copied contents. This reuses native handler semantics, supports quantities split across buses/slots, and avoids hard-casting to a particular storage implementation. Do not bind demand payment to the token's original color/distinct input group: the contract is any attached input bus. Wrong item/form/tier and insufficient totals consume nothing.

After exact payment, clear the visible demand and stop its deadline before attempting any output. A test persists PREPARED immediately and ends without output. A real run marks one reward earned and attempts native output delivery with simulation/result checking; if blocked, retain that earned reward rather than checking/charging the demand again. Retry delivery while available, with no new token, timeout, or recipe restart. Use the existing completed-report retention pattern for completed reward state; interruption semantics apply to unfinished challenges, not to recharging an already successful run.

Alternative rejected: checking only the token bus violates the input contract; per-slot extraction before verifying the total loses partial payments. Simulation is not a general transaction against arbitrary third-party handlers that mutate on simulation or fail execution after promising success. Treat unexpected execution failure as a harmless interruption, never as success or a timeout penalty; do not invent unsafe compensating item insertion or an inventory transaction framework.

### 5. Outcome-specific settlement, never punishment in reset

Use explicit success, normal-expiry, and interruption branches inside the tamer logic. Generic `resetRecipeLogic`, `interruptRecipe`, invalid saved-data handling, and structure invalidation must not invoke punishment. `onRecipeFinish` must not delegate a demand timeout to native output-producing completion.

At normal expiry, check demand one last time and confirm the current run, tier-2 structure, and intention are still valid. Capture the consequence target/intention, settle and clear the run, and mark the controller changed before changing the world. Then apply the one bounded consequence. This ordering prevents the Safety block removal and its invalidation callbacks from recursively settling the same run. Keep PREPARED outside run reset. Repeated/stale finish callbacks find no active outcome to execute.

Native completion can immediately restart an available recipe. Do not allow that behavior to reuse old demand data: a later run must pass startup gates, pay another token, and select a fresh demand. Preserve existing suspend-after-finish behavior rather than adding an alternative scheduling system.

### 6. A bounded random-origin search and cosmetic explosions

Reuse `WorldEaterScanDomain` region bounds and `isCountableInterface` eligibility, but not a prior survey's aggregated counts. At a Bravery timeout, choose a random block coordinate over the full controller-centered inclusive 9x9 chunk square and valid build-height interval. Inspect only `getChunkNow` results and their current block-entity maps. Resolve machines against those loaded chunks, apply the shared eligibility predicate, and choose minimum squared three-dimensional distance from the random origin. Resolve equal distances by a stable coordinate order.

This computes the first eligible result of an outward search without walking every air block or scheduling another survey. Randomness belongs to the starting point, not a uniform draw over a machine list. The result may favor machines with larger empty space around them; that is intentional. An empty search ends immediately without a future retry/ticket. Recheck eligibility and loaded availability before mutation; a lost target causes no deferred attack.

For Safety, target only the attached Safety Hatch. For Bravery, target at most the selected machine block. Remove that block through normal block lifecycle callbacks, and emit explosion particles/sound at that location. Preserve normal machine-removal/inventory lifecycle rather than manually deleting attached structures or contents. Do not use the existing reactor's damaging `Level.explode` path or a machine explosion helper: an explosion configured not to break blocks can still damage entities, which contradicts the no-collateral requirement. Never add fire or destroy the victim controller's other multiblock parts.

Alternative rejected: a blast with a smaller radius is not an exact one-block guarantee. Repeated random coordinate guesses can run unbounded or miss available machines; loading chunks expands the damage and performance scope.

### 7. Additive persistence with independent validation

Keep the existing version-1 save format readable and add an optional demand-run compound alongside the existing survey fields. Absence of the new compound is a normal old save. Persist new progression through the existing state string, and serialize the demanded stack, mapped tier, run kind, and earned-reward phase; reuse native progress/duration and paid recipe data.

Load earned progression separately from demand validation. Validate a pending challenge against its operation/state, registered demanded item, fixed pool/tier mapping, count range, expected deadline, progress bounds, and absence of conflicting survey state. A demand recipe with missing/invalid demand data, or an optional item removed since saving, is cancelled without reward or punishment while retaining separately valid ACTIVE/PREPARED. Do not route that case through the current broad catch that resets all progression to DORMANT. Keep existing invalid-format/progression and survey corruption guards intact.

A valid earned real reward is a completed result, not a demand awaiting validation or repayment; persist its delivery state and deliver exactly once after output becomes usable. Loading must not reconstruct a success from arbitrary malformed run fields or rerun a saved timeout consequence. Use fresh copied NBT snapshots per regression case to avoid shared-tag mutation between tests.

Alternative rejected: resetting PREPARED because one optional component disappeared violates permanent unlock. Rerolling on restore or replaying startup consumes or creates resources incorrectly. A new generic migration framework is unnecessary for these additive fields.

### 8. Extend the native renderer and presentation channels

Use server-owned synchronized demand data on the existing tamer logic/controller and extend `WorldEaterPartRender` to render a native item together with its readable display name and numerical count at `getStructureCenter()`. Resolve the name from the synchronized demanded item's native display name rather than showing its registry identifier. Reuse the neighboring `PressureChamberRender` item-rendering pattern and native font/pose primitives. Enable the existing dynamic renderer only as needed, with render bounds covering the interior; retain its current connected, dominant-color part-model behavior. Keep all client renderer/font imports out of authoritative server classes.

Clear displayed demand on every settlement/cancellation; synchronize startup, suspension/reconnect, and cleanup. Keep native `addDisplayText`/custom progress text for state, remaining deadline, demand, and pending reward. Do not replace the machine GUI or add custom networking when the installed synchronization mechanism suffices.

Extend `GregERecipeTypes` operation captions and the machine/hatch help. The test has no physical output, so its PREPARED result must be textual. Show the randomized tier-scaled deadline and immediate-payment behavior rather than suggesting the displayed recipe baseline is a mandatory wait. Remove only obsolete future/no-effect language for these new runs; existing recipes remain unchanged. Retain readable diagnostics for rejected state/tier/intention and invalid single-run metadata.

Alternative rejected: a GUI-only item/name/count does not satisfy an in-structure display, and a general world-overlay framework adds scope without benefit.

### 9. Verify the native behavior, not only helper predicates

Extend the existing GameTest fixtures for tamer runs, casing recipes, intention recipes, reload/unload, and client presentation. Add a focused demand-run fixture rather than a separate test harness. Cover actual recipe lookup and token payment, both intention choices, exact/split input payment, no partial consumption, deadline-boundary success, output-blocked retention, save/load, harmless interruptions, and duplicate callbacks. Exercise PREPARED casing parallelism and Bravery acquisition alongside the unchanged DORMANT survey behavior.

Use deterministic randomness/origin inputs at the small helper boundary where necessary to exercise pool boundaries and search ordering, while retaining integration coverage of the actual startup/timeout paths. Verify every required generated material form and an absent/partially present optional component resolver. Do not rely only on a development client for high-tier absence; also exercise the absent-registration path explicitly without modifying user configuration.

Destruction fixtures must place neighboring blocks, machines, victim multiblock parts, and entities; assert exactly the allowed block changes and no entity/fire damage. Include an actually unloaded chunk and verify no ticket/load request. Record native client checks for the centered item/name/count, including readable long component/material names, across orientations, suspension/reconnect/resource reload, cleared demand, material icons/names, UI text, and the exact JEI recipe category. Retain screenshots outside the repository. Dedicated-server startup remains a separate check for client-only class leaks.

## Risks / Trade-offs

- [Existing callbacks assume every non-craft is a survey] -> Route all operations explicitly and retain existing survey/craft regressions before adding demands.
- [Destroyed Safety hatch re-enters invalidation] -> Settle before world mutation; never punish from reset; test repeated callbacks.
- [Optional entries differ between development and production] -> Resolve registrations defensively and test missing/partial availability without forcing high-tier content.
- [Enabling native forms adds their ordinary GTCEu processing recipes] -> Restrict flags to the required forms and inspect generated recipes; no custom substitutes or unrelated material changes.
- [Native or third-party output/input handlers violate simulation expectations] -> Use native simulation plus execution results, preserve pending earned output, and fail harmlessly; do not claim arbitrary cross-handler atomicity.
- [A successful reward is waiting when the structure changes] -> Reuse completed-result retention rather than treating a paid success as another unfinished demand.
- [Random-origin nearest search is not uniform over machines] -> Preserve the requested origin-first behavior and verify nearest selection with fixed origins.
- [Specs depend on an unarchived predecessor] -> Sync/archive intention-hatch specs first when archival is later requested; do not modify that change as part of this proposal.
- [Downgrading loses knowledge of PREPARED/new material IDs] -> Back up worlds before deployment; rollback uses the matching older backup, not an in-place downgrade of new content.

## Migration Plan

1. Implement only after this proposal is explicitly applied. Keep runtime/source changes separate from these planning artifacts.
2. Add registrations and backward-readable state/run fields without rewriting old worlds. Test DORMANT/ACTIVE saves and paid existing crafts/surveys before introducing new-run save cases.
3. Verify required generated forms, recipes/resources, native GameTests, rendered client behavior, and dedicated-server loading with the pinned dependency set. Report actual test discovery/results, not a historic expected count or process exit alone.
4. Deploy with a world backup; let the modpack independently supply the two new token materials. High-tier component availability remains the pack's decision.
5. When archival is requested, synchronize/archive `add-world-eater-intention-hatches` before this change's dependent deltas. No predecessor or main spec is changed by this planning workflow.
6. For rollback after new items/PREPARED saves exist, restore the corresponding pre-upgrade world backup with the prior mod version.
