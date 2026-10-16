# World Eater casing verification

Verified October 16, 2026 on the project's pinned Minecraft 1.20.1 / Forge
47.4.10 / Java 17 / GTCEu 7.5.2 / LDLib 1.0.40.b stack.

## Original implementation execution

- `bash gradlew tasks --all --no-daemon` confirmed the available build, datagen,
  native GameTest, and client run tasks. The wrapper lacks executable permission
  in this checkout; it was invoked through bash without changing file modes.
- `bash gradlew runData --no-daemon` passed. Generated blockstates, item models,
  loot, mining tags, names and the controller model were inspected. Unrelated
  generated translation/cache churn was removed while preserving existing text.
- `bash gradlew build compileGameTestJava --no-daemon` passed after the final
  recipe-caption change. The final production build took 9 seconds.
- `PYTHONDONTWRITEBYTECODE=1 python -m unittest discover -s src/gameTest -p 'test_*.py'`
  passed all three asset tests: timing/frame bounds, fixed borders/bolts and
  shadow movement, and transparent emissive-detail separation.
- `bash gradlew runGameTestServer --no-daemon` passed in a fresh disposable world
  after the final changes (4 minutes 3 seconds). Its fresh XML contains exactly
  the 20 expected annotated tests, with no missing, duplicate, unexpected, or
  failed cases. The server independently reports all 20 required tests passed.
- Native client acceptance passed under private Xvfb with software OpenGL,
  using an explicitly prefix-guarded copy of an existing disposable verification
  world, not the user's play world. Its final report has `passed=true`,
  `gameplayDone=true`, `resourceReload=true`, an empty failure list, and 43
  screenshots. The native client run exited zero; its machine-readable report,
  not the exit code alone, establishes the assertions.
- Strict OpenSpec validation and `git diff --check` pass. No project Minecraft
  client or server process remains running.

Client invocation (WORLD is the disposable world name):

    LIBGL_ALWAYS_SOFTWARE=1 xvfb-run -a -s '-screen 0 1280x900x24' \
      bash gradlew runClient --no-daemon \
      -PworldEaterClientVerification -PworldEaterCasingVerification \
      -PworldEaterClientWorld="$WORLD"

## Acceptance mapping

| Area | Evidence |
| --- | --- |
| Artwork and animation | Original pixel-authored 16px plates; inspected nearest-neighbor contact sheet. Asset tests verify 200-tick black and 40-tick colored loops, stable border/bolt pixels and three colored shadow poses. Native lit/dark/reloaded screenshots show surface shimmer without brightening the environment. |
| Registration, creative, placement, loot | `casing_registration_and_loot`: all four creative entries and readable names; real BlockItem placement consumes one item; native loot returns the same variant; no vanilla or GT recipe produces black casing. |
| Black-only structure | `structure_limits` and `rotated_structure_and_creative_tab`: edge/corner coverage, rotation, arbitrary/empty faces/interior, UHV and colored rejection, retained independent item/fluid limits and energy-hatch rejection. |
| Hatch support | `hatch_limits_and_parallel_crafts`: standard hatch attachment, current setting, input/output limits and second-hatch rejection. Client inspects actual formed bus/hatch casing quads for the black texture before and after resource reload. |
| Exact ACTIVE recipes | `all_colors_and_state_gates`: all three loaded recipe definitions, two inputs/one output, no EU/fluid/tick capability, exact payment and 100-tick completion, DORMANT and malformed-state rejection, wrong/missing/insufficient input rejection. |
| Persistence and output safety | `active_resume_and_blocked_output`: blocked startup, immediate suspension, mid-craft native NBT round trip, completion blocked after payment, reload while blocked, single delivery when capacity returns, and no survey side effects. Parallel crafting also resumes through native NBT. |
| Activation protection | `parallel_hatch_never_multiplies_survey`: surplus sensors, one consumed sensor, one 2400-tick survey, one retained report across invalidation/UHV rejection/black rebuild, and pre-multiplied survey rejection. Native client performs a real 500-machine survey with a parallel hatch, becomes ACTIVE, then makes all three colors including a parallel conversion. |
| Existing scan guarantees | All original scan/retry/metadata/report tests remain passing, including real controller eviction/reload and target-chunk eviction/non-retention. |
| Recipe viewer | Final `recipe-0940.png` visibly shows one black casing, 16 purple terracotta, one purple casing, `Duration: 5 secs`, `Required state: ACTIVE`, and the fully visible `Casing conversion` caption. Public JEI API targets the exact machine category and recipe. |
| Packaging | All 33 casing/model/generated resources match source bytes in the final JAR; no GameTest classes or Python scripts are packaged. |

## Issues caught and resolved

- Reusing a previous GameTest world contaminated existing absolute-coordinate
  scan fixtures. Subsequent acceptance runs used fresh worlds; older disposable
  worlds were moved into profile scratch rather than deleted.
- GTCEu's normal parallel modifier synthesizes a zero-EU tick capability. The
  tamer retains its native capacity algorithm and removes only the synthetic
  energy tick entries on its already-validated no-tick casing path. Native
  single/parallel regression tests prove power-free startup and exact scaling.
- GTCEu native completion does not retain outputs on a failed output operation.
  A narrow deterministic output-capacity preflight keeps the paid native job
  pending when capacity fills mid-run. This is not a general transactional
  guarantee for arbitrary third-party handlers that violate simulation behavior.
