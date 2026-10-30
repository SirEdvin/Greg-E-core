package net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.renderRegistries;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.thingsToRender.*;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("removal")
public class GregERenederRegistries {

    public static final ResourceLocation ENHANCED_FUSION_RING_ID =
            new ResourceLocation("gregecore", "enhanced_fusion_ring");

    public static final ResourceLocation STAR_MAYKR_STAR_ID =
            new ResourceLocation("gregecore", "star_maykr_star");

    public static final ResourceLocation FORNAX_UNIVERSI_ID =
            new ResourceLocation("gregecore", "fornax_universi_blackhole");

    public static final ResourceLocation ASCENCION_ALTAR_ID =
            new ResourceLocation("gregecore", "ascencion_altar_hand");

    public static final ResourceLocation DYSONLAUNCHER_ID =
            new ResourceLocation("gregecore", "dysonlauncher");

    public static final ResourceLocation DYSONCOLLECTOR_ID =
            new ResourceLocation("gregecore", "dysoncollector");

    public static final ResourceLocation REALITYENGINE_ID =
            new ResourceLocation("gregecore", "realityenigne_eye");

    public static final ResourceLocation SPACEELEVATOR_ID =
            new ResourceLocation("gregecore", "spaceelevatorupyougo");

    public static final ResourceLocation DEEPSPACE_ID =
            new ResourceLocation("gregecore", "deepspaceexplorego");

    public static final ResourceLocation INFUSION_ALTAR_ID =
            new ResourceLocation("gregecore", "infusionrendersocool");

    public static final ResourceLocation FISSION_ROD_ID =
            new ResourceLocation("gregecore", "fissionrodinsertion");

    public static final ResourceLocation SASYNTH_ID =
            new ResourceLocation("gregecore", "sasynthsparks");

    public static final ResourceLocation AMS_ID =
            new ResourceLocation("gregecore", "amssparks");

    public static final ResourceLocation PRESSURE_ID =
            new ResourceLocation("gregecore", "pressuringchamberyay");

    public static final ResourceLocation CATHEDRAL_ID =
            new ResourceLocation("gregecore", "cathedralrender");

    public static DynamicRender<?, ?> createEnhancedFusionRingRender() {
        return new EnhancedFusionRingRender();
    }
    public static DynamicRender<?, ?> createStarMaykrRender() {
        return new StarMaykrRender();
    }
    public static DynamicRender<?, ?> createFornaxUniversiRender() {
        return new FornaxUniversiRender();
    }
    public static DynamicRender<?, ?> createAscencionAltarRender() {
        return new AscencionAltarRender();
    }
    public static DynamicRender<?, ?> createDysonSwarmLauncherRender() {
        return new DysonSwarmLauncherRender();
    }
    public static DynamicRender<?, ?> createDysonSwarmEnergyCollectorRender() { return new DysonSwarmEnergyCollectorRender(); }
    public static DynamicRender<?, ?> createRealityEngineRender() { return new RealityFractureEngineRender(); }
    public static DynamicRender<?, ?> createSpaceElevatorRender() { return new SpaceElevatorRender(); }
    public static DynamicRender<?, ?> createDeepSpaceRender() { return new DeepSpaceExplorerRender(); }
    public static DynamicRender<?, ?> createInfusionAltarRender() { return new InfusionAltarRender(); }
    public static DynamicRender<?, ?> createFissionRodRender() { return new FissionReactorRender(); }
    public static DynamicRender<?, ?> createSASRender() { return new SASRender(); }
    public static DynamicRender<?, ?> createAMSRender() { return new AMSRender(); }
    public static DynamicRender<?, ?> createPressureChamberRender() { return new PressureChamberRender(); }
    public static DynamicRender<?, ?> createCathedralRender() { return new BloodCathedralRender(); }



    public static void init() {

        DynamicRenderManager.register(ENHANCED_FUSION_RING_ID, EnhancedFusionRingRender.TYPE);
        DynamicRenderManager.register(STAR_MAYKR_STAR_ID, StarMaykrRender.TYPE);
        DynamicRenderManager.register(FORNAX_UNIVERSI_ID, FornaxUniversiRender.TYPE);
        DynamicRenderManager.register(ASCENCION_ALTAR_ID, AscencionAltarRender.TYPE);
        DynamicRenderManager.register(DYSONLAUNCHER_ID, DysonSwarmLauncherRender.TYPE);
        DynamicRenderManager.register(DYSONCOLLECTOR_ID, DysonSwarmEnergyCollectorRender.TYPE);
        DynamicRenderManager.register(REALITYENGINE_ID, RealityFractureEngineRender.TYPE);
        DynamicRenderManager.register(SPACEELEVATOR_ID, SpaceElevatorRender.TYPE);
        DynamicRenderManager.register(DEEPSPACE_ID, DeepSpaceExplorerRender.TYPE);
        DynamicRenderManager.register(INFUSION_ALTAR_ID, InfusionAltarRender.TYPE);
        DynamicRenderManager.register(FISSION_ROD_ID, FissionReactorRender.TYPE);
        DynamicRenderManager.register(SASYNTH_ID, SASRender.TYPE);
        DynamicRenderManager.register(AMS_ID, AMSRender.TYPE);
        DynamicRenderManager.register(PRESSURE_ID, PressureChamberRender.TYPE);
        DynamicRenderManager.register(CATHEDRAL_ID, BloodCathedralRender.TYPE);
        DynamicRenderManager.register(ResourceLocation.parse("gregecore:world_eater_parts"), WorldEaterPartRender.TYPE);

        EnhancedFusionRingRender.init();
        StarMaykrRender.init();
        FornaxUniversiRender.init();
        AscencionAltarRender.init();
        DysonSwarmLauncherRender.init();
        DysonSwarmEnergyCollectorRender.init();
        RealityFractureEngineRender.init();
        SpaceElevatorRender.init();
        DeepSpaceExplorerRender.init();
        InfusionAltarRender.init();
        FissionReactorRender.init();
        SASRender.init();
        AMSRender.init();
        PressureChamberRender.init();
        BloodCathedralRender.init();
    }
}