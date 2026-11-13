package net.cu5tmtp.GregECore.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;

public class GreggyItems {

    public static Material MYTHRIL, NOBELIUM, FRANKLINITE, LITHIUM_HYDROXIDE, LITHIUM_AMALGAMATION, PLUTONIUM_HEXAFLUORIDE, SUPERELEMENT27, AWAKENED_DRACONIUM;
    public static Material DEIONIZED_WATER, SUPERHEATED_SOLAR;
    public static Material MANASTEEL_CABLE, TWILIGHT_ALLOY_CABLE, DESH_CABLE, MALACHITE_CABLE, FORGOTTEN_INGOT_CABLE, BLOOD_INFUSED_CABLE, BACTERIAL_MATTER_CABLE, DRACONIUM_CABLE;
    public static Material AER, AQUA, IGNIS, TERRA, ORDO, PERDITIO;
    public static Material DESARIUM, IMPLEMENTIUM, TAMED_WORLD_EATER_ESSENSE;

    public static Material DESH_ROTOR, MALACHITE_ROTOR;

    public static Material buildFluidMaterial(String name, int color, String formula) {
        return new Material.Builder(GregECore.id(name))
                .fluid()
                .color(color)
                .iconSet(MaterialIconSet.FLUID)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .formula(formula)
                .buildAndRegister();
    }

