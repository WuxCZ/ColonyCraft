package cz.wux.colonycraft;

import cz.wux.colonycraft.blockentity.StockpileBlockEntity;
import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.entity.ColonistEntity;
import cz.wux.colonycraft.entity.ColonyMonsterEntity;
import cz.wux.colonycraft.entity.GuardEntity;
import cz.wux.colonycraft.registry.*;
import net.minecraft.sound.SoundCategory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class ColonyCraftMod implements ModInitializer {

    public static final String MOD_ID = "colonycraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final int COLONY_TICK_INTERVAL = 60;
    private static final int FOOD_CONSUME_INTERVAL = 100;
    private int tickCounter  = 0;
    private int foodCounter  = 0;

    @Override
    public void onInitialize() {
        LOGGER.info("ColonyCraft initializing...");

        ModBlocks.initialize();
        ModItems.initialize();
        ModItemGroups.initialize();
        ModBlockEntities.initialize();
        ModEntities.initialize();
        ModScreenHandlers.initialize();
        ModSounds.initialize();

        FabricDefaultAttributeRegistry.register(ModEntities.COLONIST,
                ColonistEntity.createColonistAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.GUARD,
                GuardEntity.createGuardAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.COLONY_MONSTER,
                ColonyMonsterEntity.createMonsterAttributes());

        // Give starter kit to new players (no guidebook - it's in colony management)
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            boolean hotbarEmpty = true;
            for (int i = 0; i < 9; i++) {
                if (!player.getInventory().getStack(i).isEmpty()) { hotbarEmpty = false; break; }
            }
            if (!hotbarEmpty) return;

            server.execute(() -> {
                player.getInventory().setStack(0, new ItemStack(ModItems.COLONY_BANNER_ITEM));
                player.getInventory().setStack(1, new ItemStack(ModItems.STOCKPILE_ITEM));
                player.getInventory().setStack(2, new ItemStack(ModItems.JOB_ASSIGNMENT_BOOK));
                player.getInventory().setStack(3, new ItemStack(ModItems.AREA_WAND));
                player.sendMessage(Text.literal(
                        "\u00a76\u00a7lWelcome to ColonyCraft! \u00a7r\u00a77Press \u00a7e;\u00a77 to open Colony Management."), false);
            });
        });

        // Server tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter < COLONY_TICK_INTERVAL) return;
            tickCounter = 0;
            foodCounter++;
            boolean doConsumeFood = (foodCounter >= FOOD_CONSUME_INTERVAL);
            if (doConsumeFood) foodCounter = 0;

            ColonyManager mgr = ColonyManager.get(server);
            Collection<ColonyData> colonies = mgr.getAllColonies();
            if (colonies.isEmpty()) return;

            ServerWorld overworld = server.getOverworld();
            long dayTime = overworld.getTimeOfDay() % 24000;

            for (ColonyData colony : colonies) {

                // -- Sync food from stockpile every colony tick --
                BlockPos sp = colony.getStockpilePos();
                if (sp != null && overworld.getBlockEntity(sp) instanceof StockpileBlockEntity stockpile) {
                    stockpile.syncFoodToColony();
                }

                // -- Food consumption (colonists eat from stockpile) --
                if (doConsumeFood && sp != null) {
                    var be = overworld.getBlockEntity(sp);
                    if (be instanceof StockpileBlockEntity stockpile) {
                        int colCount = colony.getColonistCount();
                        for (int i = 0; i < colCount; i++) {
                            stockpile.consumeOneFoodItem();
                        }
                        stockpile.syncFoodToColony();
                    }
                    mgr.markDirty();
                }

                // -- Population growth --
                if (colony.canSpawnMoreColonists() && colony.getFoodUnits() > 20) {
                    BlockPos bp = colony.getBannerPos();
                    if (bp != null) {
                        ServerWorld world = server.getWorld(World.OVERWORLD);
                        if (world != null) {
                            ColonistEntity.spawnForColony(world, colony, bp, mgr);
                        }
                    }
                }

                // -- Night wave: spawn zombies and skeletons --
                if (dayTime >= 13000 && dayTime <= 13100) {
                    BlockPos bp = colony.getBannerPos();
                    if (bp != null) {
                        ServerWorld world = server.getWorld(World.OVERWORLD);
                        if (world != null) {
                            world.playSound(null, bp, ModSounds.WAVE_HORN,
                                    SoundCategory.HOSTILE, 2.0f, 0.8f);
                            ColonyMonsterEntity.spawnWave(world, colony, bp);
                        }
                    }
                    colony.incrementDays();
                    mgr.markDirty();
                }
            }
        });

        LOGGER.info("ColonyCraft initialized! Good luck defending your colony.");
    }
}