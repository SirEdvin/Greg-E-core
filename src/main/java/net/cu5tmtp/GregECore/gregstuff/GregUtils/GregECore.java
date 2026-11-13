package net.cu5tmtp.GregECore.gregstuff.GregUtils;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.mojang.logging.LogUtils;
import net.cu5tmtp.GregECore.block.ModBlocks;
import net.cu5tmtp.GregECore.entity.ModEntity;
import net.cu5tmtp.GregECore.particle.ModParticles;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.recipe.GregERecipeTypes;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregEStuffInit;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.misc.ModCreativeModTabs;
import net.cu5tmtp.GregECore.item.GreggyItems;
import net.cu5tmtp.GregECore.item.ModItems;
import net.cu5tmtp.GregECore.wandOfPuppetry.AnimatedBlockEntity;
import net.cu5tmtp.GregECore.wandOfPuppetry.AnimatedBlockModel;
import net.cu5tmtp.GregECore.wandOfPuppetry.AnimatedBlockRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@SuppressWarnings("removal")
@Mod(GregECore.MOD_ID)
public class GregECore {
    public static final String MOD_ID = "gregecore";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final GTRegistrate REGISTRATE = GTRegistrate.create(MOD_ID);

    public GregECore() {
        REGISTRATE.registerRegistrate();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModCreativeModTabs.register(modEventBus);
        modEventBus.addListener(this::addMaterialRegistries);
        modEventBus.addListener(this::addMaterials);
        modEventBus.addListener(this::addDemandMaterialForms);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntity.register(modEventBus);
        ModParticles.register(modEventBus);

        modEventBus.addGenericListener(GTRecipeType.class, this::registerGregERecipeTypes);
        modEventBus.addGenericListener(MachineDefinition.class, this::registerMachines);

        modEventBus.addListener(this::registerAttributes);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SuppressWarnings("removal")
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    private void registerGregERecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        GregERecipeTypes.init();
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        GregEStuffInit.initGregEParts();
        GregEStuffInit.initHPCAParts();
        GregEStuffInit.initGregERenderRegistries();
        GregEStuffInit.initGregEMulti();
    }
    private void addMaterialRegistries(MaterialRegistryEvent event) {
        GTCEuAPI.materialManager.createRegistry(MOD_ID);
    }

    private void addMaterials(MaterialEvent event) {
        GreggyItems.register();
    }

    private void addDemandMaterialForms(PostMaterialEvent event) {
        GTMaterials.Aluminium.addFlags(MaterialFlags.GENERATE_ROTOR);
        GTMaterials.Neutronium.addFlags(MaterialFlags.GENERATE_ROTOR);
        GTMaterials.RhodiumPlatedPalladium.addFlags(MaterialFlags.GENERATE_GEAR);
        GTMaterials.Darmstadtium.addFlags(MaterialFlags.GENERATE_GEAR);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntity.ANIMATED_BLOCK.get(), AnimatedBlockEntity.createAttributes().build());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CraftingHelper.register(BraveryBookIngredient.ID, BraveryBookIngredient.SERIALIZER));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerParticles(net.minecraftforge.client.event.RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.WORLD_EATER_SCAN.get(),
                    net.cu5tmtp.GregECore.client.particle.WorldEaterScanParticle.Provider::new);
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntity.ANIMATED_BLOCK.get(), AnimatedBlockRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(AnimatedBlockRenderer.ANIMATED_BLOCK_LAYER, AnimatedBlockModel::createBodyLayer);
        }
    }

}
