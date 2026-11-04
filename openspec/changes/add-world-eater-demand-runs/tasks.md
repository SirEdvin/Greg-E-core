# Tasks

## 1. Native materials and the demand pool

- [x] 1.1 Register addon-owned Desarium/Implementium gem materials and `Tamed world eater essense` ingot material in `GreggyItems`, with the agreed icons/colors, generated names/resources, and primary forms in `ModCreativeModTabs`; add native registration assertions for IDs, generated form identities, and visible names, and verify no token acquisition or world-generation recipe is introduced.
- [x] 1.2 Wire only the missing native material-form flags through `GregECore`'s material lifecycle (Aluminium/Neutronium rotors and RhodiumPlatedPalladium/Darmstadtium gears); add assertions that every specified material/tier has its normal gear, long rod, and rotor plus native processing routes, and verify no UEV material entry or substitute form is added.
- [x] 1.3 Add a small tamer-local `(item, tier)` pool using the eight named component families through UEV and the specified material forms through UHV, resolving registrations only when ready; test full, absent, and partially present optional entries, empty-pool rejection, quantity bounds, and every tier's `600 * tier` deadline without changing high-tier configuration or requiring the developer runtime's enabled registrations.

## 2. Progression, recipe registration, and paid startup

- [x] 2.1 Extend `WorldEaterTamerRecipeLogic` with permanent PREPARED and explicit operation routing, and update `IncompleteWorldEaterTamer`'s casing-only modifier; extend existing run/casing/intention fixtures to verify PREPARED casing parallelism and Bravery acquisition, ACTIVE-only preparation, DORMANT survey isolation, and unchanged earned progression across tier/hatch changes.
- [x] 2.2 Register the Desarium test and Implementium real recipes in `GregEGTAddon`, add their operation captions and state/tier/intention diagnostics in the existing recipe/presentation paths, and verify native recipe lookup/serialization accepts the exact token and metadata while rejecting missing/wrong state, tier, intention, power/tick capabilities, and multiplied demand recipes without payment.
- [x] 2.3 Implement paid demand startup in the existing logic: check output capacity/pool/hardware first, consume one token through native setup, then choose and retain one demand and its deadline; add focused demand GameTests verifying no reroll during candidate checks, no survey initialization, one token with a parallel hatch and surplus tokens present, and a later run paying/selecting independently.

## 3. Immediate payment, retained outcomes, and persistence

- [x] 3.1 Implement full-quantity simulation followed by exact native input handling across all attached item buses before each deadline check; test correct items split across slots/buses (including a bus other than the token's input group), surplus preservation, no partial payment, wrong item/form/tier rejection, immediate early success, and successful payment on the final allowed tick.
- [x] 3.2 Settle test success as PREPARED without an item output and real success as exactly one earned essence ingot, retaining blocked delivery using the existing completed-result pattern; test one-time demand payment, blocked output followed by delivery, suspension, structural reset after earned success, and duplicate finish callbacks without additional payment/output/punishment.
- [x] 3.3 Persist the new run data additively beside version-1 survey data and validate it independently of valid earned progression; extend save/load and unload fixtures to cover the same demand/deadline without repayment or reroll, pending reward delivery once, absent optional items, corrupt count/tier/deadline/operation data, and legacy DORMANT/ACTIVE paid work. Verify real unload pauses time and all malformed new work cancels harmlessly without granting rewards or revoking valid PREPARED.
- [x] 3.4 Separate harmless interruption/test timeout from valid real normal expiry, clearing settled run data before any consequence; test frame break, intention removal, cancellation, lost eligibility, suspended processing, and stale callbacks. Verify each preserves earned progression, creates no punishment or unearned output, leaves unpaid demand items untouched, and does not refund the spent token.

## 4. Bounded intention consequences

- [x] 4.1 Implement Bravery target selection using `WorldEaterScanDomain` bounds, a random origin, current loaded-chunk block entities, and shared survey eligibility; add deterministic-origin tests for nearest selection and stable ties, boundary/build-height limits, exclusion of this tamer and parts, empty regions, and an actually unloaded chunk with no new load/ticket or deferred retry.
- [x] 4.2 Implement real-timeout settlement followed by removal of only the attached Safety Hatch or one eligible Bravery victim, with native block lifecycle and cosmetic explosion particles/sound; test neighboring machines/terrain, victim multiblock parts, entities, and fire remain unaffected, PREPARED survives Safety invalidation, a lost victim is harmless, and repeated callbacks never cause another removal.
- [x] 4.3 Update intention-hatch and tamer help in the existing registration/language paths to explain test safety, the two timeout-only consequences, intentional harmless cancellation, and single-victim Bravery; extend existing presentation assertions to reject obsolete future-only/five-victim wording while retaining placement, acquisition, and fixed-variant instructions.

## 5. World-space display and native recipe presentation

- [x] 5.1 Synchronize server-owned demand presentation and extend `WorldEaterPartRender` with the centered native item, readable item display name, and numerical quantity together, correct interior render bounds, and cleanup; extend native client fixtures to verify readable names (including long component/material names), all supported horizontal orientations, pause/reconnect/resource reload, and clearing after success/timeout/cancellation while retaining connected dominant-color casing rendering.
- [x] 5.2 Extend the existing native progress/UI and `GregERecipeTypes` captions for PREPARED, demanded item/count, remaining deadline, and pending reward; verify rendered text is readable and state/tier/intention rejection messages remain accurate without adding a replacement GUI or client-authoritative state.
- [x] 5.3 Extend `WorldEaterClientChecks`/native UI and intention JEI fixtures for both exact new recipes and material forms; verify the test's textual PREPARED result, real ingot output, variable tier deadline rather than a mandatory wait, power-free single-run behavior, and unchanged existing recipe/preview presentation. Keep captures outside the repository and inspect the machine-readable client result and failure list.

## 6. Cross-cutting verification and delivery evidence

- [x] 6.1 Run `./gradlew --no-daemon compileJava compileGameTestJava build` and `./gradlew --no-daemon runGameTestServer` with the pinned dependency set; verify fresh discovered tests and XML outcomes cover both new scenarios and existing survey/casing/structure/intention/unload regressions, and verify dedicated-server loading does not reach client-only rendering classes. Do not treat exit zero alone as a passing runtime result.
- [x] 6.2 Run the repository's native client verification entrypoint with the updated demand fixtures in its isolated verification world, inspect actual screenshots and the fresh result report, and inspect the built JAR for the new registrations/resources and absence of GameTest classes. Record commands, discovered/passed/failed results, artifact paths, optional-registration coverage, and any unverified behavior in the implementation handoff; rerun strict OpenSpec validation and review the final diff for unrelated configuration/dependency changes.

## Workflow follow-up

- Completed implementation checks and their runtime evidence are recorded in `verification.md`.
- When archival is separately requested, synchronize/archive the completed `add-world-eater-intention-hatches` predecessor before archiving this change's dependent deltas. Until then, strict validation can report expected archive-baseline information for its not-yet-main requirements.
- Back up worlds before deployment; token acquisition remains the modpack's responsibility, and downgrade after using new materials/PREPARED requires the matching older world backup.
