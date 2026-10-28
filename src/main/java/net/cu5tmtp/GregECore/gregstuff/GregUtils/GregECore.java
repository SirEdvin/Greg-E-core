package net.cu5tmtp.GregECore.gregstuff.GregUtils;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.mojang.logging.LogUtils;
import net.cu5tmtp.GregECore.block.ModBlocks;
import net.cu5tmtp.GregECore.entity.ModEntity;
import net.cu5tmtp.GregECore.particle.ModParticles;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.cleanroom.DimensionSimulator;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.misc.AntiMassSpectrometer;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.misc.NetherDrillRig;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.misc.DimensionalRelicsPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.CheckForDim;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregEStuffInit;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.ModCreativeModTabs;
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
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.items.IItemHandler;
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

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntity.ANIMATED_BLOCK.get(), AnimatedBlockEntity.createAttributes().build());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            net.minecraftforge.common.crafting.CraftingHelper.register(BraveryBookIngredient.ID, BraveryBookIngredient.SERIALIZER);
            for (int tier = GTValues.LV; tier <= GTValues.UHV; tier++) {

                String machineName = GTValues.VN[tier].toLowerCase() + "_gas_collector";
                ResourceLocation machineId = new ResourceLocation("gtceu", machineName);
                MachineDefinition machine = GTRegistries.MACHINES.get(machineId);

                if (machine != null) {
                    var oldModifier = machine.getRecipeModifier();
                    machine.setRecipeModifier((metaMachine, recipe) -> r -> CheckForDim.applyDimensionalBypass(metaMachine, r, oldModifier));
                }

                String sifterName = GTValues.VN[tier].toLowerCase() + "_sifter";
                ResourceLocation sifterId = new ResourceLocation("gtceu", sifterName);
                MachineDefinition sifter = GTRegistries.MACHINES.get(sifterId);

                if (sifter != null) {
                    var oldModifier = sifter.getRecipeModifier();
                    sifter.setRecipeModifier((metaMachine, recipe) -> r -> CheckForDim.applyDimensionalBypass(metaMachine, r, oldModifier));
                }
            }

            MachineDefinition machineMultiAMS = AntiMassSpectrometer.ANTIMASSSPECTROMETER;

            if (machineMultiAMS != null) {
                var oldMultiModifier = machineMultiAMS.getRecipeModifier();
                machineMultiAMS.setRecipeModifier((metaMachine, recipe) -> r -> CheckForDim.applyAntiMassBypass(metaMachine, r, oldMultiModifier));
            }

            MachineDefinition machineMultiNDR = NetherDrillRig.NETHERDRILLRIG;

            if (machineMultiNDR != null) {
                var oldMultiModifier = machineMultiNDR.getRecipeModifier();
                machineMultiNDR.setRecipeModifier((metaMachine, recipe) -> r -> CheckForDim.applyAntiMassBypass(metaMachine, r, oldMultiModifier));
            }
        });
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
