package cz.wux.colonycraft.registry;

import cz.wux.colonycraft.block.*;
import cz.wux.colonycraft.block.job.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

/**
 * Registers all ColonyCraft blocks.
 */
public class ModBlocks {

    // ── Core colony blocks ────────────────────────────────────────────────────
    public static final ColonyBannerBlock  COLONY_BANNER = register("colony_banner",
            new ColonyBannerBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.GOLD)
                    .strength(3.0f, 9.0f)
                    .sounds(BlockSoundGroup.WOOD)
                    .nonOpaque()));

    public static final StockpileBlock STOCKPILE = register("stockpile",
            new StockpileBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.OAK_TAN)
                    .strength(2.5f, 6.0f)
                    .sounds(BlockSoundGroup.WOOD)));

    public static final ResearchTableBlock RESEARCH_TABLE = register("research_table",
            new ResearchTableBlock(AbstractBlock.Settings.create()
                    .mapColor(MapColor.BROWN)
                    .strength(2.0f)
                    .sounds(BlockSoundGroup.WOOD)));

    // ── Job blocks ────────────────────────────────────────────────────────────
    public static final WoodcutterBlock   WOODCUTTER_BENCH   = registerJob("woodcutter_bench",   new WoodcutterBlock());
    public static final ForesterBlock     FORESTER_HUT       = registerJob("forester_hut",       new ForesterBlock());
    public static final MinerBlock        MINER_HUT          = registerJob("miner_hut",          new MinerBlock());
    public static final FarmerBlock       FARMER_HUT         = registerJob("farmer_hut",         new FarmerBlock());
    public static final BerryFarmerBlock  BERRY_FARM         = registerJob("berry_farm",         new BerryFarmerBlock());
    public static final FishermanBlock    FISHING_HUT        = registerJob("fishing_hut",        new FishermanBlock());
    public static final WaterGathererBlock WATER_WELL        = registerJob("water_well",         new WaterGathererBlock());
    public static final CookBlock         STOVE              = registerJob("stove",              new CookBlock());
    public static final SmelterBlock      BLOOMERY           = registerJob("bloomery",           new SmelterBlock());
    public static final BlacksmithBlock   BLAST_FURNACE_JOB  = registerJob("blast_furnace",      new BlacksmithBlock());
    public static final TailorBlock       TAILOR_SHOP        = registerJob("tailor_shop",        new TailorBlock());
    public static final FletcherBlock     FLETCHER_BENCH     = registerJob("fletcher_bench",     new FletcherBlock());
    public static final StonemasonBlock   STONEMASON_BENCH   = registerJob("stonemason_bench",   new StonemasonBlock());
    public static final ComposterJobBlock COMPOST_BIN        = registerJob("compost_bin",        new ComposterJobBlock());
    public static final GrinderBlock      GRINDSTONE_STATION = registerJob("grindstone_station", new GrinderBlock());
    public static final AlchemistBlock    ALCHEMIST_TABLE    = registerJob("alchemist_table",    new AlchemistBlock());
    public static final ResearcherBlock   RESEARCH_DESK      = registerJob("research_desk",      new ResearcherBlock());
    public static final GuardTowerBlock   GUARD_TOWER        = registerJob("guard_tower",        new GuardTowerBlock());
    public static final ChickenFarmerBlock CHICKEN_COOP      = registerJob("chicken_coop",       new ChickenFarmerBlock());
    public static final BeekeeperBlock    BEEHIVE_STATION    = registerJob("beehive_station",    new BeekeeperBlock());
    public static final TannersBlock      TANNERS_BENCH      = registerJob("tanners_bench",      new TannersBlock());
    public static final PotteryBlock      POTTERY_STATION    = registerJob("pottery_station",    new PotteryBlock());
    public static final GlassblowerBlock  GLASS_FURNACE      = registerJob("glass_furnace",      new GlassblowerBlock());

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static <T extends Block> T register(String name, T block) {
        return Registry.register(Registries.BLOCK, Identifier.of("colonycraft", name), block);
    }

    private static <T extends Block> T registerJob(String name, T block) {
        return Registry.register(Registries.BLOCK, Identifier.of("colonycraft", name), block);
    }

    /** Call from mod initialiser to trigger class loading. */
    public static void initialize() {}
}
