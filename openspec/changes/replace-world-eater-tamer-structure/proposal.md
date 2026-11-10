# Proposal

## Why

The 5×5×5 edge skeleton was a placeholder. The tamer now gets its intended 15×15×15 appearance built from World Eater Tamer Casing, GregTech casings, laminated glass, superconducting coils and engine intakes.

## What Changes

- Replace the tamer pattern with the fixed 15×15×15 layout (controller at the front-face center, replacing that face's assembly engine intake). Pattern symbols are mnemonic: `K` controller, `C` frame casing, `T` tier ring, `I` intention slot, space for unrestricted.
- Frame casing positions accept only the black World Eater Tamer Casing and are the only positions where buses, the parallel hatch and the ME Demand Hatch may substitute (unchanged limits).
- An inner tier ring of 44 World Eater casings (where the reference design had shock proof cutting casing) starts black and is upgraded with colored casings: any black ring casing gives tier 1, an all-colored ring gives tier 2. The controller and formed parts always stay black; the color-voted appearance and its `casing_color` model state are removed.
- The optional Safety/Bravery hatch replaces the rear-face center assembly engine intake, opposite the controller.
- The geometric structure center (scan particles, demand display) moves to seven blocks behind the controller.
- GameTests and opt-in client checks build the new structure; tier-2 coverage recolors the tier ring.

## Capabilities

### Modified Capabilities

- `world-eater-tamer`: new structure, black-only frame, tier ring, intention position and part positions.

## Impact

- Archive after `add-world-eater-me-demand-hatch`: this change removes requirements that change modifies.
- `IncompleteWorldEaterTamer` pattern, frame and tier-ring predicates, tooltips and structure center; `IntentionHatchPartMachine` placement tooltip.
- Saved worlds: existing 5×5×5 tamers no longer form until rebuilt in the new layout. Saved progression is unaffected.
