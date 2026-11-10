# Verification

Executed on branch `feat/world-eater-tamer-large-structure`:

- `./gradlew --no-daemon compileJava compileGameTestJava`: success.
- `JAVA_TOOL_OPTIONS=-Xmx8G ./gradlew --no-daemon runGameTestServer`: "All 76 required tests passed" (AE2 loaded, so ME demand placement, ownership and integration ran), including tier-ring upgrades, Bravery crafting and demand runs. Tests run one per batch on a fresh, flat, peaceful test world (deleted before each run); `-PgameTests=methodA,methodB` runs a subset.
- Round 1 (black-only frame, before the tier ring): opt-in native client (`-PworldEaterClientVerification`) and casing client (`-PworldEaterCasingVerification`) checks passed. They have not been re-run since the tier ring and always-black controller/parts change.
- `python -m unittest discover -s src/gameTest -p 'test_*.py'`: 9 tests OK.

Test fixtures form and invalidate structures through `WorldEaterStructureTests.formed`/`invalidated`, which mirror GTCEu's own formation (pattern lock, block-change mapping). Forming outside the lock races GTCEu's async re-check, and an invalidation that still has a mapping can re-form inline and leave a formed controller without capabilities; both made demand tests fail intermittently.
