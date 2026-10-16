# Design

## Context

See proposal.md for motivation and the capability deltas for observable behavior. This is a cross-cutting resource, registration, recipe-lifecycle, and save-compatibility change, so a design is warranted.

Observed implementation:

- `build.gradle:190-192` pins GTCEu 7.5.2 and LDLib 1.0.40.b on Forge 1.20.1 / Java 17.
- `IncompleteWorldEaterTamer.java:37-53` uses UHV for its appearance, frame predicate, and controller model. Its 5×5×5 skeleton allows up to five of each item/fluid import/export part, but no parallel hatch or energy hatch.
- `GregERecipeTypes.java:23-27` limits `WORLD_EATER_TAMER` to one item input, one item output, and no fluids. New recipes require two item inputs.
- `GregEGTAddon.java:40-48` registers the DORMANT sensor recipe through the existing addon recipe hook.
- `WorldEaterTamerRecipeLogic.java:104-136` accepts only the DORMANT information-collection operation and creates scan state for every accepted setup. Its working and completion overrides implement only surveys; simply loosening the state check would interrupt ACTIVE crafts or suppress their outputs.
- `WorldEaterTamerRecipeLogic.java:338-417` layers versioned survey/progression data over native recipe persistence. The native running recipe must remain distinct from the optional scan domain.
- `GreggyBlocks` supplies GTRegistrate block registration and is initialized from `GregEGTAddon.initializeAddon`. `WorldEaterStructureTests` and sibling GameTests share a UHV-frame fixture; existing opt-in client checks are available.
- The main OpenSpec spec inventory is empty. The completed, unarchived `add-incomplete-world-eater-tamer` change already owns an earlier `world-eater-tamer` delta. This proposal reuses that capability path but does not edit or archive the earlier change.

Pinned reference inspected directly from the dependency JAR:

- `assets/gtceu/textures/block/casings/gcym/industrial_steam_casing.png` is the confirmed visual reference: a framed plate with corner bolts.
- Its metadata provides connected-texture information, not an animation. Connectivity is not required for the new family.
- Existing GTCEu coil `_bloom.png.mcmeta` resources use `ldlib.emissive: true`; this offers a native emissive rendering route without requiring optional shaders. Resource metadata alone is not evidence that a newly composed model renders correctly; native client verification remains required.
- Inspection of the pinned `GTRecipeModifiers.hatchParallel` bytecode confirms it uses the controller's standard hatch and `ParallelLogic.getParallelAmount`, scales contents and parallel count, and does not change duration. `getParallelAmount` limits against inputs and output merging without imposing a voltage check.

## Goals / Non-Goals

Goals:

- Keep ordinary ACTIVE crafting on the native recipe lifecycle and isolate existing survey-specific work behind an explicit operation decision.
- Use existing Registrate, texture animation, emissive metadata, parallel hatch, and test infrastructure; keep changes local to the tamer and casing assets.
- Preserve controller registry identity and valid progression/survey persistence.

Non-goals:

- No new block entity, animation scheduler, ticking casing blocks, shader dependency, dynamic lights, or connected-texture subsystem.
- No custom parallel hatch, generalized recipe framework, power capability, batch-mode feature, larger structure, or automatic world migration.

## Decisions

### 1. Add a small casing family through existing Registrate wiring

Register four ordinary blocks and BlockItems alongside `GreggyBlocks`, with addon-owned proposed IDs `world_eater_tamer_casing`, `world_eater_tamer_casing_purple`, `world_eater_tamer_casing_green`, and `world_eater_tamer_casing_orange`. Follow neighboring block conventions for properties, mining tags, loot, models and generated names, and explicitly include all four in the mod's creative tab. Display the base as World Eater Tamer Casing and the variants with Dark Purple, Dark Green, and Very Dark Orange qualifiers. IDs and naming are proposed implementation details, not previously existing registrations.

Replace all three UHV dependencies in the controller definition (appearance block, frame predicate, model texture) together. Keep the research-station controller overlay and existing dimensions/part limits. Add `PartAbility.PARALLEL_HATCH` as an optional substitution with maximum one. Verify formed part appearance separately from the controller.

Alternative rejected: a broad casing tag accepting all variants would violate the black-only frame contract. Keeping UHV as a fallback would violate the explicitly approved break in compatibility.

### 2. Pixel-edit a dark plate and animate a separate emissive mask

Author native-resolution raster textures using direct palette/pixel edits guided by the Industrial Steam Casing reference, not a generated replacement image or smooth vector scaling. Keep border/bolt geometry recognizable, retain subtle near-black contrast for black, and use dark saturated purple/green plus a darker burnt-orange palette for colored variants. Keep the orange noticeably darker than an ordinary orange recolor. Document provenance and check applicable asset licensing if any reference pixels are reused; prefer independently drawn pixels following the reference layout.

Use compact vertical PNG frame strips with explicit animation metadata. A black shimmer cycle totals 200 texture ticks and each colored cycle totals 40; ordinary recipes use 100 ticks. Use repeated frame indices/long frame durations for quiet periods rather than allocating hundreds of duplicated frames. Exact pulse width and pixel palette are minor art-tuning details: retain a short readable shimmer, no constant bright fill. Colored base frames shift interior shadow pixels subtly, keeping outline and bolt positions fixed; align their loop with the 40-tick shimmer cycle for compact resources.

Composite normally shaded base plates with transparent-background emissive detail textures using the existing LDLib emissive route. Keep frame dimensions and timing aligned across base and emissive layers, and render the same asset family on block items and controller/formed-part casing surfaces. Do not mark the entire base texture emissive. Prefer existing model composition and metadata; if pinned model layering cannot express this, resolve the narrow resource/model composition issue before introducing a custom renderer. No mixin, dependency upgrade, or optional shader is planned.

