package cz.wux.colonycraft.registry;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Registers the ColonyCraft creative tab so all mod items appear in creative inventory.
 */
public class ModItemGroups {

    public static final RegistryKey<ItemGroup> COLONY_CRAFT_KEY = RegistryKey.of(
            RegistryKeys.ITEM_GROUP,
            Identifier.of("colonycraft", "main"));

    public static final ItemGroup COLONY_CRAFT = Registry.register(
            Registries.ITEM_GROUP,
            COLONY_CRAFT_KEY,
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.COLONY_BANNER_ITEM))
                    .displayName(Text.translatable("itemgroup.colonycraft.main"))
                    .entries((context, entries) -> {
                        // Guidebook first so players see it
                        entries.add(ModItems.GUIDEBOOK);

                        // Core colony blocks
                        entries.add(ModItems.COLONY_BANNER_ITEM);
                        entries.add(ModItems.STOCKPILE_ITEM);
                        entries.add(ModItems.RESEARCH_TABLE_ITEM);
                        entries.add(ModItems.JOB_ASSIGNMENT_BOOK);
                        entries.add(ModItems.AREA_WAND);

                        // Resource gathering jobs
                        entries.add(ModItems.WOODCUTTER_BENCH_ITEM);
                        entries.add(ModItems.FORESTER_HUT_ITEM);
                        entries.add(ModItems.MINER_HUT_ITEM);
                        entries.add(ModItems.FARMER_HUT_ITEM);
                        entries.add(ModItems.BERRY_FARM_ITEM);
                        entries.add(ModItems.FISHING_HUT_ITEM);
                        entries.add(ModItems.WATER_WELL_ITEM);

                        // Processing jobs
                        entries.add(ModItems.STOVE_ITEM);
                        entries.add(ModItems.BLOOMERY_ITEM);
                        entries.add(ModItems.BLAST_FURNACE_JOB_ITEM);
                        entries.add(ModItems.TAILOR_SHOP_ITEM);
                        entries.add(ModItems.FLETCHER_BENCH_ITEM);
                        entries.add(ModItems.STONEMASON_BENCH_ITEM);
                        entries.add(ModItems.COMPOST_BIN_ITEM);
                        entries.add(ModItems.GRINDSTONE_STATION_ITEM);
                        entries.add(ModItems.ALCHEMIST_TABLE_ITEM);
                        entries.add(ModItems.POTTERY_STATION_ITEM);
                        entries.add(ModItems.GLASS_FURNACE_ITEM);
                        entries.add(ModItems.TANNERS_BENCH_ITEM);

                        // Knowledge & defense
                        entries.add(ModItems.RESEARCH_DESK_ITEM);
                        entries.add(ModItems.GUARD_TOWER_ITEM);

                        // Animals
                        entries.add(ModItems.CHICKEN_COOP_ITEM);
                        entries.add(ModItems.BEEHIVE_STATION_ITEM);
                    })
                    .build());

    public static void initialize() {
        // side-effect: static initializer registers the group
    }
}
