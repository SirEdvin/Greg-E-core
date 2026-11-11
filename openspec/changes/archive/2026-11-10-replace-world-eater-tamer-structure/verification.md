# Verification

Executed on branch `feat/world-eater-tamer-large-structure`:

- `./gradlew --no-daemon compileJava compileGameTestJava`: success.
- `JAVA_TOOL_OPTIONS=-Xmx8G ./gradlew --no-daemon runGameTestServer`: "All 76 required tests passed" (AE2 loaded, so ME demand placement, ownership and integration ran), including tier-ring upgrades, Bravery crafting and demand runs. Tests run one per batch on a fresh, flat, peaceful test world (deleted before each run); `-PgameTests=methodA,methodB` runs a subset.
- Round 1 (black-only frame, before the tier ring): opt-in native client (`-PworldEaterClientVerification`) and casing client (`-PworldEaterCasingVerification`) checks passed.
- `python -m unittest discover -s src/gameTest -p 'test_*.py'`: 8 of 9 passed. `test_controller_color_states_preserve_workable_overlays` still expected the removed `casing_color` controller state.

Follow-up on branch `fix/world-eater-review-findings`, after the tier ring and always-black controller/parts change:

- Full `runGameTestServer`: 76 discovered, 76 passed, no failures or errors in the fresh XML. It ran before a final wording change to one controller tooltip line; the client run below covers the final text.
- Native client (`-PworldEaterClientVerification`, fresh isolated world): `passed=true`, no failures, 103 pages, recipe reload, resource reload and reconnect flags true, ME checks passed, 57 captures. Rear face, controller and controller tooltip captures were inspected.
- Casing client (`-PworldEaterCasingVerification`, fresh isolated world): `passed=true`, no failures, resource reload true, 95 captures.
- Python asset tests: 9 of 9 passed after the controller-model test was changed to expect black for every state.

Test fixtures form and invalidate structures through `WorldEaterStructureTests.formed`/`invalidated`, which mirror GTCEu's own formation (pattern lock, block-change mapping). Forming outside the lock races GTCEu's async re-check, and an invalidation that still has a mapping can re-form inline and leave a formed controller without capabilities; both made demand tests fail intermittently.
