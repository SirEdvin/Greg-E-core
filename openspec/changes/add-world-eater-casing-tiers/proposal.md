# Proposal

## Why

The small World Eater tamer can manufacture colored casings but currently rejects them in its own frame. Let players upgrade the existing structure to tier 2 by replacing every black frame casing, without losing the controller's earned DORMANT/ACTIVE progression.

## What Changes

- Accept the existing black, dark purple, dark green, and very dark orange casings in the unchanged small frame, including arbitrary mixtures.
- Derive a reversible structure tier: any actual black frame casing means tier 1; exclusively colored frame casings mean tier 2. Reintroducing black downgrades the machine to tier 1.
- Exclude the controller, permitted buses/hatches, unrestricted face panels, and interior blocks from casing-tier calculation.
- Use GTCEu's native `MultiblockState` match context during pattern matching, following the EBF coil-data lifecycle without its identical-coil restriction.
- Preserve valid DORMANT/ACTIVE progression through casing replacement, structural invalidation, reformation, and save/load while retaining the existing controller.
- Update the black-only tooltip/documentation and structure regressions. Keep existing dimensions, part limits, appearances, activation, recipes, and recipe-interruption behavior unchanged.

Non-goals: new recipes or tier-based recipe gates, a permanent tier unlock, the larger tamer, new casing registrations/assets, controller recoloring, a new UI, dependency upgrades, or new recovery behavior for unfinished recipes after a frame break.

## Capabilities

### New Capabilities

- `world-eater-tamer`: Describe mixed-color frame acceptance, reversible casing-derived tiers, and independent persistent progression. This reuses the capability path already present in the earlier unarchived changes; it is listed here because the durable `openspec/specs/` inventory is currently empty, not because the machine is new.

### Modified Capabilities

None in the durable spec inventory. The completed `add-world-eater-casings` change still declares a black-only frame and explicitly rejects colored casings. This proposal intentionally supersedes that restriction. Keep both earlier changes untouched during planning; reconcile their baseline and this delta before later spec synchronization/archive so contradictory frame requirements are not retained.

## Impact

- Production scope: `src/main/java/net/cu5tmtp/GregECore/gregstuff/GregMachines/machines/endgame/IncompleteWorldEaterTamer.java` for matching, derived-tier lifecycle/access, and tooltip text. Reuse existing casing registrations and recipe-logic persistence rather than rewriting them.
- Verification scope: existing `src/gameTest/java/net/cu5tmtp/GregECore/gametest/WorldEaterStructureTests.java`, `WorldEaterRunTests.java`, `WorldEaterTamerTests.java`, `WorldEaterCasingTests.java`, and reload fixtures as needed for focused assertions and regression runs.
- Documentation: `docs/world-eater-casings.md` for acceptance, upgrade/downgrade rules, and progression-versus-running-recipe semantics.
- Compatibility: existing black frames stay valid as tier 1; registry IDs and valid version-1 progression data remain unchanged. UHV casing and unsupported parts remain rejected. No block conversion or save migration is required.
- Dependencies remain Minecraft 1.20.1 / Forge / Java 17 / GTCEu 7.5.2 / LDLib 1.0.40.b. No new dependency, mixin, recipe schema, or global service is needed.
