package cz.wux.colonycraft.item;

import cz.wux.colonycraft.blockentity.JobBlockEntity;
import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.entity.GuardEntity;
import cz.wux.colonycraft.registry.ModEntities;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Colony Survey Wand — used to mark a rectangular work area for job blocks.
 *
 * Flow (Colony Survival style):
 *   1. Right-click any block → opens job type selection screen
 *   2. Select a job type → wand enters area-selection mode for that job
 *   3. Right-click block → set Corner 1
 *   4. Right-click another block → set Corner 2, validates against job limits, auto-assigns
 */
public class AreaWandItem extends Item {

    /** Per-player selection state: [0] = corner1, [1] = corner2 */
    private static final Map<UUID, BlockPos[]> SELECTIONS = new HashMap<>();

    /** Per-player selected job type for area assignment. */
    private static final Map<UUID, ColonistJob> SELECTED_JOB = new HashMap<>();

    /** Public accessor for the renderer to read current selection. */
    public static BlockPos[] getSelection(UUID playerId) {
        return SELECTIONS.get(playerId);
    }

    /** Get the currently selected job for a player. */
    public static ColonistJob getSelectedJob(UUID playerId) {
        return SELECTED_JOB.get(playerId);
    }

    /** Set the selected job type (called from JobSelectionScreen). */
    public static void setSelectedJob(UUID playerId, ColonistJob job) {
        SELECTED_JOB.put(playerId, job);
        // Clear any old selection
        SELECTIONS.remove(playerId);
    }

    /** Clear selection after area assignment. */
    public static void clearSelection(UUID playerId) {
        SELECTIONS.remove(playerId);
        SELECTED_JOB.remove(playerId);
    }

    /** Callback set by client to open job selection screen. */
    public static Runnable clientAreaCompleteHandler = null;

    /** Job block data collected on server thread for the screen to use. */
    public static final List<JobBlockData> lastFoundJobs = new CopyOnWriteArrayList<>();

    /** Immutable snapshot of a job block for cross-thread transfer. */
    public record JobBlockData(String jobName, BlockPos pos, boolean hasColonist, boolean hasArea, int distance) {}

