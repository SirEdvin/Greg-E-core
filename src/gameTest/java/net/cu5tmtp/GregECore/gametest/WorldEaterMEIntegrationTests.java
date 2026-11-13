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
    @GameTest(template = "empty", timeoutTicks = 400, batch = "realRequestAndExactStoragePayment")
    public static void realRequestAndExactStoragePayment(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.realRequestAndExactStoragePayment(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 600, batch = "paymentAliasingDeadlineAndCleanup")
    public static void paymentAliasingDeadlineAndCleanup(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.paymentAliasingDeadlineAndCleanup(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 1600, batch = "nativeCraftDeliveryAndFireAndForget")
    public static void nativeCraftDeliveryAndFireAndForget(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.nativeCraftDeliveryAndFireAndForget(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 600, batch = "legacyPersistenceAndReadOnlyControls")
    public static void legacyPersistenceAndReadOnlyControls(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.legacyPersistenceAndReadOnlyControls(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 1600, batch = "asynchronousRacesAndNoRetry")
    public static void asynchronousRacesAndNoRetry(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.asynchronousRacesAndNoRetry(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 1000, batch = "startupSkipsAndMatchingJobs")
    public static void startupSkipsAndMatchingJobs(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.startupSkipsAndMatchingJobs(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000, batch = "nativeHatchEvictionAndReload")
    public static void nativeHatchEvictionAndReload(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.nativeHatchEvictionAndReload(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000, batch = "nativeSubmittedJobEvictionAndReload")
    public static void nativeSubmittedJobEvictionAndReload(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.nativeSubmittedJobEvictionAndReload(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000, batch = "sharedGridAndIndependentJobs")
    public static void sharedGridAndIndependentJobs(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.sharedGridAndIndependentJobs(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000, batch = "unavailableStartupAndFailures")
    public static void unavailableStartupAndFailures(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.unavailableStartupAndFailures(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000, batch = "awaitedIntermediateAndBusyCpu")
    public static void awaitedIntermediateAndBusyCpu(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.awaitedIntermediateAndBusyCpu(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000, batch = "queuedRunsAndOfflineConsequences")
    public static void queuedRunsAndOfflineConsequences(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.queuedRunsAndOfflineConsequences(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 2000, batch = "startupShortageAndAbsentHatch")
    public static void startupShortageAndAbsentHatch(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterMETests.startupShortageAndAbsentHatch(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 1600, batch = "creativeProviderPatternsDeliveryAndIronCost")
    public static void creativeProviderPatternsDeliveryAndIronCost(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterCreativeProviderTests.patternsDeliveryAndIronCost(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 1600, batch = "creativeProviderReloadAndRemoval")
    public static void creativeProviderReloadAndRemoval(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterCreativeProviderTests.reloadAndRemoval(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 600, batch = "meBlocksConnectFromAnySide")
    public static void meBlocksConnectFromAnySide(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterCreativeProviderTests.anySideConnection(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 1600, batch = "creativeProviderPaysDemandHatch")
    public static void creativeProviderPaysDemandHatch(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        WorldEaterCreativeProviderTests.demandHatchPaidThroughProvider(helper);
    }
}
