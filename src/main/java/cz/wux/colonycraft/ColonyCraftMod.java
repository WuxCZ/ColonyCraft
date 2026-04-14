package cz.wux.colonycraft;

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

/**
 * Main mod initialiser – runs on both client and server (dedicated + integrated).
 */
public class ColonyCraftMod implements ModInitializer {

    public static final String MOD_ID = "colonycraft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** How many ticks between colony "tick" updates (once every ~3 seconds). */
    private static final int COLONY_TICK_INTERVAL = 60;
    /** Food is consumed once every this many colony-ticks (~5 min real time per colonist). */
    private static final int FOOD_CONSUME_INTERVAL = 100;
    private int tickCounter  = 0;
    private int foodCounter  = 0;

    @Override
    public void onInitialize() {
        LOGGER.info("ColonyCraft initializing…");

        // Register all content
        ModBlocks.initialize();
        ModItems.initialize();
        ModItemGroups.initialize();
        ModBlockEntities.initialize();
        ModEntities.initialize();
        ModScreenHandlers.initialize();
        ModSounds.initialize();

        // Register default entity attributes
        FabricDefaultAttributeRegistry.register(ModEntities.COLONIST,
                ColonistEntity.createColonistAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.GUARD,
                GuardEntity.createGuardAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.COLONY_MONSTER,
                ColonyMonsterEntity.createMonsterAttributes());

        // Give starter kit + guidebook to brand-new players
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            // "New player" = completely empty inventory (slots 0–8 are hotbar)
            boolean hotbarEmpty = true;
            for (int i = 0; i < 9; i++) {
                if (!player.getInventory().getStack(i).isEmpty()) { hotbarEmpty = false; break; }
            }
            if (!hotbarEmpty) return;

            server.execute(() -> {
                player.getInventory().setStack(0, new ItemStack(ModItems.COLONY_BANNER_ITEM));
                player.getInventory().setStack(1, new ItemStack(ModItems.STOCKPILE_ITEM));
                player.getInventory().setStack(2, new ItemStack(ModItems.JOB_ASSIGNMENT_BOOK));
                player.getInventory().setStack(3, new ItemStack(ModItems.GUIDEBOOK));
                player.sendMessage(Text.literal(
                        "§6§lWelcome to ColonyCraft! §r§7Right-click the §aGuide Book§7 in your hotbar to learn how to play."), false);
            });
        });

        // Server tick: manage food consumption, wave spawning, population growth
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter < COLONY_TICK_INTERVAL) return;
            tickCounter = 0;
            foodCounter++;
            boolean doConsumFood = (foodCounter >= FOOD_CONSUME_INTERVAL);
            if (doConsumFood) foodCounter = 0;

            ColonyManager mgr = ColonyManager.get(server);
            Collection<ColonyData> colonies = mgr.getAllColonies();
            if (colonies.isEmpty()) return;

            ServerWorld overworld = server.getOverworld();
            long dayTime = overworld.getTimeOfDay() % 24000;

            for (ColonyData colony : colonies) {

                // ── Food consumption (~every 5 min real time per colonist) ─
                if (doConsumFood) {
                    int colCount = colony.getColonistCount();
                    for (int i = 0; i < colCount; i++) colony.consumeFood();
                    mgr.markDirty();
                }

                // ── Population growth (spawn new colonist if food & cap OK) ─
                if (colony.canSpawnMoreColonists() && colony.getFoodUnits() > 20) {
                    BlockPos bp = colony.getBannerPos();
                    if (bp != null) {
                        ServerWorld world = server.getWorld(World.OVERWORLD);
                        if (world != null) {
                            ColonistEntity.spawnForColony(world, colony, bp, mgr);
                        }
                    }
                }

                // ── Night wave spawning ────────────────────────────────────
                if (dayTime >= 13000 && dayTime <= 13100) {
                    BlockPos bp = colony.getBannerPos();
                    if (bp != null) {
                        ServerWorld world = server.getWorld(World.OVERWORLD);
                        if (world != null) {
                            // Play wave horn at banner position
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
