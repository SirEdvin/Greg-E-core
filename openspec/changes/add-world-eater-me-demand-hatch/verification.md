# ME Demand Hatch verification

Verified November 6, 2026, on the requested existing branch `feat/add-incomplete-world-eater-tamer`.

## Results

- Minecraft 1.20.1, Forge 47.4.10, Java 17 toolchain, GTCEu 7.5.2, LDLib 1.0.40.b, AE2 Forge 15.0.18. Existing Minecraft/Forge/GTCEu/LDLib pins are unchanged.
- `compileJava`, `compileGameTestJava`, `runData`, and `build` passed. Commands use `bash gradlew --no-daemon` because this checkout's wrapper is not executable.
- Final AE2 dedicated-server run: **77 discovered, 77 passed, zero failures/errors**, normal save/shutdown, Gradle exit zero. Compared the report's test-name multiset against source annotations: no missing, unexpected, or duplicate cases.
- No-AE2 dedicated-server run: **77 discovered, 77 passed, zero failures/errors**, Gradle exit zero. This includes actual absent-integration registry/creative/recipe assertions and existing manual tamer regressions. Sixteen AE2-only test entry points deliberately return successfully without exercising AE2 behavior; they are not evidence of network behavior without AE2.
- Existing Python asset regressions: **9 passed**.
- Focused native ME client run: Gradle exit zero; `me-result.json` has `passed=true`, `failures=[]`, `resourceReload=true`, `reconnect=true`, `clearedReconnect=true`, `phase=9`. All **15 ME screenshots** exist outside the repository. Inspected actual captures of offline/online/idle/pending UI, four formed orientations, the exact shapeless recipe, selectable structure preview, and cleared request after reconnect.
- Strict OpenSpec validation passed with no issues. Reviewed all three delta specs: **19 requirements and 59 scenarios**, with the implementation/evidence mapping below.
- Production JAR inspection confirmed optional AE2 metadata, native hatch models/overlay/language resources matching the source, and no GameTest/client-check classes, Python fixture code, or bundled AE2 classes.

## Implementation and coverage map

Production paths below are under `src/main/java/net/cu5tmtp/GregECore/`; fixtures are under `src/gameTest/java/net/cu5tmtp/GregECore/gametest/`.