Alternative rejected: changing block light levels would introduce server/world updates and would not be texture-only. Recoloring a static plate alone would not satisfy emissive shimmer or moving painted shadows.

### 3. Separate state validation from survey execution

Retain a common check for exact recipe type and valid matching `required_state`. Preserve the stricter survey acceptance rules (information-collection operation, DORMANT, 2400 ticks, no tick capabilities, parallel/batch/subtick counts exactly one). Add a distinct ACTIVE casing-processing operation marker, proposed as `casing_conversion`, to the three new recipes so survey logic cannot accidentally run for them.

Route non-survey ACTIVE work through the native `RecipeLogic` implementations for setup, working, completion, and suspension/resumption; retain the current survey path for scan setup, ticking, chunk retries, pending book delivery and activation. Audit all overridden entry points together, including `setWorkingEnabled`, `onRecipeFinish`, reset/interruption, server ticking, save/load, and UI progress. In particular, ACTIVE recipes have a native running job but no scan domain: they must resume as working rather than idle and finish through normal output delivery.

Do not replace the native persisted running-job state with a new custom job record. Keep valid version-1 `WorldEaterTamer` progression and scan data readable and keep corruption rejection. Only survey runs populate counts/domain/retry/report fields. Display ACTIVE crafting progress without misleading `Scan: .../2400` or a current scan chunk. Preserve the DORMANT report-pending guard and change progression only after successful report delivery.

Alternative rejected: globally removing the DORMANT restriction from `accepts` without dispatch would enter survey-only setup/tick/completion code with casing recipes and break their lifecycle. Duplicating native inventory/output handling would increase conservation risk without benefit.

### 4. Apply the native hatch modifier only to ACTIVE conversions

Attach a small recipe modifier at the controller definition that delegates ACTIVE casing recipes to the pinned `GTRecipeModifiers.PARALLEL_HATCH`/`hatchParallel` route and returns identity for surveys. Validate the declared operation/state before delegation, and retain strict rejection of already-multiplied survey recipes in recipe logic as defense in depth. Do not attach overclock or batch modifiers.

The native hatch controls its normal current parallel setting, and native input/output limits bound actual parallels. This reuses the supported algorithm and scales both input types and output without altering 100-tick duration. A formed machine with the hatch installed must still run an unmodified single survey; merely rejecting a multiplied survey after applying the modifier is insufficient because that would disable activation.

Apply-time source inspection and the native parallel regression found that the standard modifier's `eutMultiplier` synthesizes a zero-EU per-tick capability even when the source recipe has no tick inputs/outputs. Preserve the native modifier and algorithm, then remove its synthetic EU tick entries only on this already-validated no-tick casing conversion path. This retains the power-free/no-tick contract and does not strip energy from arbitrary recipes. Add assertions on the modified recipe's tick capabilities to prevent regression.

Alternative rejected: blindly adding the modifier to every recipe would multiply survey content or cause its strict guard to reject the scan entirely. Hand-written parallel matching would duplicate native capacity and output logic.

### 5. Register exact recipes in the existing addon hook

Increase item input capacity from one to two without changing output/fluid counts. Register three stable addon-owned conversion recipe IDs in `GregEGTAddon.addRecipes`, with explicit `required_state=ACTIVE`, the new operation marker, one black input, 16 `Items.PURPLE_TERRACOTTA`, `Items.GREEN_TERRACOTTA`, or `Items.ORANGE_TERRACOTTA`, and one matching output. Duration is 100 ticks. Like the current power-free sensor recipe, add no energy or fluid capabilities. No recipe produces black casing.

Alternative rejected: broad terracotta tags could admit the wrong colors, glazed blocks, or unrequested variants. An assembler/crafting-table recipe would bypass ACTIVE progression.

## Risks / Trade-offs

- [Custom scan overrides can swallow ACTIVE completion or resume incorrectly] → Exercise real processing, disable/enable, save/load, and blocked-output paths; compare against the existing survey regression suite.
- [The black palette can become unreadable or shimmer can look permanently luminous] → Review at native pixel scale in lit and dark scenes, through full cycles, with base and emissive layers inspected separately.
- [Texture/model layering differs between standalone casing, items, controller and formed hatches] → Verify all surfaces in a native shader-free client and after resource reload; metadata inspection alone is insufficient.
- [UHV frame replacement intentionally invalidates existing structures] → Document rebuild instructions, retain controller ID/progression, and never rewrite existing blocks automatically.
- [Two unarchived changes target the same capability] → Keep the conflict explicit; before archival, establish the earlier baseline and reconcile this change's `world-eater-tamer` delta operations against it. Do not overwrite the older survey requirements or silently archive either change during proposal work.

## Migration Plan

1. During explicitly authorized apply work, implement on a dedicated feature branch without staging unrelated untracked OpenSpec or Hermes files.
2. Ship new registrations/resources and the controller/recipe changes together. Explain that the base black casing is creative-only and old UHV frames require replacement with black.
3. Preserve controller identity and valid saved progression. A retained completed report remains pending while the frame is invalid and can deliver after a valid rebuild; it must not be recreated or duplicated.
4. Run existing and new native GameTests plus client checks, and inspect fresh machine-readable reports rather than trusting exit status alone. Reconcile the earlier OpenSpec delta before archival.
5. For rollback after installing a build with new blocks, restore a pre-update backup as well as the previous mod JAR; simply downgrading a world containing new casing IDs is not a safe data-preserving rollback.
