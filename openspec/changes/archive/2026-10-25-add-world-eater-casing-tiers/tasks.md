# Tasks

Implementation starts only after a separate apply request. Keep every task unchecked until its stated evidence exists. Use the existing GameTest fixtures and a disposable verification world; run Gradle with `--no-daemon`.

## 1. Deliver native mixed-casing tiers

- [x] 1.1 Update the colored-casing rejection portion of `src/gameTest/java/net/cu5tmtp/GregECore/gametest/WorldEaterStructureTests.java` into acceptance cases for black, each colored variant, and mixed frames while retaining UHV rejection; run the native fixture first and capture an assertion failure caused by current black-only matching, not a setup or compilation error.
- [x] 1.2 In `src/main/java/net/cu5tmtp/GregECore/gregstuff/GregMachines/machines/endgame/IncompleteWorldEaterTamer.java`, implement the casing-only minimum-tier match-context predicate, native preview candidates, and formation-owned runtime tier/accessor from the design; verify black and partially colored frames return 1, uniform/mixed colored frames return 2, invalid structures return the unavailable sentinel, and all supported orientations agree.
- [x] 1.3 Extend the existing structure fixture to verify tier independence from controller/part appearance, permitted buses/hatches, and unrestricted face/interior blocks; exercise black casings at different traversal positions, repeated/failed checks, all existing part limits, missing edges/corners, and unsupported blocks, and confirm these assertions pass without weakening the old rejection/limit cases.
- [x] 1.4 Replace the black-only controller tooltip and update `docs/world-eater-casings.md` with mixed-color acceptance, reversible tier rules, unchanged acquisition/appearance, and progression-versus-unfinished-recipe semantics; verify the delivered text matches the spec and no obsolete colored-frame rejection remains in current player guidance.

## 2. Verify real lifecycle and progression compatibility

- [x] 2.1 Reuse the structure/run fixtures to form an ACTIVE machine and replace actual world blocks through black, partial-color, all-color, another color, black again, broken, and repaired frames; let native block-change formation callbacks update the controller and assert the expected operative tier and unchanged ACTIVE state at each stage. Repeat the progression-sensitive cases for DORMANT and verify a fresh controller in a colored frame starts DORMANT.
- [x] 2.2 Extend the existing progression/reload fixtures, including `WorldEaterTamerTests.java` and `WorldEaterUnloadTests.java` as appropriate, to verify valid version-1 saves without tier data, fresh controller reload with unchanged or changed frame composition, and save/reload while unformed; require actual restored progression plus recomputed tier, and retain passing malformed-save rejection tests. Do not add persistent tier data or relax validation to make a fixture pass.
- [x] 2.3 Extend `WorldEaterRunTests.java` and `WorldEaterCasingTests.java` only as needed to exercise existing surveys/conversions on tier-2 frames, existing state gates and parallel rules at both tiers, and a completed blocked report surviving a colored rebuild and delivering once; verify unchanged payments, durations, activation threshold, and unfinished-recipe cancellation. Check that the documentation from 1.4 accurately distinguishes retained ACTIVE from interrupted work.

## 3. Integrated verification

- [x] 3.1 Run `bash gradlew compileJava compileGameTestJava build --no-daemon` and `bash gradlew runGameTestServer --no-daemon` using the disposable test environment; inspect fresh machine-readable results for the new cases and existing structure, survey, persistence, casing, and parallel regressions, with no required failures. Record commands, expected test names, actual results, and any environment blocker rather than treating exit zero or stale reports as proof.
- [x] 3.2 Check the native client's structure preview/casing candidates and revised tooltip using the existing opt-in client verification setup or a disposable interactive session; verify casing selection and supported part previews remain usable and no black-only instruction is shown. Review the final diff for unchanged registry IDs, save format, recipes, assets, dependencies, and unrelated files, and keep verification captures outside the repository.

## Workflow follow-up

- Before a separately requested spec synchronization/archive, consolidate the earlier unarchived `world-eater-tamer` baseline and reconcile this delta's replacement for the black-only frame requirement. Preserve the earlier survey/parallel/casing requirements instead of leaving contradictory acceptance rules. Do not archive or rewrite earlier changes as part of this implementation checklist.