| Requirements | Implementation | Executed evidence |
| --- | --- | --- |
| Distinct hatch; optional AE2 integration | `MEDemandHatchPartMachine`, guarded `GregEStuffInit`/`ModCreativeModTabs`/`GregEGTAddon`; optional loader metadata | `optionalDemandHatchRegistration` with and without AE2 checks block/item/creative/recipe visibility, native recovery, localized name and automation help. Native client checks item model and formed appearance. |
| Four-ingot shapeless upgrade | `MEDemandHatchPartMachine.addRecipe` through the existing GT addon recipe provider | `craftingConsumptionLayoutsReloadAndNetwork` consumes actual crafting-grid inputs in nine layouts, preserves surplus, rejects insufficient/wrong bus/material/form/extraneous inputs, round-trips the native serializer and repeats after actual server recipe reload. Client opens the exact JEI crafting category/recipe. |
| Initial structure; existing substitutions; optional freely positioned hatch | Separate `DEMAND` ability and zero-or-one frame predicate in `IncompleteWorldEaterTamer` | `meDemandPlacementAndIndependentLimits` covers every ordinary frame position in all horizontal orientations, independent maximum I/O counts, parallel/intention limits, energy rejection, and unattached interior/face hatches; existing structure/color/progression tests remain green. Client preview checks hatch candidates at all 43 non-controller frame positions. |
| One attached controller | Native `canShared=false` and controller-derived request owner | `meDemandOverlappingOwnership` forms two genuinely overlapping frames, rejects the second owner, then transfers attachment only after native invalidation. |
| Controller-owned real request; immediate exact payment; cleanup | Paid-start `getRealDemand`/`handleDemand` path in `WorldEaterTamerRecipeLogic`; derived request and fresh `demandInputs` in the hatch | `realRequestAndExactStoragePayment`, `paymentAliasingDeadlineAndCleanup`, and existing demand tests cover ghost requests, preparation exclusion, local/ME/mixed payments, insufficient/wrong input, actual network deltas, surplus, final-tick priority and blocked reward cleanup. No second independent payment mechanism is introduced. |
| Accessible ME supply; exact startup shortage | Native grid/action source and extraction simulation; deduplicated usable stocking-bus views | `startupShortageAndAbsentHatch` verifies one local plus one ME item yields a one-item startup plan for a three-item demand, in both distinct modes and with an ordinary bus's larger minimum-stock setting. `queuedRunsAndOfflineConsequences` physically disconnects powered storage and proves no partial payment. Native connectivity/channel/access decisions remain delegated to the pinned APIs, not replaced by addon permission logic. |
| Single startup opportunity | Controller's considered marker is set before external work; only paid startup calls `begin` | `startupSkipsAndMatchingJobs`, `unavailableStartupAndFailures`, `startupShortageAndAbsentHatch` cover sufficient supply, no hatch, disabled/offline connection, missing pattern/materials, missing/busy CPU, exceptional calculation, restoration of availability, and no retry. Consecutive paid runs receive new opportunities. |
| Suppress matching scheduled production | Public CPU final-output status plus `isRequesting`, scoped to connected grid | `startupSkipsAndMatchingJobs` verifies a smaller already scheduled final-output job before final dispatch. `awaitedIntermediateAndBusyCpu` uses another requester's real CPU job to distinguish unrelated final output, awaited intermediate demand and later unrelated busy CPU. `sharedGridAndIndependentJobs` verifies separate networks and two tamers on one grid. |
| One guarded asynchronous attempt | Transient pending record ties future to controller, fresh recipe instance, hatch and grid; server-thread submission rechecks supply and duplicates | `asynchronousRacesAndNoRetry` holds completion of a real native plan across full/partial supply, manual payment, interruption, reload, identical consecutive demands, another job, frame invalidation and timeout. It also checks continuing deadline progress and no retry after external cancellation. Shared-grid tests cover network identity changes. |
| Fire-and-forget jobs | `submitJob(plan, null, null, false, actionSource)`; no retained submission result or job link | `nativeCraftDeliveryAndFireAndForget` uses a real CPU and processing pattern, returns output through native storage insertion, verifies exact payment and surplus, manual fulfillment/removal without cancellation, and late output retained in storage without another reward. |
| Demand persistence; independent job lifetime | Existing demand serialization plus considered marker; transient calculations cleared on controller/part unload and invalidation | `legacyPersistenceAndReadOnlyControls`, fresh NBT replays in skip/failure/submission fixtures, blocked-reward tests and existing corrupt-core-demand tests. `nativeHatchEvictionAndReload` and `nativeSubmittedJobEvictionAndReload` require both an unload event and a missing non-loading cache entry, then restore fresh machines; pending planning is not replaced and submitted CPU work survives independently. |
| Help/read-only UI; challenge gates and outcomes | Native read-only component panel, blocked stocking configurators/data-stick/screwdriver paths; unchanged normal tamer gates, deadlines and consequences | Data-stick/configuration fixture, native help assertions, client idle/offline/pending and cleared captures, resource reload/reconnect before and after settlement. `queuedRunsAndOfflineConsequences` verifies native queued tokens and physical-offline Safety/Bravery timeouts; shared-grid fixtures verify suspension/resume. Existing survey, preparation, casing, parallel, report/reward retention and consequence suites remain green. |

Deterministic race tests inspect/gate the addon's own transient pending record and reuse existing controller fixture helpers. Production contains no reflection, mixin, access transformer, custom AE2 requester or job-management registry. The test-only external requester demonstrates behavior of someone else's job; it is not the hatch's submission strategy.

## Reproduction and isolation

The scratch evidence root is:

    /home/siredvin/.hermes/profiles/albina/cache/scratch/me-demand-hatch-verification/

