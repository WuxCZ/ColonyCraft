package cz.wux.colonycraft;

import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.entity.ColonistEntity;
import cz.wux.colonycraft.entity.ColonyMonsterEntity;
import cz.wux.colonycraft.entity.GuardEntity;
import cz.wux.colonycraft.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.server.world.ServerWorld;
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
    private int tickCounter = 0;

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

        // Register default entity attributes
        FabricDefaultAttributeRegistry.register(ModEntities.COLONIST,
                ColonistEntity.createColonistAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.GUARD,
                GuardEntity.createGuardAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.COLONY_MONSTER,
                ColonyMonsterEntity.createMonsterAttributes());

        // Server tick: manage food consumption, wave spawning, population growth
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter < COLONY_TICK_INTERVAL) return;
            tickCounter = 0;

            ColonyManager mgr = ColonyManager.get(server);
            Collection<ColonyData> colonies = mgr.getAllColonies();
            if (colonies.isEmpty()) return;

            ServerWorld overworld = server.getOverworld();
            long dayTime = overworld.getTimeOfDay() % 24000;

            for (ColonyData colony : colonies) {

                // ── Food consumption (every tick interval per colonist) ─────
                int colCount = colony.getColonistCount();
                for (int i = 0; i < colCount; i++) {
                    colony.consumeFood();
                }
                mgr.markDirty();

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
                    // Dusk of each night: spawn a wave scaled by days survived
                    BlockPos bp = colony.getBannerPos();
                    if (bp != null) {
                        ServerWorld world = server.getWorld(World.OVERWORLD);
                        if (world != null) {
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