    public static void register(){
        DESARIUM = new Material.Builder(GregECore.id("desarium"))
                .gem().color(0x9B59B6).iconSet(MaterialIconSet.DIAMOND).buildAndRegister();
        IMPLEMENTIUM = new Material.Builder(GregECore.id("implementium"))
                .gem().color(0x38A65A).iconSet(MaterialIconSet.EMERALD).buildAndRegister();
        TAMED_WORLD_EATER_ESSENSE = new Material.Builder(GregECore.id("tamed_world_eater_essense"))
                .ingot().color(0x3B1656).iconSet(MaterialIconSet.SHINY).buildAndRegister();

        //items
        MYTHRIL = new Material.Builder(GregECore.id("mythril"))
                .ingot()
                .color(0x63A2B0)
                .iconSet(MaterialIconSet.SHINY)
                .formula("Mt")
                .buildAndRegister();

        NOBELIUM = new Material.Builder(GregECore.id("nobelium"))
                .ingot()
                .color(0xFFA500)
                .iconSet(MaterialIconSet.SHINY)
                .flags(MaterialFlags.GENERATE_BOLT_SCREW)
                .formula("No")
                .buildAndRegister();

        FRANKLINITE = new Material.Builder(GregECore.id("franklinite"))
                .ingot()
                .color(0x964B00)
                .iconSet(MaterialIconSet.ROUGH)
                .formula("(Zn,Mn,Fe)2+(Fe,Mn)3+2O4")
                .buildAndRegister();

        LITHIUM_HYDROXIDE = new Material.Builder(GregECore.id("lithium_hydroxide"))
                .dust()
                .color(0x7393B3)
                .formula("LiOH")
                .buildAndRegister();

        LITHIUM_AMALGAMATION = new Material.Builder(GregECore.id("lithium_amalgamation"))
                .dust()
                .color(0x739FF3)
                .formula("LiHg₃")
                .buildAndRegister();

        PLUTONIUM_HEXAFLUORIDE = new Material.Builder(GregECore.id("plutonium_hexafluoride"))
                .dust()
                .color(0x141414)
                .formula("PuF₆")
                .buildAndRegister();

        SUPERELEMENT27 = new Material.Builder(GregECore.id("superelement27"))
                .ingot()
                .liquid(59999)
                .formula("(Qz₂Mp)+(C₉HeVr),(NbQt₆ZnMnFe)+(XeJeQt)")
                .color(0x023020)
                .cableProperties(GTValues.V[GTValues.LuV], 24, 0, true)
                .rotorStats(800, 400, 100.0f, 100000)
                .flags(MaterialFlags.GENERATE_FOIL, MaterialFlags.GENERATE_ROTOR)
                .buildAndRegister();

        AWAKENED_DRACONIUM = new Material.Builder(GregECore.id("awakened_draconium_cable"))
                .formula("Dr*")
                .color(0xF28C28)
                .cableProperties(GTValues.V[GTValues.UHV], 64, 0, true)
                .flags(MaterialFlags.GENERATE_FOIL)
                .buildAndRegister();

        //cables
        MANASTEEL_CABLE = new Material.Builder(GregECore.id("manasteel_cable"))
                .cableProperties(GTValues.V[GTValues.LV], 4, 0, true)
                .color(0x6495ED)
                .formula("Mana₂FE")
                .buildAndRegister();

        TWILIGHT_ALLOY_CABLE = new Material.Builder(GregECore.id("twilight_alloy_cable"))
                .cableProperties(GTValues.V[GTValues.MV], 8, 0, true)
                .color(0x4F7942)
                .formula("☠🐍⚔🍄")
                .buildAndRegister();

        DESH_CABLE = new Material.Builder(GregECore.id("desh_cable"))
                .cableProperties(GTValues.V[GTValues.HV], 12, 0, true)
                .color(0xF28C28)
                .formula("Dh")
                //.rotorStats(400, 200, 20.0f, 25000)
                //.flags(MaterialFlags.GENERATE_ROTOR)
                .buildAndRegister();

        MALACHITE_CABLE = new Material.Builder(GregECore.id("malachite_cable"))
                .cableProperties(GTValues.V[GTValues.EV], 16, 0, true)
                .color(0x0BDA51)
                .formula("Ml")
                //.rotorStats(600, 300, 50.0f, 50000)
                //.flags(MaterialFlags.GENERATE_ROTOR)
                .buildAndRegister();

        FORGOTTEN_INGOT_CABLE = new Material.Builder(GregECore.id("forgotten_ingot_cable"))
                .cableProperties(GTValues.V[GTValues.IV], 20, 0, true)
                .color(0x7DF9FF)
                .formula("???")
                .buildAndRegister();

        BLOOD_INFUSED_CABLE = new Material.Builder(GregECore.id("blood_infused_cable"))
                .cableProperties(GTValues.V[GTValues.ZPM], 24, 0, true)
                .color(0xA52A2A)
                .formula("C₂₉₅₂H₄₆₆₄N₈₁₂O₈₃₂S₈Fe₄")
                .buildAndRegister();

        BACTERIAL_MATTER_CABLE = new Material.Builder(GregECore.id("bacterial_matter_cable"))
                .cableProperties(GTValues.V[GTValues.UV], 28, 0, true)
                .color(0x50C878)
                .formula("CH₁.₈O₀.₅N₀.₂")
                .buildAndRegister();

        DRACONIUM_CABLE = new Material.Builder(GregECore.id("draconium_cable"))
                .cableProperties(GTValues.V[GTValues.UHV], 32, 0, true)
                .flags(MaterialFlags.GENERATE_DENSE, MaterialFlags.GENERATE_PLATE)
                .color(0x5D3FD3)
                .formula("Dc")
                .buildAndRegister();

        //rotors
        MALACHITE_ROTOR = new Material.Builder(GregECore.id("malachite_rotor"))
                .color(0x0BDA51)
                .formula("Ml")
                .rotorStats(600, 300, 50.0f, 50000)
                .flags(MaterialFlags.GENERATE_ROTOR)
                .buildAndRegister();

        DESH_ROTOR = new Material.Builder(GregECore.id("desh_rotor"))
                .color(0xF28C28)
                .formula("Dh")
                .rotorStats(400, 200, 20.0f, 25000)
                .flags(MaterialFlags.GENERATE_ROTOR)
                .buildAndRegister();


        //fluids
        DEIONIZED_WATER = new Material.Builder(GregECore.id("deionized_water"))
                .fluid()
                .color(0xdddddd)
                .iconSet(MaterialIconSet.FLUID)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .formula("H₂O")
                .buildAndRegister();

        SUPERHEATED_SOLAR = new Material.Builder(GregECore.id("superheated_solar"))
                .fluid()
                .color(0xFDDA0D)
                .iconSet(MaterialIconSet.FLUID)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .formula("H₄He₂So*⁺")
                .buildAndRegister();

        //Essences
        AER = new Material.Builder(GregECore.id("aer_essentia"))
                .fluid()
                .color(0xebef10)
                .iconSet(MaterialIconSet.FLUID)
                .flags(MaterialFlags.DISABLE_MATERIAL_RECIPES)
                .formula("༄")
                .buildAndRegister();

        AQUA = new Material.Builder(GregECore.id("aqua_essentia"))
                .fluid()
                .color(0x394cc6)
                .iconSet(MaterialIconSet.FLUID)
                .flags(MaterialFlags.DISABLE_MATERIAL_RECIPES)
                .formula("❛")
                .buildAndRegister();

        IGNIS = new Material.Builder(GregECore.id("ignis_essentia"))
                .fluid()
                .color(0xe21d26)
                .iconSet(MaterialIconSet.FLUID)
                .flags(MaterialFlags.DISABLE_MATERIAL_RECIPES)
                .formula("♨")
                .buildAndRegister();

        TERRA = new Material.Builder(GregECore.id("terra_essentia"))
                .fluid()
                .color(0x1d8339)
                .iconSet(MaterialIconSet.FLUID)
                .flags(MaterialFlags.DISABLE_MATERIAL_RECIPES)
                .formula("♣")
                .buildAndRegister();

        ORDO = new Material.Builder(GregECore.id("ordo_essentia"))
                .fluid()
                .color(0xd0d2d2)
                .iconSet(MaterialIconSet.FLUID)
                .flags(MaterialFlags.DISABLE_MATERIAL_RECIPES)
                .formula("⥀")
                .buildAndRegister();

        PERDITIO = new Material.Builder(GregECore.id("perditio_essentia"))
                .fluid()
                .color(0x434746)
                .iconSet(MaterialIconSet.FLUID)
                .flags(MaterialFlags.DISABLE_MATERIAL_RECIPES)
                .formula("ϟ")
                .buildAndRegister();


        //₀ ₁ ₂ ₃ ₄ ₅ ₆ ₇ ₈ ₉

        buildFluidMaterial("molten_mythril", 0x63A2B0, "Mt");
        buildFluidMaterial("molten_franklinite", 0x964B00, "(Zn,Mn,Fe)2+(Fe,Mn)3+2O4");
        buildFluidMaterial("xenozene_gas", 0xAAFF00, "C₆H₅Xe");
        buildFluidMaterial("nobalureium_gas", 0x6495ED, "Nb₂");
        buildFluidMaterial("quadractik_gas", 0xB4C424, "Qt₄Cl₂");
        buildFluidMaterial("jelenogas_gas", 0x808000,"C₂H₅Je");
        buildFluidMaterial("verci_54", 0x9F2B68, "Vr₅₄He₂");
        buildFluidMaterial("manopered_36", 0x5D3FD3,"Mp₃₆ArLi");
        buildFluidMaterial("quenzin", 0x770737, "Qz₂N₄");
    }
}
