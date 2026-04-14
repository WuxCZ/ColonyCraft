package cz.wux.colonycraft.data;

/**
 * All colonist job types, inspired by Colony Survival.
 * Some jobs (like Berry Farmer) work area-based without needing a workstation block.
 */
public enum ColonistJob {

    // -- Unemployed --
    UNEMPLOYED("Unemployed", "none", false, 0),

    // -- Resource gathering --
    WOODCUTTER    ("Woodcutter",     "woodcutter_bench",   true,  16),
    FORESTER      ("Forester",       "forester_hut",       true,  16),
    MINER         ("Miner",          "miner_hut",          true,  12),
    FARMER        ("Farmer",         "farmer_hut",         true,  10),
    BERRY_FARMER  ("Berry Farmer",   "berry_farm",         false, 10),
    FISHERMAN     ("Fisherman",      "fishing_hut",        false, 10),
    WATER_GATHERER("Water Gatherer", "water_well",         true,  0),

    // -- Processing --
    COOK        ("Cook",         "stove",              true,  0),
    SMELTER     ("Smelter",      "bloomery",           true,  0),
    BLACKSMITH  ("Blacksmith",   "blast_furnace",      true,  0),
    TANNER      ("Tanner",       "tanners_bench",      true,  0),
    TAILOR      ("Tailor",       "tailor_shop",        true,  0),
    FLETCHER    ("Fletcher",     "fletcher_bench",     true,  0),
    STONEMASON  ("Stonemason",   "stonemason_bench",   true,  0),
    COMPOSTER   ("Composter",    "compost_bin",        true,  0),
    GRINDER     ("Grinder",      "grindstone_station", true,  0),
    POTTER      ("Potter",       "pottery_station",    true,  0),
    ALCHEMIST   ("Alchemist",    "alchemist_table",    true,  0),
    GLASSBLOWER ("Glassblower",  "glass_furnace",      true,  0),
    BEEKEEPER   ("Beekeeper",    "beehive_station",    true,  10),
    CHICKEN_FARMER("Chicken Farmer","chicken_coop",    true,  10),

    // -- Knowledge --
    RESEARCHER  ("Researcher",   "research_desk",      true,  0),

    // -- Defense --
    GUARD_SWORD ("Guard (Sword)", "guard_tower",        false, 32),
    GUARD_BOW   ("Guard (Bow)",   "guard_tower",        false, 16);

    public final String displayName;
    public final String jobBlockKey;
    public final boolean requiresBlock;
    /** Max area width/depth in blocks. 0 = no area needed (workshop-type jobs). */
    public final int maxAreaSize;

    ColonistJob(String displayName, String jobBlockKey, boolean requiresBlock, int maxAreaSize) {
        this.displayName   = displayName;
        this.jobBlockKey   = jobBlockKey;
        this.requiresBlock = requiresBlock;
        this.maxAreaSize   = maxAreaSize;
    }

    /** Returns true if this job uses a work area (farming, forestry, mining, etc.) */
    public boolean usesArea() {
        return maxAreaSize > 0;
    }

    public boolean isGuard() {
        return this == GUARD_SWORD || this == GUARD_BOW;
    }

    /** Migration helper: converts old save strings (e.g. "GUARD") to current enum values. */
    public static ColonistJob fromString(String name) {
        if ("GUARD".equals(name)) return GUARD_SWORD;
        try { return valueOf(name); }
        catch (Exception e) { return UNEMPLOYED; }
    }

    public boolean isFarmer() {
        return this == FARMER || this == BERRY_FARMER || this == FORESTER
                || this == CHICKEN_FARMER || this == BEEKEEPER;
    }
}