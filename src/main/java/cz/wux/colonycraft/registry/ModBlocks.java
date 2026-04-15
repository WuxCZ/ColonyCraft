package cz.wux.colonycraft.registry;

import cz.wux.colonycraft.block.*;
import cz.wux.colonycraft.block.job.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.function.Function;

/**
 * Registers all ColonyCraft blocks.
 */
public class ModBlocks {

    // ── Core colony blocks ────────────────────────────────────────────────────
    public static final ColonyBannerBlock COLONY_BANNER = reg("colony_banner",
            s -> new ColonyBannerBlock(s.mapColor(MapColor.GOLD).strength(3.0f, 9.0f).sounds(BlockSoundGroup.WOOD).nonOpaque()));

    public static final StockpileBlock STOCKPILE = reg("stockpile",
            s -> new StockpileBlock(s.mapColor(MapColor.OAK_TAN).strength(2.5f, 6.0f).sounds(BlockSoundGroup.WOOD)));

    public static final ResearchTableBlock RESEARCH_TABLE = reg("research_table",
            s -> new ResearchTableBlock(s.mapColor(MapColor.BROWN).strength(2.0f).sounds(BlockSoundGroup.WOOD)));

    // ── Job blocks ────────────────────────────────────────────────────────────
    public static final WoodcutterBlock   WOODCUTTER_BENCH   = regJob("woodcutter_bench",   WoodcutterBlock::new);
    public static final ForesterBlock     FORESTER_HUT       = regJob("forester_hut",       ForesterBlock::new);
    public static final MinerBlock        MINER_HUT          = regJob("miner_hut",          MinerBlock::new);
    public static final FarmerBlock       FARMER_HUT         = regJob("farmer_hut",         FarmerBlock::new);
    public static final BerryFarmerBlock  BERRY_FARM         = regJob("berry_farm",         BerryFarmerBlock::new);
    public static final FishermanBlock    FISHING_HUT        = regJob("fishing_hut",        FishermanBlock::new);
    public static final WaterGathererBlock WATER_WELL        = regJob("water_well",         WaterGathererBlock::new);
    public static final CookBlock         STOVE              = regJob("stove",              CookBlock::new);
    public static final SmelterBlock      BLOOMERY           = regJob("bloomery",           SmelterBlock::new);
    public static final BlacksmithBlock   BLAST_FURNACE_JOB  = regJob("blast_furnace",      BlacksmithBlock::new);
    public static final TailorBlock       TAILOR_SHOP        = regJob("tailor_shop",        TailorBlock::new);
    public static final FletcherBlock     FLETCHER_BENCH     = regJob("fletcher_bench",     FletcherBlock::new);
    public static final StonemasonBlock   STONEMASON_BENCH   = regJob("stonemason_bench",   StonemasonBlock::new);
    public static final ComposterJobBlock COMPOST_BIN        = regJob("compost_bin",        ComposterJobBlock::new);
    public static final GrinderBlock      GRINDSTONE_STATION = regJob("grindstone_station", GrinderBlock::new);
    public static final AlchemistBlock    ALCHEMIST_TABLE    = regJob("alchemist_table",    AlchemistBlock::new);
    public static final ResearcherBlock   RESEARCH_DESK      = regJob("research_desk",      ResearcherBlock::new);
    public static final GuardTowerBlock   GUARD_TOWER        = regJob("guard_tower",        GuardTowerBlock::new);
    public static final ChickenFarmerBlock CHICKEN_COOP      = regJob("chicken_coop",       ChickenFarmerBlock::new);
    public static final BeekeeperBlock    BEEHIVE_STATION    = regJob("beehive_station",    BeekeeperBlock::new);
    public static final TannersBlock      TANNERS_BENCH      = regJob("tanners_bench",      TannersBlock::new);
    public static final PotteryBlock      POTTERY_STATION    = regJob("pottery_station",    PotteryBlock::new);
    public static final GlassblowerBlock  GLASS_FURNACE      = regJob("glass_furnace",      GlassblowerBlock::new);
    public static final BuilderBlock      BUILDER_HUT        = regJob("builder_hut",        BuilderBlock::new);
    public static final DiggerBlock       DIGGER_HUT         = regJob("digger_hut",         DiggerBlock::new);
    public static final ShepherdBlock     SHEPHERD_HUT       = regJob("shepherd_hut",       ShepherdBlock::new);
    public static final CowHerderBlock    COW_BARN           = regJob("cow_barn",           CowHerderBlock::new);

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Creates key, injects into settings, constructs block, then registers. */
    private static <T extends Block> T reg(String name, Function<AbstractBlock.Settings, T> factory) {
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of("colonycraft", name));
        return Registry.register(Registries.BLOCK, key, factory.apply(AbstractBlock.Settings.create().registryKey(key)));
    }

    /** Same as {@link #reg} but with standard job-block settings pre-applied. */
    private static <T extends Block> T regJob(String name, Function<AbstractBlock.Settings, T> factory) {
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of("colonycraft", name));
        AbstractBlock.Settings s = AbstractBlock.Settings.create()
                .mapColor(MapColor.OAK_TAN)
                .strength(2.0f, 6.0f)
                .sounds(BlockSoundGroup.WOOD)
                .registryKey(key);
        return Registry.register(Registries.BLOCK, key, factory.apply(s));
    }

    /** Call from mod initialiser to trigger class loading. */
    public static void initialize() {}
}
