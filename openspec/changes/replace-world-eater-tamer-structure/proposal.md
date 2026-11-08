# Proposal

## Why

The 5×5×5 edge skeleton was a placeholder. The tamer now gets its intended 15×15×15 appearance built from World Eater Tamer Casing, GregTech casings, laminated glass, superconducting coils and engine intakes.

## What Changes

- Replace the tamer pattern with the fixed 15×15×15 layout (controller at the front-face center, replacing that face's assembly engine intake).
- World Eater casing positions accept only the black World Eater Tamer Casing. Colored casings no longer form a frame, so casing tier 2 is unreachable; its code stays dormant.
- Item/fluid buses, the parallel hatch and the ME Demand Hatch may replace only World Eater casing positions, with unchanged limits.
- The optional Safety/Bravery hatch replaces the rear-face center assembly engine intake, opposite the controller.
- The geometric structure center (scan particles, demand display) moves to seven blocks behind the controller.
- GameTests and opt-in client checks that need tier 2 (Bravery crafting, demand runs, ME demand integration, casing-color appearance) are removed; the remaining fixtures build the new structure.

## Capabilities

### Modified Capabilities

- `world-eater-tamer`: new structure, black-only frame, dormant tier 2, intention position and part positions.

## Impact

- Archive after `add-world-eater-me-demand-hatch`: this change removes the requirements that change modifies. Its verification cites ME demand GameTests that this change deletes because they need tier 2.

- `IncompleteWorldEaterTamer` pattern, frame predicate, tooltips and structure center; `IntentionHatchPartMachine` placement tooltip.
- Saved worlds: existing 5×5×5 tamers no longer form until rebuilt in the new layout; colored-frame tamers lose tier 2. Saved progression is unaffected.
