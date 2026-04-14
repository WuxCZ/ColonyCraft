package cz.wux.colonycraft.data;

/**
 * All colonist job types, inspired by Colony Survival.
 * Some jobs (like Berry Farmer) work area-based without needing a workstation block.
 */
public enum ColonistJob {

    // -- Unemployed --
    UNEMPLOYED("Unemployed", "none", false),

    // -- Resource gathering --
    WOODCUTTER    ("Woodcutter",     "woodcutter_bench",   true),
    FORESTER      ("Forester",       "forester_hut",       true),
    MINER         ("Miner",          "miner_hut",          true),
    FARMER        ("Farmer",         "farmer_hut",         true),
    BERRY_FARMER  ("Berry Farmer",   "berry_farm",         false),
    FISHERMAN     ("Fisherman",      "fishing_hut",        false),
    WATER_GATHERER("Water Gatherer", "water_well",         true),

    // -- Processing --
    COOK        ("Cook",         "stove",              true),
    SMELTER     ("Smelter",      "bloomery",           true),
    BLACKSMITH  ("Blacksmith",   "blast_furnace",      true),
    TANNER      ("Tanner",       "tanners_bench",      true),
    TAILOR      ("Tailor",       "tailor_shop",        true),
    FLETCHER    ("Fletcher",     "fletcher_bench",     true),
    STONEMASON  ("Stonemason",   "stonemason_bench",   true),
    COMPOSTER   ("Composter",    "compost_bin",        true),
    GRINDER     ("Grinder",      "grindstone_station", true),
    POTTER      ("Potter",       "pottery_station",    true),
    ALCHEMIST   ("Alchemist",    "alchemist_table",    true),
    GLASSBLOWER ("Glassblower",  "glass_furnace",      true),
    BEEKEEPER   ("Beekeeper",    "beehive_station",    true),
    CHICKEN_FARMER("Chicken Farmer","chicken_coop",    true),

    // -- Knowledge --
    RESEARCHER  ("Researcher",   "research_desk",      true),

    // -- Defense (single guard type - zombies/skeletons burn at dawn) --
    GUARD       ("Guard",        "guard_tower",        true);

    public final String displayName;
    public final String jobBlockKey;
    public final boolean requiresBlock;

    ColonistJob(String displayName, String jobBlockKey, boolean requiresBlock) {
        this.displayName   = displayName;
        this.jobBlockKey   = jobBlockKey;
        this.requiresBlock = requiresBlock;
    }

    public boolean isGuard() {
        return this == GUARD;
    }

    public boolean isFarmer() {
        return this == FARMER || this == BERRY_FARMER || this == FORESTER
                || this == CHICKEN_FARMER || this == BEEKEEPER;
    }
}