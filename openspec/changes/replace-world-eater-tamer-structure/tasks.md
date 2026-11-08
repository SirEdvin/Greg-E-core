# Tasks

## 1. Structure

- [x] 1.1 Replace the `IncompleteWorldEaterTamer` pattern with the shared 15×15×15 `SHAPE` (controller `S`, rear intention slot `I`), black-only frame casing and the requested GregTech/GregECore blocks.
- [x] 1.2 Move the structure center, tooltips and intention-hatch placement help to the new geometry.

## 2. Verification

- [x] 2.1 Rebuild GameTest fixtures from `SHAPE`; cover required blocks, part-only-on-casing positions, colored-casing rejection, rear-slot rules, rotation, overlapping ME demand ownership and scan exclusion.
- [x] 2.2 Remove tier-2-dependent GameTests and client checks; keep tier-1 rejections of tier-2 operations.
- [x] 2.3 `compileJava compileGameTestJava` and the full `runGameTestServer` suite pass.
