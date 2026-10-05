package net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff;

import net.cu5tmtp.GregECore.gregstuff.GregMachines.hpca.GregEHPCAInit;
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
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.single.DimensionalCleaningMaintenance;
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
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.renderRegistries.GregERenederRegistries;

public class GregEStuffInit {
    public static void initGregEMulti(){
        AcceleratedEBF.init();
        GiantAcceleratedEBF.init();
        GiantChemicalReactor.init();
        DysonSwarmLauncher.init();
        DysonSwarmEnergyCollector.init();
        LearningAcceleratedEBF.init();
        EnhancedFusionReactor.init();
        StarMaykr.init();
        FornaxUniversi.init();
        EnhancedBlastChiller.init();
        BigFreezer.init();
        AscencionAltar.init();
        RealityFractureEngine.init();
        SpaceElevator.init();
        DeepSpaceExplorer.init();
        InfusionAltar.init();
        FissionReactor.init();
        GiantAlloyBlastSmelter.init();
        SpecializedAssemblyLine.init();
        AutomaticAssemblyLine.init();
        //BoxMachines.init();
        //CartridgeCase.init();
        UHVMultiRegistry.init();
        SubatomicAntimatterSyntheticator.init();
        PressureChamber.init();
        AntiMassSpectrometer.init();
        NetherDrillRig.init();
        BloodCathedral.init();
        GenesisCrucible.init();
        GenesisCrucibleCases.init();
        AssemblyHall.init();
        PrecisionFabricationArray.init();
        IncompleteWorldEaterTamer.init();
    }

    public static void initGregEParts(){
        StarFeederPartMachine.init();
        ParallelBoosterPartMachine.init();
        AdvancedParallelBoosterPartMachine.init();
        AdvancedCoolantInputPartMachine.init();
        RealityFractureEnginePartMachine.init();
        BacteriaInputPartMachine.init();
        CoolantInputPartMachine.init();
        RepairPartsInputPartMachine.init();
        AscencionPartMachine.init();
        AdvancedHeaterInputPartMachine.init();
        DroneAccessHatchPartMachine.init();
        PedestalPartMachine.init();
        AerInputPartMachine.init();
        AquaInputPartMachine.init();
        TerraInputPartMachine.init();
        OrdoInputPartMachine.init();
        IgnisInputPartMachine.init();
        PerditioInputPartMachine.init();
        CoolantOutputPartMachine.init();
        ThreadT1PartMachine.init();
        ThreadT2PartMachine.init();
        ThreadT3PartMachine.init();
        PressurePartMachine.init();
        DimensionalRelicsPartMachine.init();
        DimensionalCleaningMaintenance.init();
        BloodStoragePartMachine.init();
        DysonSwarmEnergyOutputPartMachine.init();
        DysonSwarmEuclidEnergyOutputPartMachine.init();
    }

    public static void initHPCAParts(){
        GregEHPCAInit.init();
    }

    public static void initGregERenderRegistries(){
        GregERenederRegistries.init();
    }
}
