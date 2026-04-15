package cz.wux.colonycraft;

import cz.wux.colonycraft.blockentity.JobBlockEntity;
import cz.wux.colonycraft.blockentity.StockpileBlockEntity;
import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.data.Quest;
import cz.wux.colonycraft.entity.ColonistEntity;
import cz.wux.colonycraft.entity.ColonyMonsterEntity;
import cz.wux.colonycraft.entity.GuardEntity;
import cz.wux.colonycraft.registry.*;
import net.minecraft.sound.SoundCategory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.BedBlock;
import net.minecraft.block.enums.BedPart;
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

                BlockPos bp = colony.getBannerPos();

                // -- Auto-discover/verify stockpile near banner --
                {
                    BlockPos existingSp = colony.getStockpilePos();
                    if (existingSp != null && !(overworld.getBlockEntity(existingSp) instanceof StockpileBlockEntity)) {
                        colony.setStockpilePos(null);
                        mgr.markDirty();
                    }
                    if (colony.getStockpilePos() == null && bp != null) {
                        for (BlockPos scan : BlockPos.iterate(bp.add(-32, -4, -32), bp.add(32, 4, 32))) {
                            if (overworld.getBlockEntity(scan) instanceof StockpileBlockEntity sbe) {
                                colony.setStockpilePos(scan.toImmutable());
                                sbe.setColonyId(colony.getColonyId());
                                mgr.markDirty();
                                break;
                            }
                        }
                    }
                }

                // -- Sync food from stockpile every colony tick --
                BlockPos sp = colony.getStockpilePos();
                if (sp != null && overworld.getBlockEntity(sp) instanceof StockpileBlockEntity stockpile) {
                    stockpile.setColonyId(colony.getColonyId());
                    stockpile.syncFoodToColony();
                }

                // -- Count beds near banner for population cap --
                if (bp != null) {
                    int bedCount = 0;
                    for (BlockPos scan : BlockPos.iterate(bp.add(-32, -4, -32), bp.add(32, 4, 32))) {
                        var blockState = overworld.getBlockState(scan);
                        if (blockState.getBlock() instanceof BedBlock
                                && blockState.get(BedBlock.PART) == BedPart.HEAD) {
                            bedCount++;
                        }
                    }
                    colony.setPopulationCap(bedCount);
                }

                // -- Cleanup stale colonists (dead/despawned) --
                colony.getColonistUuids().removeIf(uuid -> {
                    var entity = overworld.getEntity(uuid);
                    return entity != null && !entity.isAlive();
                });

                // -- Injured colonist warnings --
                {
                    for (java.util.UUID uuid : colony.getColonistUuids()) {
                        var entity = overworld.getEntity(uuid);
                        if (entity instanceof ColonistEntity ce && ce.getHealth() < ce.getMaxHealth() * 0.3f) {
                            String name = ce.getCustomName() != null ? ce.getCustomName().getString() : "Colonist";
                            for (ServerPlayerEntity player : overworld.getPlayers()) {
                                if (colony.isMember(player.getUuid())
                                        && player.squaredDistanceTo(ce.getX(), ce.getY(), ce.getZ()) < 6400) {
                                    player.sendMessage(Text.literal("\u00a7c\u2620 " + name + " is critically injured!"), true);
                                }
                            }
                        }
                    }
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

                // -- Population growth (gradual, 1 per ~5 minutes) --
                if (colony.canSpawnMoreColonists() && colony.getFoodUnits() > 20
                        && overworld.random.nextInt(100) == 0) {
                    BlockPos bannerPos = colony.getBannerPos();
                    if (bannerPos != null) {
                        ServerWorld world = server.getWorld(World.OVERWORLD);
                        if (world != null) {
                            ColonistEntity.spawnForColony(world, colony, bannerPos, mgr);
                        }
                    }
                }

                // -- Auto-spawn guards for unclaimed guard towers --
                if (colony.canSpawnMoreColonists() && bp != null) {
                    ServerWorld world = server.getWorld(World.OVERWORLD);
                    if (world != null) {
                        for (BlockPos scan : BlockPos.iterate(bp.add(-32, -4, -32), bp.add(32, 4, 32))) {
                            if (!colony.canSpawnMoreColonists()) break;
                            var be = world.getBlockEntity(scan);
                            if (be instanceof JobBlockEntity jb && jb.getJob().isGuard()
                                    && !jb.hasAssignedColonist()) {
                                GuardEntity guard = ModEntities.GUARD.create(world,
                                        net.minecraft.entity.SpawnReason.MOB_SUMMONED);
                                if (guard != null) {
                                    BlockPos spawnPos = scan.add(0, 1, 0);
                                    guard.refreshPositionAndAngles(
                                            spawnPos.getX() + 0.5, spawnPos.getY(),
                                            spawnPos.getZ() + 0.5, 0, 0);
                                    guard.setColonyId(colony.getColonyId());
                                    guard.setHomePos(scan);
                                    guard.setGuardJob(jb.getJob());
                                    jb.assignColonist(guard.getUuid());
                                    guard.assignRandomName();
                                    guard.updateDisplayName();
                                    world.spawnEntity(guard);
                                    world.playSound(null, scan, ModSounds.COLONIST_SPAWN,
                                            SoundCategory.NEUTRAL, 1.0f, 0.9f);
                                    colony.addColonist(guard.getUuid());
                                    mgr.markDirty();
                                }
                            }
                        }
                    }
                }

                // -- Quest progress checking --
                {
                    boolean questCompleted = false;
                    for (Quest quest : Quest.values()) {
                        if (colony.isQuestCompleted(quest.name())) continue;
                        if (!quest.isUnlocked(colony)) continue;
                        if (quest.isComplete(colony, overworld)) {
                            colony.completeQuest(quest.name());
                            if (quest.scienceReward > 0) colony.addScience(quest.scienceReward);
                            questCompleted = true;
                            for (ServerPlayerEntity player : overworld.getPlayers()) {
                                if (colony.isMember(player.getUuid())) {
                                    player.sendMessage(Text.literal(
                                            "\u00a7a\u00a7l\u2714 Quest Complete: \u00a7r\u00a7e" + quest.displayName
                                            + (quest.scienceReward > 0 ? " \u00a77(+" + quest.scienceReward + " science)" : "")), false);
                                }
                            }
                        }
                    }
                    if (questCompleted) mgr.markDirty();
                }

                // -- Wave early warning (dusk) --
                if (dayTime >= 11800 && dayTime <= 11900) {
                    BlockPos wavePos = colony.getBannerPos();
                    if (wavePos != null) {
                        ServerWorld world = server.getWorld(World.OVERWORLD);
                        if (world != null) {
                            for (ServerPlayerEntity player : world.getPlayers()) {
                                if (player.squaredDistanceTo(wavePos.getX(), wavePos.getY(), wavePos.getZ()) < 10000) {
                                    player.sendMessage(Text.literal("\u00a76\u00a7l\u2694 Dusk approaches... Prepare your defenses!"), true);
                                }
                            }
                        }
                    }
                }

                // -- Wave imminent warning --
                if (dayTime >= 12700 && dayTime <= 12800) {
                    BlockPos wavePos = colony.getBannerPos();
                    if (wavePos != null) {
                        ServerWorld world = server.getWorld(World.OVERWORLD);
                        if (world != null) {
                            world.playSound(null, wavePos, ModSounds.WAVE_HORN,
                                    SoundCategory.HOSTILE, 1.5f, 1.2f);
                            for (ServerPlayerEntity player : world.getPlayers()) {
                                if (player.squaredDistanceTo(wavePos.getX(), wavePos.getY(), wavePos.getZ()) < 10000) {
                                    player.sendMessage(Text.literal("\u00a74\u00a7l\u26A0 WAVE INCOMING! \u00a7cEnemies are gathering!"), true);
                                }
                            }
                        }
                    }
                }

                // -- Night wave: spawn zombies and skeletons --
                if (dayTime >= 13000 && dayTime <= 13100) {
                    BlockPos wavePos = colony.getBannerPos();
                    if (wavePos != null) {
                        ServerWorld world = server.getWorld(World.OVERWORLD);
                        if (world != null) {
                            world.playSound(null, wavePos, ModSounds.WAVE_HORN,
                                    SoundCategory.HOSTILE, 2.0f, 0.8f);
                            ColonyMonsterEntity.spawnWave(world, colony, wavePos);
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