- An output-focused JEI lookup initially selected the block-tags category; the
  fixture now explicitly selects the actual tamer recipe. Screenshot inspection
  also caught and fixed the clipped internal operation identifier by displaying
  a concise caption without modifying recipe metadata.

## Original artifact and evidence (superseded below)

Final JAR:

    /home/siredvin/projects/Greg-E-core/build/libs/gregecore-1.12.7-1.20.1.jar

Size: 888082 bytes.

SHA-256:

    23f8105cc76b77f54c80e9e156066a2d6a0429e063219a710d11f9bd5c19f72a

Evidence under `/home/siredvin/.hermes/profiles/albina/cache/scratch/`:

- `world-eater-final-build.log`
- `world-eater-datagen.log`
- `world-eater-final-gametest.log`
- `world-eater-gametest.xml`
- `world-eater-casings-client.log`
- `world-eater-casings-client/result.json`
- `world-eater-casings-client/screenshots/`
- `world-eater-casings/pixel-contact-sheet.png`

The headless client reports an unavailable audio device; this feature adds no
sounds and no audio acceptance is claimed. Shader-specific bloom and arbitrary
third-party inventory handlers were not tested. Emissive appearance was verified
without optional shaders, with the standard GTCEu buses/hatch. Scratch evidence
can expire; this document retains the acceptance mapping and artifact checksum.

The original verification performed no dependency upgrade, survival black-casing
recipe, automatic world conversion, commit, push, or OpenSpec archive. Before archival, reconcile the
older completed tamer change with this change's shared capability as described
in proposal.md and tasks.md. Back up worlds and replace UHV frame casing with
black; valid retained controller progression is preserved.

## Connected-texture and full-sweep correction — October 16, 2026

User feedback identified two gaps in the original acceptance: casings did not
connect, and the emissive sweep stopped near the center. The initial design
incorrectly excluded the reference casing's connectivity without confirmation.
The design, specification and affected tasks were reopened for correction.

### Corrected resources and regression checks

- Both base and emissive source sprites now declare `ldlib.connection`, each
  referencing an original pixel-authored animated 32px CTM sheet in LDLib's
  native four-tile layout. The normal 16px sprite remains the isolated fallback.
- Source and connected sprites share frame indices and durations: black remains
  200 ticks, each colored variant 40 ticks. Colored shadows still shift subtly;
  base plates remain shaded and only detail pixels are emissive.
- The old shimmer stopped at diagonal position 16. The highlight now traverses
  the full writable diagonal, reaching the far corner before the quiet reset.
- The two new regressions failed on the original assets: missing connection
  metadata and an unlit far corner. All five Python asset tests now pass,
  including both ends of the source and connected shimmer sweeps, exact timing,
  synchronized CTM sheets, fixed frame geometry and emissive separation.
- `bash gradlew build compileGameTestJava --no-daemon` passed. All 32 texture
  resources (PNG strips and metadata) byte-match the production JAR. The JAR
  contains neither GameTest classes nor Python scripts.
- Re-ran `bash gradlew runGameTestServer --no-daemon` in a fresh disposable
  server world after the correction: passed in 3 minutes 11 seconds. Parsed
  the new XML and compared normalized case names against the source annotations:
  exactly 20 expected tests, no missing/unexpected/duplicate cases and zero
  failures or errors. The previous disposable server world was preserved in
  profile scratch, not deleted.

### Fresh native client acceptance

Ran the existing opt-in acceptance with software OpenGL/Xvfb in a new disposable
verification world with fresh region data. The previous report and world were
preserved outside the production artifact. The process exited zero and the
fresh report contains `passed=true`, `failures=[]`, `gameplayDone=true` and
`resourceReload=true`. All 190 reported screenshots exist on disk.

`WorldEaterCasingConnectionChecks` exercises actual world-context baked models
with `getModelData` and the five-argument `getQuads`, before and after reload:

- Filled panels use four connected quadrants for each base/emissive layer.
- All four variants select connected sprites on all six face normals.
- Connected emissive quadrants retain native fullbright lighting.
- Horizontal and vertical strips use the expected native submap indices.
- Missing-diagonal L shapes retain the required concave corner and exterior
  quadrants continue using the original framed sprite.
- Different colors do not connect.
- The black controller, parallel hatch and item buses select connected black
  casing appearances after formation.

Inspected real lit/dark native screenshots: 3×3 panels have no internal frame or
bolt grid, their outer borders remain, highlights stay emissive in darkness,
and no missing textures are present. The source frame contact sheet also shows
the sweep continuing past the middle to the far corner. Existing native client
gameplay checks still activate through a hatch-installed 500-machine survey,
craft all three colors and complete a parallel conversion and JEI inspection.

### Updated artifact and evidence

JAR: `/home/siredvin/projects/Greg-E-core/build/libs/gregecore-1.12.7-1.20.1.jar`

Size: 899971 bytes.

SHA-256: `d736b7972c516c17ce36fa46794b358adb4f8aef451108d42be02b5247e4546c`

Evidence under profile scratch:

- `world-eater-ctm-build.log`
- `world-eater-ctm-client.log`
- `world-eater-ctm-gametest.log`
- `world-eater-gametest.xml`
- `world-eater-casings-client/result.json`
- `world-eater-casings-client/screenshots/lit-0334.png`
- `world-eater-casings-client/screenshots/dark-0598.png`
- `world-eater-casings/complete-shimmer.png`
- `world-eater-casings/purple-ctm-layout.png`

The correction uses the existing native renderer and adds no production Java,
dependency, mixin or world tick logic. No commit, push or OpenSpec archive was
performed for this correction.
