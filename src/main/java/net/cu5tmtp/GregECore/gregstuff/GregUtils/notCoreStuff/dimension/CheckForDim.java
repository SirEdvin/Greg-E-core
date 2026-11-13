package net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.dimension;

import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.CleaningMaintenanceHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.cleanroom.DimensionSimulator;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.misc.DimensionalRelicsPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.single.DimensionalCleaningMaintenance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Set;

@SuppressWarnings("removal")
public class CheckForDim {
    private static final Map<ResourceLocation, Set<ResourceLocation>> RELICS = Map.of(
            new ResourceLocation("minecraft:dragon_egg"),
            Set.of(Level.END.location()),

            new ResourceLocation("minecraft:nether_star"),
            Set.of(Level.NETHER.location()),

            new ResourceLocation("twilightforest:twilight_portal_miniature_structure"),
            Set.of(new ResourceLocation("twilightforest:twilight_forest")),

            new ResourceLocation("kubejs:burialmask"),
            Set.of(new ResourceLocation("ad_astra:mars")),

            new ResourceLocation("bloodmagic:rawdemonite"),
            Set.of(new ResourceLocation("bloodmagic:dungeon")),

            new ResourceLocation("kubejs:eyeofpride"),
            Set.of(new ResourceLocation("cataclysm_dimension:cataclysm_forge_of_aeons"),
                    new ResourceLocation("cataclysm_dimension:cataclysm_abyssal_depths"),
                    new ResourceLocation("cataclysm_dimension:cataclysm_pharaohs_bane"),
                    new ResourceLocation("cataclysm_dimension:cataclysm_infernos_maw")),

            new ResourceLocation("kubejs:eyeofsin"),
            Set.of(new ResourceLocation("cataclysm_dimension:cataclysm_bastion_lost"),
                    new ResourceLocation("cataclysm_dimension:cataclysm_eternal_frosthold"),
                    new ResourceLocation("cataclysm_dimension:cataclysm_sanctum_fallen"),
                    new ResourceLocation("cataclysm_dimension:cataclysm_souls_anvil")),

            new ResourceLocation("kubejs:stoneofhorus"),
            Set.of(new ResourceLocation("ad_astra:earth_orbit"),
                    new ResourceLocation("ad_astra:venus_orbit"),
                    new ResourceLocation("ad_astra:mercury_orbit"),
                    new ResourceLocation("ad_astra:mars_orbit")),

            new ResourceLocation("kubejs:charmofguilliman"),
            Set.of(new ResourceLocation("ad_extendra:neptune_orbit"),
                    new ResourceLocation("ad_extendra:uranus_orbit"),
                    new ResourceLocation("ad_extendra:saturn_orbit"),
                    new ResourceLocation("ad_extendra:jupiter_orbit"),
                    new ResourceLocation("ad_extendra:sedna"))

    );

    public static boolean canSimulate(MetaMachine machine, ResourceLocation dimension) {
        MultiblockControllerMachine controller = findTargetController(machine);
        if (controller == null) return false;

        for (IMultiPart part : controller.getParts()) {
            if (!(part instanceof DimensionalRelicsPartMachine)) continue;

            for (var handlerList : part.getRecipeHandlers()) {
                for (Object handler : handlerList.getCapability(ItemRecipeCapability.CAP)) {
                    if (!(handler instanceof IItemHandler inventory)) continue;

                    for (int i = 0; i < inventory.getSlots(); i++) {
                        var stack = inventory.getStackInSlot(i);
                        if (stack.isEmpty()) continue;

                        var itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());

                        if (itemId != null &&
                                RELICS.getOrDefault(itemId, Set.of()).contains(dimension)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private static MultiblockControllerMachine findTargetController(MetaMachine machine) {
        if (machine instanceof ICleanroomReceiver receiver) {
            var cleanroom = receiver.getCleanroom();

            if (cleanroom != null &&
                    cleanroom.getTypes().contains(DimensionSimulator.DIMENSIONAL_SIMULATOR_CLEANROOM)) {

                if (cleanroom instanceof MultiblockControllerMachine controller)
                    return controller;

                if (machine instanceof MultiblockControllerMachine controller)
                    return controller;
            }
        }

        if (machine instanceof MultiblockControllerMachine controller) {
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof CleaningMaintenanceHatchPartMachine hatch &&
                        hatch.getDefinition() == DimensionalCleaningMaintenance.DIMENSIONAL_CLEANING_HATCH) {
                    return controller;
                }
            }
        }

        return null;
    }
}