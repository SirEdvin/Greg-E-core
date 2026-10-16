# Proposal

## Why

The World Eater tamer still uses temporary UHV casing and has no useful ACTIVE-state processing. Dedicated animated casings will give it a recognizable identity and its first state-gated manufacturing recipes without weakening the activation survey.

## What Changes

- Introduce four placeable, item-backed casings inspired by GTCEu's Industrial Steam Casing: black, dark purple, dark green, and very dark orange. Author textures through pixel editing, retaining the reference's framed, bolted industrial plate language.
- Give black casing an emissive shimmer every 10 seconds; colored casings shimmer every 2 seconds and have subtly moving, texture-animated shadows.
- Preserve the reference casing's native connected-texture behavior on all four variants, with synchronized base/emissive animation and a shimmer sweep that traverses the full face before resetting.
- **BREAKING**: require the new black casing instead of UHV casing in the existing tamer skeleton and use its appearance for the controller. Neither UHV nor colored casings are accepted as frame casing. Existing UHV structures must be rebuilt; no automatic block conversion is introduced.
- Expose black casing through creative access only; do not introduce a survival acquisition recipe.
- Add three ACTIVE-only, power-free, 5-second recipes: one black casing plus 16 corresponding vanilla terracotta yields one colored casing.
- Accept at most one optional standard GTCEu parallel hatch. Enable normal hatch-based parallel processing for colored-casing recipes only; never parallelize, batch, or accelerate the DORMANT activation survey.
- Preserve existing structure dimensions, independent item/fluid part limits, no-energy operation, scan coverage/retries, report delivery, and progression persistence.

## Capabilities

### New Capabilities

- `world-eater-casings`: Dedicated casing blocks, pixel-art identity, emissive animation, creative access, and ACTIVE-state manufacturing.
- `world-eater-tamer`: Frame casing and optional parallel-hatch contract, state-specific processing, and protected single-run activation survey.

### Modified Capabilities

None in the main spec inventory: `openspec list --specs` currently returns no specs. The completed but unarchived `add-incomplete-world-eater-tamer` change contains the earlier `world-eater-tamer` delta; this change intentionally supersedes its temporary-casing requirement while preserving the survey contract. Reconcile that earlier change before archiving this one rather than silently creating conflicting baseline requirements.

## Impact

- Forge 1.20.1 / Java 17 / GTCEu 7.5.2 / LDLib 1.0.40.b; no dependency upgrade intended.
- Registrate casing registration, creative inventory, localized names, models, textures, loot and mining tags.
- `IncompleteWorldEaterTamer`, `WorldEaterTamerRecipeLogic`, `GregERecipeTypes`, and recipe registration in `GregEGTAddon`.
- Existing server GameTests and opt-in native client verification require new casing fixtures and coverage of ACTIVE processing, hatch limits, and animations.
- Existing saves retain controller identity and progression data, but old UHV frames no longer form. No larger tamer, survival black-casing recipe, colored-frame support, dynamic world lighting, or new progression state is included.
