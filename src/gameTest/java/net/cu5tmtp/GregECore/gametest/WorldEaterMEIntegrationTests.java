package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.GTCEu;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class WorldEaterMEIntegrationTests {
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void realRequestAndExactStoragePayment(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.realRequestAndExactStoragePayment(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void paymentAliasingDeadlineAndCleanup(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.paymentAliasingDeadlineAndCleanup(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 1600)
    public static void nativeCraftDeliveryAndFireAndForget(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.nativeCraftDeliveryAndFireAndForget(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void legacyPersistenceAndReadOnlyControls(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.legacyPersistenceAndReadOnlyControls(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 1600)
    public static void asynchronousRacesAndNoRetry(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.asynchronousRacesAndNoRetry(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 1000)
    public static void startupSkipsAndMatchingJobs(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.startupSkipsAndMatchingJobs(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void nativeHatchEvictionAndReload(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.nativeHatchEvictionAndReload(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void nativeSubmittedJobEvictionAndReload(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.nativeSubmittedJobEvictionAndReload(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void sharedGridAndIndependentJobs(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.sharedGridAndIndependentJobs(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void unavailableStartupAndFailures(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.unavailableStartupAndFailures(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void awaitedIntermediateAndBusyCpu(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.awaitedIntermediateAndBusyCpu(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void queuedRunsAndOfflineConsequences(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.queuedRunsAndOfflineConsequences(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000)
    public static void startupShortageAndAbsentHatch(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.startupShortageAndAbsentHatch(helper);
    }
}
