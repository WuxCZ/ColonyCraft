package cz.wux.colonycraft.registry;

import cz.wux.colonycraft.blockentity.ColonyBannerBlockEntity;
import cz.wux.colonycraft.blockentity.JobBlockEntity;
import cz.wux.colonycraft.blockentity.ResearchTableBlockEntity;
import cz.wux.colonycraft.blockentity.StockpileBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static final BlockEntityType<ColonyBannerBlockEntity> COLONY_BANNER =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of("colonycraft", "colony_banner"),
                    FabricBlockEntityTypeBuilder.create(ColonyBannerBlockEntity::new,
                            ModBlocks.COLONY_BANNER).build());

    public static final BlockEntityType<StockpileBlockEntity> STOCKPILE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of("colonycraft", "stockpile"),
                    FabricBlockEntityTypeBuilder.create(StockpileBlockEntity::new,
                            ModBlocks.STOCKPILE).build());

    public static final BlockEntityType<ResearchTableBlockEntity> RESEARCH_TABLE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of("colonycraft", "research_table"),
                    FabricBlockEntityTypeBuilder.create(ResearchTableBlockEntity::new,
                            ModBlocks.RESEARCH_TABLE).build());

    public static final BlockEntityType<JobBlockEntity> JOB_BLOCK =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of("colonycraft", "job_block"),
                    FabricBlockEntityTypeBuilder.create(JobBlockEntity::new,
                            // All job blocks share this entity type
                            ModBlocks.WOODCUTTER_BENCH,
                            ModBlocks.FORESTER_HUT,
                            ModBlocks.MINER_HUT,
                            ModBlocks.FARMER_HUT,
                            ModBlocks.BERRY_FARM,
                            ModBlocks.FISHING_HUT,
                            ModBlocks.WATER_WELL,
                            ModBlocks.STOVE,
                            ModBlocks.BLOOMERY,
                            ModBlocks.BLAST_FURNACE_JOB,
                            ModBlocks.TAILOR_SHOP,
                            ModBlocks.FLETCHER_BENCH,
                            ModBlocks.STONEMASON_BENCH,
                            ModBlocks.COMPOST_BIN,
                            ModBlocks.GRINDSTONE_STATION,
                            ModBlocks.ALCHEMIST_TABLE,
                            ModBlocks.RESEARCH_DESK,
                            ModBlocks.GUARD_TOWER,
                            ModBlocks.CHICKEN_COOP,
                            ModBlocks.BEEHIVE_STATION,
                            ModBlocks.TANNERS_BENCH,
                            ModBlocks.POTTERY_STATION,
                            ModBlocks.GLASS_FURNACE
                    ).build());

    public static void initialize() {}
}
