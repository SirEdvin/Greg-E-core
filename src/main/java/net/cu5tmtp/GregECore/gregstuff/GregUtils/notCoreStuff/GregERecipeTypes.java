package net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.PhantomSlotWidget;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class GregERecipeTypes {
    public static final GTRecipeType WORLD_EATER_TAMER = GTRecipeTypes.register("world_eater_tamer", "endgame")
            .setMaxIOSize(1, 1, 0, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .addDataInfo(data -> "Required state: " + data.getString("required_state"))
            .addDataInfo(data -> "Operation: " + data.getString("operation"));

    public static GTRecipeType LAUNCH_SAILS = GTRecipeTypes.register("launch_sails", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3,3,3,3)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_FUSION, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.COOLING)
            .addDataInfo(data -> {
                if (data.contains("sailMultiplier")) {
                    int multi = data.getInt("sailMultiplier");
                    switch (multi){
                        case 1 -> {
                            return ChatFormatting.GREEN + "Counts as 50 sails shot up.";
                        }
                        case 2 -> {
                            return ChatFormatting.GREEN + "Counts as 150 sails shot up.";
                        }
                        default -> {
                            return ChatFormatting.GREEN + "Counts as 10 sails shot up.";
                        }
                    }
                }
                return null;
            });

    public static GTRecipeType GET_SOLAR_SAIL_ENERGY = GTRecipeTypes.register("get_solar_sail_energy", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3,3,3,3)
            .setEUIO(IO.OUT)
            .setProgressBar(GuiTextures.PROGRESS_BAR_FUSION, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.TURBINE);

    public static GTRecipeType INFUSION_ALTAR_INFUSING = GTRecipeTypes.register("infusion_altar_infusing", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(12,1,6,0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_FUSION, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.FIRE);
    
    public static GTRecipeType STAR_MAYKR_SINGULARITIES = GTRecipeTypes.register("star_maykr_singularities", "endgame")
            .setMaxIOSize(9,1,0,0)
            .setEUIO(IO.IN)
            .setProgressBar(GregEGUITextures.STARMAYKR_PROGRESS, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.JET_ENGINE)
            .addDataInfo(data -> {
                if (data.contains("weight")) {
                    double weight = data.getDouble("weight");
                    return ChatFormatting.DARK_PURPLE+ "Star weight cost: " + ChatFormatting.WHITE + (int) weight;
                }
                return null;
            });

    public static GTRecipeType PLANETARGY_GAS_SIPHON = GTRecipeTypes.register("planetarygassiphon", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(3, 3, 9, 9)
            .setSlotOverlay(false, false, GuiTextures.BOX_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.COMPRESSOR);

    public static GTRecipeType ZERO_GRAV_MIXER = GTRecipeTypes.register("zerogravmixer", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(3, 3, 9, 9)
            .setSlotOverlay(false, false, GuiTextures.BOX_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MIXER);

    public static GTRecipeType SEDNASAMPLER = GTRecipeTypes.register("sednasampler", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(3, 3, 0, 0)
            .setSlotOverlay(false, false, GuiTextures.BOX_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MIXER);

    public static GTRecipeType FISSION_REACTION = GTRecipeTypes.register("fission_reaction", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3,3,3,3)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.JET_ENGINE)
            .addDataInfo(data -> {
                if (data.contains("heatgen")) {
                    double weight = data.getInt("heatgen");
                    return ChatFormatting.RED + "Heat generated: " + ChatFormatting.WHITE + (int) weight + "K";
                }
                return null;
            });

    public static GTRecipeType FORNAX_UNIVERSI_ACCELERETION = GTRecipeTypes.register("fornax_universi_acceleration", "endgame")
            .setMaxIOSize(9,1,0,0)
            .setEUIO(IO.IN)
            .setProgressBar(GregEGUITextures.FORNAX_PROGRESS, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.JET_ENGINE);

    public static GTRecipeType SPECIALIZEDASSEMBLYLINE = GTRecipeTypes.register("specializedassemblyline", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(15,1,3,0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ASSEMBLER);

    public static GTRecipeType NETHERDRILLRIGRECIPE = GTRecipeTypes.register("netherdrillrig", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(9,3,3,3)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.DRILL_TOOL);

    public static GTRecipeType BLOODCATHEDRALCRAFT = GTRecipeTypes.register("bloodcathedral", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1,1,0,0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.FIRE)
            .addDataInfo(data -> {
                if (data.contains("bloodcost")){
                    int pa = data.getInt("bloodcost");
                    return ChatFormatting.DARK_RED + "Consumes " + pa + "mb of Blood.";
                }
                return null;
            });

    public static GTRecipeType SASCRAFTING = GTRecipeTypes.register("sascrafting", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3,3,3,3)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ASSEMBLER);

    public static GTRecipeType ASSEMBLYHALL = GTRecipeTypes.register("assemblyhall", "euclid")
            .setMaxIOSize(6, 1, 3, 0)
            .setEUIO(IO.IN)
            .setProgressBar(GregEGUITextures.ASSEMBLY_HALL_PROGRESS, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ASSEMBLER)
            .setUiBuilder((recipe, widgetGroup) -> {
                String data = recipe.data.getString("cube_block");
                ItemStack displayStack = ItemStack.EMPTY;

                ResourceLocation location = new ResourceLocation(data);
                Item item = BuiltInRegistries.ITEM.get(location);

                if (item != Items.AIR) {
                    displayStack = item.getDefaultInstance();
                }

                if (!displayStack.isEmpty()) {
                    int xPos = 90;
                    int yPos = 22;

                    ItemStackTransfer dummyInventory = new ItemStackTransfer(1);
                    dummyInventory.setStackInSlot(0, displayStack);

                    PhantomSlotWidget fakeSlot = new PhantomSlotWidget(dummyInventory, 0, xPos, yPos);
                    fakeSlot.setBackgroundTexture(GuiTextures.SLOT);
                    fakeSlot.setHoverTooltips(
                            Component.literal("Used to build the hollow central cube.").withStyle(ChatFormatting.LIGHT_PURPLE),
                            Component.literal("Make sure the inside is completely empty!").withStyle(ChatFormatting.LIGHT_PURPLE)
                    );

                    widgetGroup.addWidget(fakeSlot);
                }
            });

    public static GTRecipeType GCCRAFTING = GTRecipeTypes.register("gccrafting", "euclid")
            .setMaxIOSize(6,3,3,3)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ASSEMBLER)
            .addDataInfo(data -> {
                if (data.contains("insertedc")) {
                    String requiredCasesRaw = data.getString("insertedc");
                    if (!requiredCasesRaw.isEmpty()) {
                        String[] parts = requiredCasesRaw.split(",");
                        StringBuilder translatedNames = new StringBuilder();

                        for (int i = 0; i < parts.length; i++) {
                            String internalName = parts[i].trim();

                            String readableName = switch (internalName) {
                                case "genesiscruciblecaseone" -> "D";
                                case "genesiscruciblecasetwo" -> "K";
                                case "genesiscruciblecasethree" -> "G";
                                case "genesiscruciblecasefour" -> "Placeholder 4";
                                default -> internalName;
                            };

                            translatedNames.append(readableName);

                            if (i < parts.length - 1) {
                                translatedNames.append(", ");
                            }
                        }

                        return ChatFormatting.LIGHT_PURPLE + "Cartridges: " + ChatFormatting.DARK_GREEN + translatedNames;
                    }
                }
                return null;
            });

    public static GTRecipeType PFARRAYCRAFT = GTRecipeTypes.register("pfarraycraft", "euclid")
            .setMaxIOSize(6,1,0,0)
            .setEUIO(IO.IN)
            .setProgressBar(GregEGUITextures.PRECISION_ARRAY_PROGRESS, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ASSEMBLER);

    public static GTRecipeType DUMMYRECIPE = GTRecipeTypes.register("dummydontuse", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3,3,3,3)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.ASSEMBLER);

    public static GTRecipeType PRESSURECHAMCRAFT = GTRecipeTypes.register("pressuring", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(9,1,0,0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.FORGE_HAMMER)
            .addDataInfo(data -> {
                if (data.contains("pa")){
                    int pa = data.getInt("pa");
                    return ChatFormatting.LIGHT_PURPLE + "Requires at least " + pa + "Pa.";
                }
                return null;
            });

    public static GTRecipeType ASCENCION_ALTAR_DONATION = GTRecipeTypes.register("ascention_altar_donation", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(9,1,0,0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.FIRE)
            .addDataInfo(data -> {
                if (data.contains("tome")){
                    int tome = data.getInt("tome");

                    String blue = ChatFormatting.BLUE.toString();

                    String tomeName = switch (tome) {
                        case 1 -> "Forbidden Tome Of" + "\n" + blue + "Extraterrestrial Planets";
                        case 2 -> "Forbidden Tome Of" + "\n" + blue + "Mighty Beings";
                        case 3 -> "Forbidden Tome Of" + "\n" + blue + "Hidden Lifeforms";
                        case 4 -> "Forbidden Tome Of Rare Blocks";
                        case 5 -> "Forbidden Tome Of Rare Items";
                        default -> throw new IllegalStateException("Unexpected value: " + tome);
                    };

                    return ChatFormatting.LIGHT_PURPLE + "Tome Needed: " + "\n" + ChatFormatting.BLUE + tomeName.replace("\n", "\n" + ChatFormatting.BLUE);
                }
                return null;
            });

    public static GTRecipeType ADVANCED_FUSION = GTRecipeTypes.register("advanced_fusion", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(3,3,3,3)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_FUSION, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.JET_ENGINE)
            .addDataInfo(data -> {
                if (data.contains("heat_level")) {
                    int heat = data.getInt("heat_level");
                    return ChatFormatting.RED + "Heat: " + ChatFormatting.WHITE + (heat - 500) + "K - " + heat + "K";
                }
                return null;
            });

    public static GTRecipeType OPEN_THE_RIFT = GTRecipeTypes.register("open_rift", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1,1, 0,0)
            .setEUIO(IO.IN)
            .setProgressBar(GuiTextures.PROGRESS_BAR_FUSION, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.JET_ENGINE)
            .addDataInfo(data -> ChatFormatting.RED + "Needs all 3 eyes inserted.");

    public static GTRecipeType AMSPECTROMETER = GTRecipeTypes.register("antimassspectro", "halflife")
            .setMaxIOSize(1,0, 0,1)
            .setEUIO(IO.IN)
            .setProgressBar(GregEGUITextures.AMSPEC, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.JET_ENGINE);

    public static GTRecipeType SEND_UP_THE_MATS = GTRecipeTypes.register("send_up_the_mats", "endgame")
            .setMaxIOSize(6,6, 3,3)
            .setEUIO(IO.IN)
            .setProgressBar(GregEGUITextures.ELEVATOR_PROGRESS, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.JET_ENGINE)
            .addDataInfo(data -> {
                if (data.contains("height_level")) {
                    int height = data.getInt("height_level");
                    return ChatFormatting.GREEN + "Height: " + ChatFormatting.GOLD + (height - 10) + ChatFormatting.GREEN + " KM - " + ChatFormatting.GOLD + height + ChatFormatting.GREEN + " KM";
                }
                return null;
            });

    public static GTRecipeType DEEP_SPACE_EXPLORE = GTRecipeTypes.register("deep_space_explore", "endgame")
            .setMaxIOSize(6,6, 6,6)
            .setEUIO(IO.IN)
            .setProgressBar(GregEGUITextures.DEEP_PROGRESS, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.SCIENCE)
            .addDataInfo(data -> {
                List<String> info = new ArrayList<>();

                if (data.contains("drone")) {
                    int drone = data.getInt("drone");
                    String droneName = switch (drone) {
                        case 1 -> "Milano";
                        case 2 -> "Bebop";
                        case 3 -> "Cepheus";
                        default -> "Unknown";
                    };
                    info.add(ChatFormatting.GOLD + "Drone: " + ChatFormatting.GREEN + droneName);
                }

                if (data.contains("system")) {
                    int system = data.getInt("system");
                    String systemName = switch (system) {
                        case 1 -> "61 Cygni";
                        case 2 -> "Struve 2398";
                        case 3 -> "Lacaille 8760";
                        case 4 -> "Gliese 1";
                        case 5 -> "70 Ophiuchi";
                        case 6 -> "Stein 2051";
                        default -> "Unknown";
                    };
                    info.add(ChatFormatting.GOLD + "Solar System: " + ChatFormatting.GREEN + systemName);
                }

                return info.isEmpty() ? null : String.join("\n", info);
            });

    public static void init(){
    }
}
