package net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import net.cu5tmtp.GregECore.block.ModBlocks;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.chillers.BigFreezer;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.chillers.EnhancedBlastChiller;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.dyson.DysonSwarmEnergyCollector;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.dyson.DysonSwarmLauncher;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.euclid.AssemblyHall;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.euclid.GenesisCrucible;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.euclid.GenesisCrucibleCases;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.euclid.PrecisionFabricationArray;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.furnaces.AcceleratedEBF;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.furnaces.GiantAcceleratedEBF;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.furnaces.GiantAlloyBlastSmelter;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.furnaces.LearningAcceleratedEBF;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.*;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.misc.*;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.reactors.EnhancedFusionReactor;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.reactors.FissionReactor;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.reactors.GiantChemicalReactor;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.uhvmulti.UHVMultiRegistry;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.coolant.AdvancedCoolantInputPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.coolant.CoolantInputPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.coolant.CoolantOutputPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.*;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.essentiaParts.*;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.misc.*;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.parallel.AdvancedParallelBoosterPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.parallel.ParallelBoosterPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.threadParts.ThreadT1PartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.threadParts.ThreadT2PartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.threadParts.ThreadT3PartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GregECore.MOD_ID);

    public static final RegistryObject<CreativeModeTab> GREGE_TAB = CREATIVE_MODE_TABS.register("gregecoretab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.LINEARACCELERATOR.get()))
                    .title(Component.literal("Greg-E-core"))
                    .displayItems((pParameters, pOutput) -> {
                        ModBlocks.TAB_BLOCKS.forEach(block -> pOutput.accept(block.get()));
                        pOutput.accept(ModItems.ENERGY_BACTERIA.get());
                        pOutput.accept(ModItems.SPEED_BACTERIA.get());
                        pOutput.accept(ModItems.PARALLEL_BACTERIA.get());
                        pOutput.accept(ModItems.ULTIMATE_BACTERIA.get());
                        pOutput.accept(ModItems.SOLAR_ACTIVATOR.get());
                        pOutput.accept(ModItems.SOLAR_SAIL.get());
                        pOutput.accept(ModItems.QUANTUM_ACCELERATOR.get());
                        pOutput.accept(ModItems.ROCKET_CONE.get());
                        pOutput.accept(ModItems.SERVER_RACK.get());
                        pOutput.accept(ModItems.BRASS_PELLET.get());
                        pOutput.accept(ModItems.AMERICIUM_PELLET.get());
                        pOutput.accept(ModItems.NEUTRONIUM_PELLET.get());
                        pOutput.accept(ModItems.TOME1.get());
                        pOutput.accept(ModItems.TOME2.get());
                        pOutput.accept(ModItems.TOME3.get());
                        pOutput.accept(ModItems.TOME4.get());
                        pOutput.accept(ModItems.TOME5.get());
                        pOutput.accept(ModItems.REDEYE.get());
                        pOutput.accept(ModItems.GREENEYE.get());
                        pOutput.accept(ModItems.PURPLEEYE.get());
                        pOutput.accept(ModItems.UNSTABLE.get());
                        pOutput.accept(ModItems.SPACESHIP1.get());
                        pOutput.accept(ModItems.SPACESHIP2.get());
                        pOutput.accept(ModItems.SPACESHIP3.get());
                        pOutput.accept(ModItems.TINYBLOOD.get());
                        pOutput.accept(ModItems.MEDIUMBLOOD.get());
                        pOutput.accept(ModItems.LARGEBLOOD.get());
                        pOutput.accept(ModItems.WAND_OF_PUPPETRY.get());
                        pOutput.accept(AcceleratedEBF.ACCELERATEDEBF.getItem());
                        pOutput.accept(GiantAcceleratedEBF.GIANTACCELERATEDEBF.getItem());
                        pOutput.accept(EnhancedFusionReactor.ENHANCED_FUSION_REACTOR.getItem());
                        pOutput.accept(EnhancedBlastChiller.ENHANCEDBLASTCHILLER.getItem());
                        pOutput.accept(BigFreezer.BIGFREEZER.getItem());
                        pOutput.accept(StarMaykr.STAR_MAYKR.getItem());
                        pOutput.accept(RealityFractureEngine.REALITYFRACTUREENGINE.getItem());
                        pOutput.accept(AscencionAltar.ASCENCION_ALTAR.getItem());
                        pOutput.accept(GiantChemicalReactor.GIANTCHR.getItem());
                        pOutput.accept(DysonSwarmLauncher.DYSON_SWARM_LAUNCHER.getItem());
                        pOutput.accept(DysonSwarmEnergyCollector.DYSON_SWARM_LAUNCHER.getItem());
                        pOutput.accept(LearningAcceleratedEBF.LEARNING_ACC_EBF.getItem());
                        pOutput.accept(DeepSpaceExplorer.DEEPSPACEEXPLORER.getItem());
                        pOutput.accept(FornaxUniversi.FORNAX_UNIVERSI.getItem());
                        pOutput.accept(FissionReactor.FISSIONREACTOR.getItem());
                        pOutput.accept(SpecializedAssemblyLine.SPECIALIZED_ASSEMBLY_LINE.getItem());
                        pOutput.accept(GiantAlloyBlastSmelter.GIANT_ABS.getItem());
                        pOutput.accept(SpaceElevator.SPACE_ELEVATOR.getItem());
                        pOutput.accept(InfusionAltar.INFUSION_ALTAR.getItem());
                        pOutput.accept(AutomaticAssemblyLine.AUTASSEMBLYLINE.getItem());
                        pOutput.accept(PressureChamber.PRESSURECHAMBER.getItem());
                        pOutput.accept(SubatomicAntimatterSyntheticator.SASYNTH.getItem());
                        pOutput.accept(NetherDrillRig.NETHERDRILLRIG.getItem());
                        pOutput.accept(BloodCathedral.BLODDCATHEDRAL.getItem());
                        pOutput.accept(GenesisCrucible.GENESISCRUCIBLE.getItem());
                        pOutput.accept(GenesisCrucibleCases.GENESISCRUCIBLECASE1.getItem());
                        pOutput.accept(GenesisCrucibleCases.GENESISCRUCIBLECASE2.getItem());
                        pOutput.accept(GenesisCrucibleCases.GENESISCRUCIBLECASE3.getItem());
                        pOutput.accept(AssemblyHall.ASSEMBLYHALL.getItem());
                        pOutput.accept(PrecisionFabricationArray.PFARRAY.getItem());
                        pOutput.accept(IncompleteWorldEaterTamer.INCOMPLETE_WORLD_EATER_TAMER.getItem());

                        for (MachineDefinition box : UHVMultiRegistry.ALL_MACHINES) {
                            pOutput.accept(box.getItem());
                        }
                        /*
                        pOutput.accept(CartridgeCase.CARTRIDGECASE.getItem());
                        for (MachineDefinition box : BoxMachines.ALL_BOXES) {
                            pOutput.accept(box.getItem());
                        }
                        */

                        pOutput.accept(StarFeederPartMachine.STAR_FEEDER_MACHINE.getItem());
                        pOutput.accept(DroneAccessHatchPartMachine.DRONE_ACCESS_MACHINE.getItem());
                        pOutput.accept(ParallelBoosterPartMachine.PARALLEL_BOOSTER_MACHINE.getItem());
                        pOutput.accept(RepairPartsInputPartMachine.REPAIR_PART_INPUT_MACHINE.getItem());
                        pOutput.accept(AdvancedParallelBoosterPartMachine.ADVANCED_PARALLEL_BOOSTER_MACHINE.getItem());
                        pOutput.accept(CoolantInputPartMachine.COOLANT_INPUT_MACHINE.getItem());
                        pOutput.accept(CoolantOutputPartMachine.COOLANT_OUTPUT_MACHINE.getItem());
                        pOutput.accept(ThreadT1PartMachine.THREADING_1_MACHINE.getItem());
                        pOutput.accept(ThreadT2PartMachine.THREADING_2_MACHINE.getItem());
                        pOutput.accept(ThreadT3PartMachine.THREADING_3_MACHINE.getItem());
                        pOutput.accept(TerraInputPartMachine.TERRA_INPUT_MACHINE.getItem());
                        pOutput.accept(AerInputPartMachine.AER_INPUT_MACHINE.getItem());
                        pOutput.accept(AquaInputPartMachine.AQUA_INPUT_MACHINE.getItem());
                        pOutput.accept(IgnisInputPartMachine.IGNIS_INPUT_MACHINE.getItem());
                        pOutput.accept(PerditioInputPartMachine.PERDITIO_INPUT_MACHINE.getItem());
                        pOutput.accept(OrdoInputPartMachine.ORDO_INPUT_MACHINE.getItem());
                        pOutput.accept(BacteriaInputPartMachine.BACTERIAL_INPUT_MACHINE.getItem());
                        pOutput.accept(AdvancedCoolantInputPartMachine.ADVANCED_COOLANT_INPUT_MACHINE.getItem());
                        pOutput.accept(AdvancedHeaterInputPartMachine.ADVANCED_HEATER_INPUT_MACHINE.getItem());
                        pOutput.accept(AscencionPartMachine.ASCENCION_PART_MACHINE.getItem());
                        pOutput.accept(RealityFractureEnginePartMachine.REALITY_FRACTURE_PART_MACHINE.getItem());
                        pOutput.accept(PedestalPartMachine.PEDESTAL_INF_MACHINE.getItem());
                        pOutput.accept(PressurePartMachine.PRESSURE_INPUT_MACHINE.getItem());
                        pOutput.accept(DimensionalRelicsPartMachine.DIMENSIONAL_RELICS_MACHINE.getItem());
                        pOutput.accept(DysonSwarmEnergyOutputPartMachine.DYSON_SWARM_EN_OUTPUT_MACHINE.getItem());
                        pOutput.accept(DysonSwarmEuclidEnergyOutputPartMachine.DYSON_SWARM_EN_OUTPUT_EUCLID_MACHINE.getItem());

                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
