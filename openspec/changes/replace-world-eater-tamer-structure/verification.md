# Verification

Executed on branch `feat/world-eater-tamer-large-structure`:

- `./gradlew --no-daemon compileJava compileGameTestJava`: success.
- `JAVA_TOOL_OPTIONS=-Xmx8G ./gradlew --no-daemon runGameTestServer`: "All 40 required tests passed"; the JUnit report lists 40 cases with no failures (AE2 loaded, so ME demand placement and ownership ran).
- Opt-in native client (`-PworldEaterClientVerification`, disposable `albina-world-eater-verification-structure-*` world, Xvfb, software GL): `passed=true`, empty failures, native UI, JEI structure preview (one black casing candidate, rear intake/intention slot 14 blocks behind the controller), scan particle to the new center, intention reload/reconnect.
- Opt-in casing client (`-PworldEaterCasingVerification`, disposable `albina-world-eater-verification-casings-*` world): `passed=true`, gameplay, 62 screenshots, resource reload.
- `python -m unittest discover -s src/gameTest -p 'test_*.py'`: 9 tests OK.

The GameTest server world under `run-gametest/world` persists between runs; a run after changing the test list should start from a fresh world, otherwise blocks left by earlier test layouts can sit inside scan areas.
