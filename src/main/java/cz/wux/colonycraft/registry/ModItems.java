package cz.wux.colonycraft.registry;

import cz.wux.colonycraft.item.GuidebookItem;
import cz.wux.colonycraft.item.JobAssignmentBook;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * Registers all ColonyCraft items (including BlockItems for every block).
 */
public class ModItems {

    // Guidebook (tutorial)
    public static final GuidebookItem GUIDEBOOK = reg("guidebook",
            key -> new GuidebookItem(new Item.Settings().maxCount(1).registryKey(key)));

    // Job assignment tool
    public static final JobAssignmentBook JOB_ASSIGNMENT_BOOK = reg("job_assignment_book",
            key -> new JobAssignmentBook(new Item.Settings().maxCount(1).registryKey(key)));

    // Block items (auto-generated)
    public static final BlockItem COLONY_BANNER_ITEM      = regBlock("colony_banner",       ModBlocks.COLONY_BANNER);
    public static final BlockItem STOCKPILE_ITEM          = regBlock("stockpile",           ModBlocks.STOCKPILE);
    public static final BlockItem RESEARCH_TABLE_ITEM     = regBlock("research_table",      ModBlocks.RESEARCH_TABLE);
    public static final BlockItem WOODCUTTER_BENCH_ITEM   = regBlock("woodcutter_bench",    ModBlocks.WOODCUTTER_BENCH);
    public static final BlockItem FORESTER_HUT_ITEM       = regBlock("forester_hut",        ModBlocks.FORESTER_HUT);
    public static final BlockItem MINER_HUT_ITEM          = regBlock("miner_hut",           ModBlocks.MINER_HUT);
    public static final BlockItem FARMER_HUT_ITEM         = regBlock("farmer_hut",          ModBlocks.FARMER_HUT);
    public static final BlockItem BERRY_FARM_ITEM         = regBlock("berry_farm",          ModBlocks.BERRY_FARM);
    public static final BlockItem FISHING_HUT_ITEM        = regBlock("fishing_hut",         ModBlocks.FISHING_HUT);
    public static final BlockItem WATER_WELL_ITEM         = regBlock("water_well",          ModBlocks.WATER_WELL);
    public static final BlockItem STOVE_ITEM              = regBlock("stove",               ModBlocks.STOVE);
    public static final BlockItem BLOOMERY_ITEM           = regBlock("bloomery",            ModBlocks.BLOOMERY);
    public static final BlockItem BLAST_FURNACE_JOB_ITEM  = regBlock("blast_furnace",       ModBlocks.BLAST_FURNACE_JOB);
    public static final BlockItem TAILOR_SHOP_ITEM        = regBlock("tailor_shop",         ModBlocks.TAILOR_SHOP);
    public static final BlockItem FLETCHER_BENCH_ITEM     = regBlock("fletcher_bench",      ModBlocks.FLETCHER_BENCH);
    public static final BlockItem STONEMASON_BENCH_ITEM   = regBlock("stonemason_bench",    ModBlocks.STONEMASON_BENCH);
    public static final BlockItem COMPOST_BIN_ITEM        = regBlock("compost_bin",         ModBlocks.COMPOST_BIN);
    public static final BlockItem GRINDSTONE_STATION_ITEM = regBlock("grindstone_station",  ModBlocks.GRINDSTONE_STATION);
    public static final BlockItem ALCHEMIST_TABLE_ITEM    = regBlock("alchemist_table",     ModBlocks.ALCHEMIST_TABLE);
    public static final BlockItem RESEARCH_DESK_ITEM      = regBlock("research_desk",       ModBlocks.RESEARCH_DESK);
    public static final BlockItem GUARD_TOWER_ITEM        = regBlock("guard_tower",         ModBlocks.GUARD_TOWER);
    public static final BlockItem CHICKEN_COOP_ITEM       = regBlock("chicken_coop",        ModBlocks.CHICKEN_COOP);
    public static final BlockItem BEEHIVE_STATION_ITEM    = regBlock("beehive_station",     ModBlocks.BEEHIVE_STATION);
    public static final BlockItem TANNERS_BENCH_ITEM      = regBlock("tanners_bench",       ModBlocks.TANNERS_BENCH);
    public static final BlockItem POTTERY_STATION_ITEM    = regBlock("pottery_station",     ModBlocks.POTTERY_STATION);
    public static final BlockItem GLASS_FURNACE_ITEM      = regBlock("glass_furnace",       ModBlocks.GLASS_FURNACE);

    // Helpers

    private static <T extends Item> T reg(String name, java.util.function.Function<RegistryKey<Item>, T> factory) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of("colonycraft", name));
        return Registry.register(Registries.ITEM, key, factory.apply(key));
    }

    private static BlockItem regBlock(String name, Block block) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of("colonycraft", name));
        return Registry.register(Registries.ITEM, key, new BlockItem(block, new Item.Settings().registryKey(key)));
    }

    public static void initialize() {}
}