Server invocations use a fresh directory containing these properties:

    level-name=world
    level-type=minecraft:flat
    generator-settings={"layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}],"biome":"minecraft:plains","structures":{}}
    generate-structures=false
    sync-chunk-writes=false

The scratch init script `me-demand-verification.gradle` sets the native ForgeGradle run configuration after project evaluation:

    gradle.projectsEvaluated {
        def project = gradle.rootProject
        if (project.hasProperty('meRegistrationOnly')) {
            project.sourceSets.gameTest.java.setIncludes(['**/WorldEaterMERegistrationTests.java'])
        }
        project.minecraft.runs.gameTestServer.workingDirectory(new File(System.getenv('WORLD_EATER_TEST_DIR')))
        project.minecraft.runs.gameTestServer.jvmArgs '-Xmx6G'
    }

Final server commands:

    WORLD_EATER_TEST_DIR="$TMPDIR/me-demand-hatch-verification/final-foreground-ae2" \
      bash gradlew --no-daemon -I "$TMPDIR/me-demand-verification.gradle" \
      compileJava compileGameTestJava build runGameTestServer

    WORLD_EATER_TEST_DIR="$TMPDIR/me-demand-hatch-verification/final-no-ae2" \
      bash gradlew --no-daemon -PwithAE2=false -I "$TMPDIR/me-demand-verification.gradle" \
      compileJava compileGameTestJava runGameTestServer

Data generation/build and asset checks:

    bash gradlew --no-daemon compileJava compileGameTestJava runData build
    PYTHONDONTWRITEBYTECODE=1 python -m unittest discover -s src/gameTest -p 'test_*.py'
    openspec validate add-world-eater-me-demand-hatch --strict --json
    git diff --check

Datagen's unrelated language/cache churn was discarded. Existing translations were preserved byte-for-byte apart from adding the hatch key (and the missing trailing newline in `en_ud.json`); the final build followed this cleanup.

The focused client uses the existing opt-in client verification entrypoint and an isolated Xvfb display with software rendering:

    WORLD_EATER_CLIENT_DIR="$TMPDIR/me-demand-hatch-verification/client" \
      LIBGL_ALWAYS_SOFTWARE=1 xvfb-run -a -s '-screen 0 1280x900x24' \
      bash gradlew --no-daemon -I "$TMPDIR/me-demand-client.gradle" \
      -PworldEaterClientVerification \
      -PworldEaterClientWorld=albina-world-eater-verification-me-demand runClient

`me-demand-client.gradle` sets the client's working directory from `WORLD_EATER_CLIENT_DIR`, adds `-Xmx6G`, and sets `gregecore.meClientVerification=true`. A disposable verification world's `level.dat` seeded this private world; no production play world was used. The focused ME report is authoritative for this run, not the older suite's `nativeUI/pagesChecked` fields, which are deliberately not exercised by this focused entrypoint.

Acceptance evidence:

- `final-foreground-ae2.log` and `final-foreground-ae2.xml`: complete final AE2 server/build, normal exit.
- `final-no-ae2.log` and `final-no-ae2.xml`: complete absent-AE2 server run, normal exit.
- `build-data.log`: successful data generation and build.
- `client-verified.log`, `client-me-result.json`: successful complete ME client check, normal exit.
- `$TMPDIR/world-eater-client/screenshots/me-*.png`: fifteen native captures. `me-cleared-reconnect.png` verifies the settled request does not return after reconnect.

Scratch can expire; this document retains the commands, acceptance mapping and artifact identity.

## Findings and limits

- Native input setup helpers clear local slots. An initial mixed-startup deficit fixture accidentally removed its own local supply; the fixture now inserts that supply after setup, and the final native plan is verified to request only the actual shortage.
- Deduplication must not mark a grid consumed by an ordinary stocking-bus view that cannot expose stock under its minimum-stack policy. The demand hatch can still access that grid's smaller available quantity. The implementation checks for a usable view before deduplication.
- Exploratory background runs were sometimes terminated, and world saving can continue for several minutes after the test report completes. Those partial/terminated runs are not acceptance evidence. One earlier expanded run failed the existing scan-eviction assertion; its unchanged test passed in both final complete runs. Final client and AE2 acceptance were run sequentially to avoid competing heavy workloads.
- The intentionally exceptional-calculation fixture logs its deliberate exception. It passes by verifying the pending attempt is discarded without retry; this expected log is not a suite failure.
- Client captures validate native software-rendered visuals, not representative GPU performance. The headless audio device is unavailable, so audible playback is not verified.
- Native simulation/payment is not a distributed transaction against arbitrary third-party handlers that violate simulation semantics or sudden process termination. No such crash-atomicity is claimed.
- A matching job may be too small or owned by another requester. Suppression still consumes this run's one opportunity, as specified; delivery before the deadline is not guaranteed.
- No version bump, deployment, commit, push, canonical-spec synchronization or archival was performed. Back up worlds before downgrading/removing AE2 or placed demand hatches.

## Artifact

    /home/siredvin/projects/Greg-E-core/build/libs/gregecore-1.12.7-1.20.1.jar

Size: 939773 bytes.

SHA-256:

    4402b44755b6a20827d4fc5343c8a5ba607b6108c02ce7aa0266c20df44384b1
