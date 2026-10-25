# Design

## Context

See `proposal.md` for motivation and `specs/world-eater-tamer/spec.md` for the behavior contract. A design is warranted to distinguish transient formation data from persistent progression and prevent stale tiers across invalidation/reload.

Observed local integration points:

- `src/main/java/net/cu5tmtp/GregECore/gregstuff/GregMachines/machines/endgame/IncompleteWorldEaterTamer.java:65-79` owns the small frame and explicit part alternatives. Line 73 accepts only black casing. The controller has no casing-tier field or formation overrides yet.
- The same class's appearance/model registration remains black, and its tooltip at line 94 states the obsolete black-only rule. Its existing native UI reads progression from the recipe logic; no new UI is required by this change.
- `src/main/java/net/cu5tmtp/GregECore/gregstuff/GregRecipeLogic/WorldEaterTamerRecipeLogic.java:329-343` already preserves progression during recipe resets. Lines 366-445 save and validate version-1 progression/run data independently of structure composition. Completed pending survey reports have explicit retention; unfinished recipes retain existing cancellation behavior.
- `src/gameTest/java/net/cu5tmtp/GregECore/gametest/WorldEaterStructureTests.java:102-106` currently rejects colored casings alongside UHV. `WorldEaterRunTests.java:118-125` already checks ACTIVE surviving invalidation/reformation. Reuse these fixtures and the casing-processing/reload suites.
- `docs/world-eater-casings.md:3-7` still documents black-only frames. No casing registration, recipe provider, recipe type, or asset needs changing.

Pinned upstream source inspected at GTCEu tag `v7.5.2-1.20.1`:

- `common/data/machines/GTMultiMachines.java:134-150` registers the EBF with `CoilWorkableElectricMultiblockMachine` and `Predicates.heatingCoils()`.
- `api/pattern/Predicates.java:226-246` records `CoilType` in the match context but rejects unequal coil types. Reuse the storage/lifecycle pattern, not that equality rule.
- `api/machine/multiblock/CoilWorkableElectricMultiblockMachine.java:28-33` reads the matched value during `onStructureFormed()` into a controller-owned runtime field.
- `api/pattern/BlockPattern.java:119-124` cleans the state before a full pattern check; `api/pattern/MultiblockState.java:66-70` resets the match context. Its `onBlockStateChanged` path rechecks the pattern and calls formation or invalidation on the controller.
- `api/pattern/util/PatternMatchContext.java` supplies `getOrDefault` and `set`; it is temporary matching data, not a progression-save mechanism.

These are source observations, not evidence of runtime verification of this proposed change.

## Goals / Non-Goals

Goals:

- Compute casing composition inside the existing pattern traversal, then expose only a successfully formed result.
- Keep a minimal controller-local implementation with native predicates, lifecycle callbacks, and existing tests.
- Reuse the current progression data and fail-closed load validation without a format migration.

Non-goals:

- A casing-tier registry, extensible upgrade framework, independent world scan, tick subscription, custom packet, or new dependency.
- Treating native recipe WORKING/WAITING status as DORMANT/ACTIVE progression.
- Generalizing recipe acceptance for future tier-2 recipes, changing controller drops, adding a tier GUI, or modifying connected textures.

## Decisions

### 1. Accumulate the minimum casing tier in native match context

Replace only the black-casing branch of `C` with a small tamer-local casing predicate patterned after `heatingCoils()`. Recognize the existing four exact block registrations: black contributes 1; purple, green, and orange each contribute 2. Reject unrelated blocks without mutating the context.

Use one addon-specific context key, proposed as `gregecore:world_eater_casing_tier`. For an accepted casing, reduce its value with the existing minimum, defaulting the accumulator to 2. Do not use the context's absent integer value of zero as the starting minimum. Supply the four real casing variants as native preview candidates, with black first for a stable default preview.

Keep the existing explicit part predicates and all of their limits. Their successful branches do not contribute a casing tier. The controller and unrestricted `#` positions never enter the casing predicate. This naturally ignores black controller/part appearances and decorative black blocks outside required casing positions.

The minimum operation accepts mixed colors, is independent of traversal order, and tolerates repeated evaluation of the same accepted casing. Native context clearing prevents previous checks from influencing the next one. Do not mutate the controller from the predicate: matching can be provisional, fail, or run through upstream asynchronous checking.

Alternatives rejected: copying the coil equality test would prohibit requested mixtures; counting only colored casings against a fixed frame size would mishandle hatch substitutions; scanning positions after formation duplicates native traversal; a broad tag silently admits future unapproved variants.

### 2. Publish a derived runtime tier only after successful formation

Mirror the EBF lifecycle with a small controller runtime field and proposed accessor `getCasingTier()`; these are new local symbols, not assumed existing APIs. Read the completed match-context value in `onStructureFormed()` while preserving the superclass callback. Clear the cached tier in `onStructureInvalid()` while retaining the superclass invalidation behavior.

Use 0 only as the internal unavailable sentinel, not as a gameplay tier. The accessor returns an operative 1 or 2 only for a formed, error-free structure with a valid matched result; otherwise it returns 0. A missing result must not default to tier 2. This prevents a failed check or unloaded structure from exposing stale capability. The current required-frame geometry and maximum part substitutions leave actual casing positions; no new minimum-casing rule is needed.

