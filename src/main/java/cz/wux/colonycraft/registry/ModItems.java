package cz.wux.colonycraft.registry;

import cz.wux.colonycraft.item.JobAssignmentBook;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registers all ColonyCraft items (including BlockItems for every block).
 */
public class ModItems {

    // ── Job assignment tool ───────────────────────────────────────────────────
    public static final JobAssignmentBook JOB_ASSIGNMENT_BOOK = register("job_assignment_book",
            new JobAssignmentBook(new Item.Settings().maxCount(1)));

    // ── Block items (auto-generated) ──────────────────────────────────────────
    public static final BlockItem COLONY_BANNER_ITEM   = registerBlock("colony_banner",   ModBlocks.COLONY_BANNER);
    public static final BlockItem STOCKPILE_ITEM       = registerBlock("stockpile",       ModBlocks.STOCKPILE);
    public static final BlockItem RESEARCH_TABLE_ITEM  = registerBlock("research_table",  ModBlocks.RESEARCH_TABLE);
    public static final BlockItem WOODCUTTER_BENCH_ITEM= registerBlock("woodcutter_bench",ModBlocks.WOODCUTTER_BENCH);
    public static final BlockItem FORESTER_HUT_ITEM    = registerBlock("forester_hut",    ModBlocks.FORESTER_HUT);
    public static final BlockItem MINER_HUT_ITEM       = registerBlock("miner_hut",       ModBlocks.MINER_HUT);
    public static final BlockItem FARMER_HUT_ITEM      = registerBlock("farmer_hut",      ModBlocks.FARMER_HUT);
    public static final BlockItem BERRY_FARM_ITEM      = registerBlock("berry_farm",      ModBlocks.BERRY_FARM);
    public static final BlockItem FISHING_HUT_ITEM     = registerBlock("fishing_hut",     ModBlocks.FISHING_HUT);
    public static final BlockItem WATER_WELL_ITEM      = registerBlock("water_well",      ModBlocks.WATER_WELL);
    public static final BlockItem STOVE_ITEM           = registerBlock("stove",           ModBlocks.STOVE);
    public static final BlockItem BLOOMERY_ITEM        = registerBlock("bloomery",        ModBlocks.BLOOMERY);
    public static final BlockItem BLAST_FURNACE_JOB_ITEM = registerBlock("blast_furnace",ModBlocks.BLAST_FURNACE_JOB);
    public static final BlockItem TAILOR_SHOP_ITEM     = registerBlock("tailor_shop",     ModBlocks.TAILOR_SHOP);
    public static final BlockItem FLETCHER_BENCH_ITEM  = registerBlock("fletcher_bench",  ModBlocks.FLETCHER_BENCH);
    public static final BlockItem STONEMASON_BENCH_ITEM= registerBlock("stonemason_bench",ModBlocks.STONEMASON_BENCH);
    public static final BlockItem COMPOST_BIN_ITEM     = registerBlock("compost_bin",     ModBlocks.COMPOST_BIN);
    public static final BlockItem GRINDSTONE_STATION_ITEM = registerBlock("grindstone_station", ModBlocks.GRINDSTONE_STATION);
    public static final BlockItem ALCHEMIST_TABLE_ITEM = registerBlock("alchemist_table", ModBlocks.ALCHEMIST_TABLE);
    public static final BlockItem RESEARCH_DESK_ITEM   = registerBlock("research_desk",   ModBlocks.RESEARCH_DESK);
    public static final BlockItem GUARD_TOWER_ITEM     = registerBlock("guard_tower",     ModBlocks.GUARD_TOWER);
    public static final BlockItem CHICKEN_COOP_ITEM    = registerBlock("chicken_coop",    ModBlocks.CHICKEN_COOP);
    public static final BlockItem BEEHIVE_STATION_ITEM = registerBlock("beehive_station", ModBlocks.BEEHIVE_STATION);
    public static final BlockItem TANNERS_BENCH_ITEM   = registerBlock("tanners_bench",   ModBlocks.TANNERS_BENCH);
    public static final BlockItem POTTERY_STATION_ITEM = registerBlock("pottery_station", ModBlocks.POTTERY_STATION);
    public static final BlockItem GLASS_FURNACE_ITEM   = registerBlock("glass_furnace",   ModBlocks.GLASS_FURNACE);

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(Registries.ITEM, Identifier.of("colonycraft", name), item);
    }

    private static BlockItem registerBlock(String name, net.minecraft.block.Block block) {
        return register(name, new BlockItem(block, new Item.Settings()));
    }

    public static void initialize() {}
}