    public AreaWandItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld().isClient()) return ActionResult.SUCCESS;

        PlayerEntity player = context.getPlayer();
        if (player == null) return ActionResult.PASS;

        UUID playerId = player.getUuid();
        BlockPos clicked = context.getBlockPos();
        ColonistJob selectedJob = SELECTED_JOB.get(playerId);

        // ── No job selected yet → open job selection screen ──────────────
        if (selectedJob == null) {
            // Collect nearby job blocks for context
            collectNearbyJobs(context.getWorld(), player.getBlockPos());
            if (clientAreaCompleteHandler != null) {
                clientAreaCompleteHandler.run();
            }
            return ActionResult.SUCCESS;
        }

        // ── Job selected → selecting corners ─────────────────────────────
        BlockPos[] sel = SELECTIONS.computeIfAbsent(playerId, k -> new BlockPos[2]);

        if (sel[0] == null) {
            // Set corner 1
            sel[0] = clicked.toImmutable();
            sel[1] = null;
            player.sendMessage(Text.literal(
                "\u00a7a[" + selectedJob.displayName + "] \u00a7fCorner 1: \u00a7e" +
                clicked.getX() + " " + clicked.getY() + " " + clicked.getZ() +
                " \u00a77\u2014 now select Corner 2"), false);
        } else {
            // Set corner 2 and validate
            sel[1] = clicked.toImmutable();
            int w = Math.abs(sel[1].getX() - sel[0].getX()) + 1;
            int d = Math.abs(sel[1].getZ() - sel[0].getZ()) + 1;

            // Validate area size
            if (selectedJob.maxAreaSize > 0 && (w > selectedJob.maxAreaSize || d > selectedJob.maxAreaSize)) {
                player.sendMessage(Text.literal(
                    "\u00a7c[" + selectedJob.displayName + "] Area too large! Max: \u00a7e" +
                    selectedJob.maxAreaSize + "\u00d7" + selectedJob.maxAreaSize +
                    "\u00a7c, selected: \u00a7e" + w + "\u00d7" + d), false);
                sel[1] = null;
                return ActionResult.SUCCESS;
            }

            BlockPos min = new BlockPos(
                Math.min(sel[0].getX(), sel[1].getX()),
                Math.min(sel[0].getY(), sel[1].getY()),
                Math.min(sel[0].getZ(), sel[1].getZ()));
            BlockPos max = new BlockPos(
                Math.max(sel[0].getX(), sel[1].getX()),
                Math.max(sel[0].getY(), sel[1].getY()),
                Math.max(sel[0].getZ(), sel[1].getZ()));

            // Find nearest matching job block to assign the area
            JobBlockEntity target = findMatchingJobBlock(context.getWorld(), player.getBlockPos(), selectedJob);
            if (target != null) {
                target.setArea(min, max);
                player.sendMessage(Text.literal(
                    "\u00a7a[" + selectedJob.displayName + "] \u00a7fWork area set: \u00a7e" + w + "\u00d7" + d +
                    " \u00a7fblocks \u00a77(assigned to job block at " +
                    target.getPos().getX() + " " + target.getPos().getY() + " " + target.getPos().getZ() + ")"), false);
            } else if (selectedJob.isGuard() && context.getWorld() instanceof ServerWorld sw) {
                // Guard without a guard tower — spawn guard directly
                BlockPos center = new BlockPos(
                    (min.getX() + max.getX()) / 2, min.getY(),
                    (min.getZ() + max.getZ()) / 2);
                ColonyManager mgr = ColonyManager.get(sw);
                mgr.getNearestColony(center, 64 * 64).ifPresentOrElse(colony -> {
                    if (!colony.canSpawnMoreColonists()) {
                        player.sendMessage(Text.literal(
                            "\u00a7c[Guard] Colony at max population! Place more beds."), false);
                        return;
                    }
                    GuardEntity guard = ModEntities.GUARD.create(sw, SpawnReason.MOB_SUMMONED);
                    if (guard != null) {
                        guard.refreshPositionAndAngles(
                            center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5, 0, 0);
                        guard.setColonyId(colony.getColonyId());
                        guard.setHomePos(center);
                        guard.setGuardJob(selectedJob);
                        guard.setCustomName(net.minecraft.text.Text.literal(selectedJob.displayName));
                        guard.setCustomNameVisible(true);
                        sw.spawnEntity(guard);
                        colony.addColonist(guard.getUuid());
                        mgr.markDirty();
                        player.sendMessage(Text.literal(
                            "\u00a7a[Guard] \u00a7fGuard deployed! Patrol area: \u00a7e" +
                            w + "\u00d7" + d + " \u00a7fblocks"), false);
                    }
                }, () -> player.sendMessage(Text.literal(
                    "\u00a7c[Guard] No colony found nearby!"), false));
            } else if (!selectedJob.requiresBlock && context.getWorld() instanceof ServerWorld sw) {
                // Assign an unemployed colonist directly — no block needed
                BlockPos center = new BlockPos(
                    (min.getX() + max.getX()) / 2, min.getY(),
                    (min.getZ() + max.getZ()) / 2);
                ColonyManager mgr = ColonyManager.get(sw);
                var colonyOpt = mgr.getNearestColony(center, 64 * 64);
                if (colonyOpt.isPresent()) {
                    var colony = colonyOpt.get();
                    var searchBox = new net.minecraft.util.math.Box(
                        center.getX() - 64, center.getY() - 16, center.getZ() - 64,
                        center.getX() + 64, center.getY() + 16, center.getZ() + 64);
                    var colonists = sw.getEntitiesByClass(
                        cz.wux.colonycraft.entity.ColonistEntity.class, searchBox,
                        e -> e.getColonyId() != null && e.getColonyId().equals(colony.getColonyId())
                             && e.getColonistJob() == ColonistJob.UNEMPLOYED);
                    if (!colonists.isEmpty()) {
                        var colonist = colonists.get(0);
                        colonist.setColonistJob(selectedJob);
                        colonist.setJobBlockPos(center);
                        if (colony.getStockpilePos() != null) {
                            colonist.setStockpilePos(colony.getStockpilePos());
                        }
                        player.sendMessage(Text.literal(
                            "\u00a7a[" + selectedJob.displayName + "] \u00a7fWork area: \u00a7e" +
                            w + "\u00d7" + d + " \u00a7fblocks. Colonist assigned!"), false);
                    } else {
                        player.sendMessage(Text.literal(
                            "\u00a7c[" + selectedJob.displayName + "] No unemployed colonist! Recruit more."), false);
                    }
                } else {
                    player.sendMessage(Text.literal(
                        "\u00a7c[" + selectedJob.displayName + "] No colony found nearby!"), false);
                }
            } else {
                player.sendMessage(Text.literal(
                    "\u00a7c[" + selectedJob.displayName + "] No matching job block found nearby! " +
                    "Place a \u00a7e" + selectedJob.jobBlockKey.replace('_', ' ') + "\u00a7c first."), false);
                sel[1] = null;
                return ActionResult.SUCCESS;
            }

            clearSelection(playerId);
        }
        return ActionResult.SUCCESS;
    }

    /** Find nearest unassigned job block matching the selected job type. */
    private static JobBlockEntity findMatchingJobBlock(net.minecraft.world.World world, BlockPos playerPos, ColonistJob job) {
        JobBlockEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos bp : BlockPos.iterate(
                playerPos.add(-48, -8, -48),
                playerPos.add(48, 8, 48))) {
            var be = world.getBlockEntity(bp);
            if (be instanceof JobBlockEntity jb && jb.getJob() == job) {
                double dist = playerPos.getSquaredDistance(bp);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = jb;
                }
            }
        }
        return best;
    }

    /** Collects job block data on the server thread so the client screen can use it safely. */
    private static void collectNearbyJobs(net.minecraft.world.World world, BlockPos playerPos) {
        lastFoundJobs.clear();
        for (BlockPos bp : BlockPos.iterate(
                playerPos.add(-48, -8, -48),
                playerPos.add(48, 8, 48))) {
            var be = world.getBlockEntity(bp);
            if (be instanceof JobBlockEntity jb) {
                lastFoundJobs.add(new JobBlockData(
                    jb.getJob().displayName,
                    bp.toImmutable(),
                    jb.hasAssignedColonist(),
                    jb.hasArea(),
                    (int) Math.sqrt(playerPos.getSquaredDistance(bp))
                ));
            }
        }
    }
}