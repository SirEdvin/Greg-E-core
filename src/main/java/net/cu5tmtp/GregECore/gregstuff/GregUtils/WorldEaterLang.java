package net.cu5tmtp.GregECore.gregstuff.GregUtils;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

/** English for World Eater text built at runtime (hints, recipe captions, panels), registered for data generation. */
public final class WorldEaterLang {
    private WorldEaterLang() {}

    public static void init() {
        REGISTRATE.addRawLang("gregecore.world_eater.hint.reward", "Free output space for the earned essence");
        REGISTRATE.addRawLang("gregecore.world_eater.hint.report", "Free output space for the survey report");
        REGISTRATE.addRawLang("gregecore.world_eater.hint.demand", "Deliver %s x %s within %ss");
        REGISTRATE.addRawLang("gregecore.world_eater.hint.survey", "Insert an IV Sensor to survey nearby machines (500 needed)");
        REGISTRATE.addRawLang("gregecore.world_eater.hint.surveying", "Surveying nearby machines: %s%%");
        REGISTRATE.addRawLang("gregecore.world_eater.hint.casing", "Replace the casings in the middle with colored ones to upgrade to tier 2");
        REGISTRATE.addRawLang("gregecore.world_eater.hint.intention", "Install a Safety or Bravery Hatch at the rear face center");
        REGISTRATE.addRawLang("gregecore.world_eater.hint.test", "Insert Desarium to run the preparation test");
        REGISTRATE.addRawLang("gregecore.world_eater.hint.real", "Ready: insert Implementium for real runs");
        REGISTRATE.addRawLang("gregecore.me.online", "ME network: online");
        REGISTRATE.addRawLang("gregecore.me.offline", "ME network: offline");
        REGISTRATE.addRawLang("gregecore.me_demand_hatch.demand", "Demand: %s x %s");
        REGISTRATE.addRawLang("gregecore.me_demand_hatch.no_demand", "No real-run demand");
        REGISTRATE.addRawLang("gregecore.creative_demand_pattern_provider.pending", "Pending deliveries: %s");
        REGISTRATE.addRawLang("gregecore.world_eater.recipe.information_collection.0", "DORMANT");
        REGISTRATE.addRawLang("gregecore.world_eater.recipe.information_collection.1", "500 machines awaken it");
        REGISTRATE.addRawLang("gregecore.world_eater.recipe.casing_conversion.0", "ACTIVE or PREPARED");
        REGISTRATE.addRawLang("gregecore.world_eater.recipe.bravery_crafting.0", "ACTIVE or PREPARED");
        REGISTRATE.addRawLang("gregecore.world_eater.recipe.bravery_crafting.1", "Tier 2");
        REGISTRATE.addRawLang("gregecore.world_eater.recipe.preparation_test.0", "ACTIVE → PREPARED");
        REGISTRATE.addRawLang("gregecore.world_eater.recipe.preparation_test.1", "Tier 2, hatch, demand");
        REGISTRATE.addRawLang("gregecore.world_eater.recipe.essence_production.0", "PREPARED");
        REGISTRATE.addRawLang("gregecore.world_eater.recipe.essence_production.1", "Tier 2, hatch, demand");
    }
}
