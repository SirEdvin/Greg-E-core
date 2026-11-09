# Tasks

## 1. Structure

- [x] 1.1 Replace the `IncompleteWorldEaterTamer` pattern with the shared 15×15×15 `SHAPE` using mnemonic symbols, a black-only frame and the requested GregTech/GregECore blocks.
- [x] 1.2 Replace shock proof cutting casing with a World Eater tier ring whose all-colored state gives casing tier 2.
- [x] 1.3 Move the structure center, tooltips and intention-hatch placement help to the new geometry.

## 2. Verification

- [x] 2.1 Rebuild GameTest fixtures from `SHAPE`; cover required blocks, part-only-on-frame positions, colored-frame rejection, tier-ring upgrades/downgrades, rear-slot rules, rotation, overlapping ME demand ownership and scan exclusion.
- [x] 2.2 Keep tier-2 GameTests and client checks (Bravery crafting, demand runs, ME demand integration, always-black appearance) by recoloring the tier ring.
- [x] 2.3 `compileJava compileGameTestJava` and the full `runGameTestServer` suite pass.
- [ ] 2.4 Re-run the opt-in client checks after the tier-ring and always-black appearance change.
