package cz.wux.colonycraft.data;

/**
 * All colonist job types, mapped 1:1 from Colony Survival's npctypes.json.
 * Each job has a display name, a corresponding job block key, and whether it
 * requires a physical workstation block.
 */
public enum ColonistJob {

    // ── Unemployed ───────────────────────────────────────────────────────────
    UNEMPLOYED("Unemployed", "none", false),

    // ── Resource gathering ────────────────────────────────────────────────────
    WOODCUTTER  ("Woodcutter",   "woodcutter_bench",   true),
    FORESTER    ("Forester",     "forester_hut",       true),
    MINER       ("Miner",        "miner_hut",          true),
    FARMER      ("Farmer",       "farmer_hut",         true),
    BERRY_FARMER("Berry Farmer", "berry_farm",         true),
    FISHERMAN   ("Fisherman",    "fishing_hut",        true),
    WATER_GATHERER("Water Gatherer", "water_well",     true),

    // ── Processing ────────────────────────────────────────────────────────────
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

    // ── Knowledge ─────────────────────────────────────────────────────────────
    RESEARCHER  ("Researcher",   "research_desk",      true),

    // ── Guards ────────────────────────────────────────────────────────────────
    GUARD_BOW      ("Guard (Bow)",       "guard_tower",  true),
    GUARD_CROSSBOW ("Guard (Crossbow)", "guard_tower",   true),
    GUARD_MUSKET   ("Guard (Musket)",   "guard_tower",   true);

    // ─────────────────────────────────────────────────────────────────────────

    public final String displayName;
    /** Key matching the block registered in ModBlocks (may be "none"). */
    public final String jobBlockKey;
    /** Whether the colonist must stand at a physical block to work. */
    public final boolean requiresBlock;

    ColonistJob(String displayName, String jobBlockKey, boolean requiresBlock) {
        this.displayName   = displayName;
        this.jobBlockKey   = jobBlockKey;
        this.requiresBlock = requiresBlock;
    }

    public boolean isGuard() {
        return this == GUARD_BOW || this == GUARD_CROSSBOW || this == GUARD_MUSKET;
    }

    public boolean isFarmer() {
        return this == FARMER || this == BERRY_FARMER || this == FORESTER
                || this == CHICKEN_FARMER || this == BEEKEEPER;
    }
}
