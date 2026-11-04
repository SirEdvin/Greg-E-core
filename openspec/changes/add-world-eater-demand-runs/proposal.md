# Proposal

## Why

The tier-2 World Eater tamer has activation and intention hatches but no playable preparation or essence-production step. Add timed delivery challenges that turn the player's Safety/Bravery choice into a bounded, understandable consequence while permanently unlocking production through a successful test.

## What Changes

- Register addon-owned GTCEu materials: purple DIAMOND-style Desarium gems for test tokens, green EMERALD-style Implementium gems for real tokens, and dark-purple SHINY ingots of `Tamed world eater essense`. Preserve the requested `essense` spelling. Add no token acquisition recipes or world generation; the modpack supplies tokens.
- Add power-free, single-run tamer recipes requiring a validated tier-2 frame and either installed intention hatch. The test consumes one Desarium gem in ACTIVE; success permanently advances to PREPARED. A real run consumes one Implementium gem in PREPARED; success produces one essence ingot. PREPARED retains existing casing and Bravery-hatch crafting and never reverts merely because a run fails or the structure changes.
- At paid startup, select one demanded item and a quantity from 1 through 5. Use motors, pumps, conveyors, pistons, robot arms, sensors, emitters, and field generators from LV through UEV. UHV/UEV entries are optional: include only actually registered items, without forcing high-tier content or changing configuration.
- Also include normal gears, long rods, and rotors using casing-tier materials from LV/Steel through UHV/Neutronium; add no UEV material entries. Enable missing native forms needed for this explicitly requested material pool rather than inventing substitute items.
- Give each run a deadline of 30 seconds per voltage-tier index: LV 30 seconds through UEV 300 seconds, independent of quantity. Detect and consume exactly the full demanded quantity from attached input buses and finish immediately; this is a deadline, not a minimum processing time. Display the demanded item's icon, readable name, and count together inside the structure.
- A test timeout produces nothing, changes no progression, and damages nothing. Only a normally expiring, unmet real challenge triggers punishment: Safety destroys its own hatch only; Bravery destroys at most one other eligible machine only. Both use explosion presentation without collateral damage.
- Bravery chooses a random point in the same full-height 9x9-chunk survey region, then searches outward for an eligible machine using loaded chunks only. Reuse survey eligibility, excluding this tamer and multiblock parts. An empty search has no destructive effect.
- Frame breaks, hatch removal, cancellation, invalid saved work, and other non-timeout failures cause no punishment. This escape route is intentional. Preserve demands and remaining processing time over normal save/load, and retain successfully earned output when a bus becomes blocked.

## Capabilities

### New Capabilities

- `world-eater-demand-runs`: Token/reward materials, supported demand pool, immediate exact payment, tier deadlines, in-world demand presentation, persistent run outcomes, and bounded timeout consequences.

### Modified Capabilities

- `world-eater-tamer`: PREPARED progression, explicit operation/state compatibility, unchanged survey behavior, tier/intention gates, and retained ordinary crafting/parallel behavior.
- `world-eater-casings`: Existing ACTIVE casing conversions and paid-work guarantees also apply in PREPARED without changing recipes or artwork.
- `world-eater-intention-hatches`: Existing Bravery acquisition and its presentation remain valid in PREPARED; hatch descriptions now explain the new timeout-only consequences. This capability is defined by the completed predecessor change, as noted below.

## Impact

- Extend `WorldEaterTamerRecipeLogic`, `IncompleteWorldEaterTamer`, the existing `WorldEaterPartRender`, and recipe captions in `GregERecipeTypes`; preserve current casing rendering, survey bookkeeping, and recipe diagnostics.
- Register materials through `GreggyItems`/`GregECore`, recipes through `GregEGTAddon`, and primary item creative entries through `ModCreativeModTabs`; update intention-hatch help, language/generated resources, and native client/JEI fixtures.
- Reuse existing World Eater and intention GameTests, adding focused demand, deadline, optional-registry, persistence, target-selection, and no-collateral regressions. No dependency upgrade, mixin, global failure framework, new configuration surface, or token crafting balance is required.
- Planning baseline includes the implemented but unarchived `add-world-eater-intention-hatches` change. Its delta specs must be synchronized/archived before this change's dependent deltas are archived; this proposal does not modify or archive that predecessor. Current main specs also still describe older casing appearance; this change preserves the current dominant-color implementation rather than doing an unrelated appearance-spec cleanup.
- Existing DORMANT/ACTIVE saves remain readable. PREPARED and new material IDs are additive saved-world content; downgrading after using them requires a world backup. This workflow writes planning artifacts only.