Do not persist the field. A fresh controller or reload starts with no derived result until the current structure is validated. Rechecks of a still-valid frame must refresh the result too, so direct casing replacement can upgrade or downgrade without requiring controller removal. The source of truth is the current pattern match, not historical NBT.

    required casing --> native match context --> successful formation --> tier 1 or 2
    failed validation / invalidation -------------------------------> unavailable

Alternatives rejected: a permanent tier-2 flag contradicts downgrade behavior; placing tier in recipe-logic NBT conflates hardware with progression; reading raw provisional match data directly from recipe consumers risks exposing a partial check. Subclassing the electric coil machine would introduce unrelated power/coil behavior.

### 3. Leave persistent progression and recipe behavior separate

Do not move DORMANT/ACTIVE into `MultiblockState`, rebuild the recipe-logic trait on tier changes, or clear its saved progression. Retain the controller registration and version-1 data. Existing code already preserves valid progression on recipe reset, so prefer no production edit to `WorldEaterTamerRecipeLogic` unless a focused regression identifies a real defect.

The preservation guarantee applies to casing/structure lifecycle events, not to disabling the intended DORMANT-to-ACTIVE survey transition. It also does not turn malformed save data into valid progression. Fresh controllers keep their existing DORMANT default. Destroying/replacing the controller is outside the retained-controller guarantee; do not introduce new drop-NBT or relocation policies.

Existing recipes remain usable at both casing tiers under their existing state gates. Add no `required_tier` metadata, modifiers, or future recipe dispatch. An actual frame break still invokes ordinary recipe cancellation. Completed pending survey reports retain their existing special protection and can trigger activation only on normal successful delivery.

Alternative rejected: preserving every unfinished recipe during construction is a separate recovery feature with paid-input/output risks, not necessary to preserve ACTIVE.

### 4. Keep presentation and compatibility changes proportional

Update the existing tooltip and `docs/world-eater-casings.md` to explain accepted variants, reversible tiers, permitted-part exclusion, and retained progression. Preserve black controller/formed-part appearance, overlays, animations, and same-variant connected-texture behavior. Mixed-color acceptance does not imply that unlike textures should connect.

No new live tier panel is required. Tests and future server-side recipe logic can use the small tier accessor; a later recipe feature can decide whether it needs additional presentation. Retain existing preview support through the casing predicate rather than new rendering infrastructure.

The durable spec inventory is empty, while `add-incomplete-world-eater-tamer` and `add-world-eater-casings` both contain unarchived `world-eater-tamer` deltas. This change consequently uses the same capability path with ADDED requirements and a Purpose. Before a later synchronization/archive, consolidate the earlier baseline and reconcile the frame replacement as the appropriate MODIFIED/RENAMED delta (or equivalent supported reconciliation), preserving unrelated survey and casing requirements. Never append the new acceptance rule alongside an unrevised black-only rule. This planning change does not edit or archive either earlier change.

## Risks / Trade-offs

- [Stale tier survives a failed check or reload] -> Keep match data provisional, clear the runtime cache on invalidation, gate its accessor on valid formation, and exercise repeated checks, actual replacement callbacks, and reload with a changed frame.
- [Black casing seen early is overwritten by later colored casing] -> Use a minimum, not last-match assignment; test black at different frame positions and across orientations.
- [Parts accidentally prevent tier 2] -> Reduce only on accepted casing blocks; test fully colored frames with each permitted part family and maximum permitted substitutions.
- [A test invokes callbacks manually but misses real block-change behavior] -> Include an actual formed-machine placement/removal sequence and a controller reload fixture, in addition to direct pattern assertions.
- [Players confuse ACTIVE retention with craft retention] -> Document that frame breaks can cancel unfinished paid work; recommend replacing casings while idle. Do not weaken completed-survey-report retention.
- [Save compatibility is accidentally weakened] -> Keep existing identifiers and version-1 validation, and rerun progression round-trip, malformed-data, pending-report, survey, and casing-processing regressions.
- [Earlier unarchived specs contradict the new frame contract] -> Explicitly reconcile the baseline at later synchronization/archive, not by silently rewriting prior planning now.

## Migration Plan

1. Implementation is a separate, explicitly requested apply phase. Work on a dedicated feature branch then, preserving unrelated untracked files; do not stage the entire existing OpenSpec/Hermes trees.
2. Ship the controller change, regression coverage, and updated documentation together. Existing black frames remain valid tier-1 structures with unchanged progression. Players can replace casings incrementally without replacing controllers; no automatic block conversion or NBT migration is needed.
3. Verify in a disposable world: all-black, partially upgraded, fully colored mixed frames, tier-2-to-1 downgrade, rebuild/reload, existing recipes at both tiers, and ACTIVE retained throughout. Use fresh named GameTest results, not merely Gradle exit status.
4. Rolling back this change alone restores black-only frame acceptance: colored frame casings must be replaced with black before the older build can form them. The casing IDs already exist in that baseline. Back up the save and do not imply that downgrading past the casing-registration release is safe.
