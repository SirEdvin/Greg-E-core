package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.euclid;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;


public class AssemblyHall extends WorkableElectricMultiblockMachine {

    public AssemblyHall(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {

        assert recipe != null;
        String blockId = recipe.data.getString("cube_block");

        Block requiredBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));

        if (requiredBlock != null && requiredBlock != Blocks.AIR) {
            Direction back = this.getFrontFacing().getOpposite();
            BlockPos centerPos = this.getPos()
                    .relative(back, 7)
                    .above(6);

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        BlockPos checkPos = centerPos.offset(x, y, z);
                        BlockState state = this.getLevel().getBlockState(checkPos);

                        if (!state.is(requiredBlock)) {
                            return false;
                        }
                    }
                }
            }

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        BlockPos breakPos = centerPos.offset(x, y, z);
                        this.getLevel().removeBlock(breakPos, false);
                    }
                }
            }
        }

        return super.beforeWorking(recipe);
    }

    public static MachineDefinition ASSEMBLYHALL = REGISTRATE
            .multiblock("assemblyhall", AssemblyHall::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(GCYMBlocks.CASING_STRESS_PROOF)
            .recipeTypes(GregERecipeTypes.ASSEMBLYHALL)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("aaabbcbbbcbbaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaabbcbbbcbbaaa")
                        .aisle("aabddcefecddbaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aabddcefecddbaa")
                        .aisle("abdddcefecdddba", "aagggaaaaagggaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "aahihaaaaahihaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "aahihaaaaahihaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "abdddcefecdddba")
                        .aisle("bddddcefecddddb", "aagjgaaaaagjgaa", "aagjgaaaaagjgaa", "aagjgaaaaagjgaa", "aaijiaaaaaijiaa", "aagjgaaaaagjgaa", "aagjgaaaaagjgaa", "aagjgaaaaagjgaa", "aaijiaaaaaijiaa", "aagjgaaaaagjgaa", "aagjgaaaaagjgaa", "aagjgaaaaagjgaa", "bddddcefecddddb")
                        .aisle("bddddcefecddddb", "aagggaaaaagggaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "aahihaaaaahihaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "aahihaaaaahihaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "bddddcefecddddb")
                        .aisle("ccccckbbbkccccc", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "ccccckbbbkccccc")
                        .aisle("beeeebeeebeeeeb", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaalllaaaaaa", "aaaaaalllaaaaaa", "aaaaaalllaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "beeeebeeebeeeeb")
                        .aisle("bffffbefebffffb", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaalllaaaaaa", "aaaaaalalaaaaaa", "aaaaaalllaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "bffffbefebffffb")
                        .aisle("beeeebeeebeeeeb", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaalllaaaaaa", "aaaaaalllaaaaaa", "aaaaaalllaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "beeeebeeebeeeeb")
                        .aisle("ccccckbbbkccccc", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "ccccckbbbkccccc")
                        .aisle("bddddcefecddddb", "aagggaaaaagggaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "aahihaaaaahihaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "aahihaaaaahihaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "bddddcefecddddb")
                        .aisle("bddddcefecddddb", "aagjgaaaaagjgaa", "aagjgaaaaagjgaa", "aagjgaaaaagjgaa", "aaijiaaaaaijiaa", "aagjgaaaaagjgaa", "aagjgaaaaagjgaa", "aagjgaaaaagjgaa", "aaijiaaaaaijiaa", "aagjgaaaaagjgaa", "aagjgaaaaagjgaa", "aagjgaaaaagjgaa", "bddddcefecddddb")
                        .aisle("abdddcefecdddba", "aagggaaaaagggaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "aahihaaaaahihaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "aahihaaaaahihaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "aagggaaaaagggaa", "abdddcefecdddba")
                        .aisle("aabddcefecddbaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aabddcefecddbaa")
                        .aisle("aaabbcbzbcbbaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaa", "aaabbcbbbcbbaaa")

                        .where("a", Predicates.any())
                        .where("b", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:stress_proof_casing")))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(2)))
                        .where("c", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_tiled_very_dark_gray"))))
                        .where("d", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:sturdy_machine_casing"))))
                        .where("e", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:robust_machine_casing"))))
                        .where("f", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:superconducting_coil"))))
                        .where("g", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:laminated_glass"))))
                        .where("h", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:solid_machine_casing"))))
                        .where("i", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gregecore:solid_engine_intake"))))
                        .where("j", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:assembly_line_grating"))))
                        .where("k", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:white_lamp"))))
                        .where("l", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:shock_proof_cutting_casing")))
                                .or(Predicates.any()))
                        .where("z", Predicates.controller(Predicates.blocks(definition.get())))
                        .build();
            })
            .workableCasingModel(
                    GTCEu.id("block/casings/gcym/stress_proof_casing"),
                    GTCEu.id("block/multiblock/distillation_tower")
            )
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.assemblyhall1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.assemblyhall2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.assemblyhall3").withStyle(style -> style.withColor(0x90EE90)))
            .register();

    public static void init() {
    }
}