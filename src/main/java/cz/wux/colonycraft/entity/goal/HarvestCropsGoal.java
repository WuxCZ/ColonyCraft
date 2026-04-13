package cz.wux.colonycraft.entity.goal;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.entity.ColonistEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * Makes a FARMER colonist:
 *  1. Find mature wheat (age == max), harvest it and deposit in stockpile.
 *  2. Find empty farmland and plant wheat seeds from stockpile.
 *
 * Mirrors Colony Survival's farmer behaviour.
 */
public class HarvestCropsGoal extends Goal {

    private static final int SEARCH_RADIUS = 20;
    private static final int WORK_TICKS    = 30; // time per harvest/plant action
    private static final int COOLDOWN      = 40;

    private enum Phase { SEEK_HARVEST, HARVESTING, SEEK_PLANT, PLANTING }

    private final ColonistEntity colonist;
    private BlockPos targetPos;
    private Phase phase = Phase.SEEK_HARVEST;
    private int workTick;

    public HarvestCropsGoal(ColonistEntity colonist) {
        this.colonist = colonist;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return colonist.getColonistJob() == ColonistJob.FARMER
                && !colonist.isWorkCoolingDown()
                && !colonist.isHungry()
                && colonist.getStockpile().isPresent();
    }

    @Override
    public void start() {
        workTick  = 0;
        targetPos = findMatureWheat();
        if (targetPos != null) {
            phase = Phase.SEEK_HARVEST;
            moveTo(targetPos);
        } else {
            targetPos = findEmptyFarmland();
            if (targetPos != null) {
                phase = Phase.SEEK_PLANT;
                moveTo(targetPos);
            }
        }
    }

    @Override
    public void tick() {
        if (targetPos == null) {
            // Try to find new work
            targetPos = findMatureWheat();
            if (targetPos != null) {
                phase    = Phase.SEEK_HARVEST;
                workTick = 0;
                moveTo(targetPos);
            } else {
                targetPos = findEmptyFarmland();
                if (targetPos != null) {
                    phase    = Phase.SEEK_PLANT;
                    workTick = 0;
                    moveTo(targetPos);
                }
            }
            return;
        }

        double distSq = colonist.squaredDistanceTo(
                targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);

        if (distSq > 6.25) { // 2.5 blocks
            if (!colonist.getNavigation().isFollowingPath()) moveTo(targetPos);
            return;
        }

        // Close enough — perform action
        workTick++;
        if (workTick % 4 == 0) {
            colonist.swingHand(Hand.MAIN_HAND);
            colonist.getLookControl().lookAt(
                    targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
        }

        if (workTick >= WORK_TICKS) {
            World world = colonist.getEntityWorld();
            switch (phase) {
                case SEEK_HARVEST, HARVESTING -> {
                    BlockState state = world.getBlockState(targetPos);
                    if (state.getBlock() instanceof CropBlock crop && crop.isMature(state)) {
                        // Break the crop
                        world.breakBlock(targetPos, false, colonist);
                        // Deposit wheat and keep seeds for replanting
                        colonist.getStockpile().ifPresent(s -> {
                            s.insertItem(new ItemStack(Items.WHEAT, 2));
                            s.insertItem(new ItemStack(Items.WHEAT_SEEDS, 1));
                        });
                        // Replant immediately on the farmland below
                        BlockPos farmland = targetPos.down();
                        BlockState below  = world.getBlockState(farmland);
                        if (below.isOf(Blocks.FARMLAND)) {
                            world.setBlockState(targetPos, Blocks.WHEAT.getDefaultState());
                        }
                    }
                }
                case SEEK_PLANT, PLANTING -> {
                    BlockState farmState = world.getBlockState(targetPos.down());
                    BlockState above     = world.getBlockState(targetPos);
                    boolean hasSeed = colonist.getStockpile().map(s -> s.hasItem(Items.WHEAT_SEEDS, 1)).orElse(false);
                    if (farmState.isOf(Blocks.FARMLAND) && above.isAir() && hasSeed) {
                        colonist.getStockpile().ifPresent(s -> s.withdrawItem(Items.WHEAT_SEEDS, 1));
                        world.setBlockState(targetPos, Blocks.WHEAT.getDefaultState());
                    }
                }
            }
            workTick  = 0;
            targetPos = null;
            colonist.startWorkCooldown(COOLDOWN);
        }
    }

    @Override
    public boolean shouldContinue() {
        return !colonist.isWorkCoolingDown() && !colonist.isHungry();
    }

    @Override
    public void stop() {
        targetPos = null;
        workTick  = 0;
        colonist.getNavigation().stop();
    }

    private BlockPos findMatureWheat() {
        BlockPos center = getCenter();
        if (center == null) return null;
        World world = colonist.getEntityWorld();
        BlockPos best = null;
        double bestD  = Double.MAX_VALUE;
        int r = SEARCH_RADIUS;
        for (BlockPos p : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 4, r))) {
            BlockState s = world.getBlockState(p);
            if (s.getBlock() instanceof CropBlock crop && crop.isMature(s)) {
                double d = colonist.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                if (d < bestD) { bestD = d; best = p.toImmutable(); }
            }
        }
        return best;
    }

    private BlockPos findEmptyFarmland() {
        BlockPos center = getCenter();
        if (center == null) return null;
        // Only plant if we have seeds
        boolean hasSeed = colonist.getStockpile().map(s -> s.hasItem(Items.WHEAT_SEEDS, 1)).orElse(false);
        if (!hasSeed) return null;
        World world = colonist.getEntityWorld();
        BlockPos best = null;
        double bestD  = Double.MAX_VALUE;
        int r = SEARCH_RADIUS;
        for (BlockPos p : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 4, r))) {
            if (!world.getBlockState(p.down()).isOf(Blocks.FARMLAND)) continue;
            if (!world.getBlockState(p).isAir()) continue;
            double d = colonist.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
            if (d < bestD) { bestD = d; best = p.toImmutable(); }
        }
        return best;
    }

    private BlockPos getCenter() {
        BlockPos c = colonist.getJobBlockPos();
        return c != null ? c : colonist.getHomePos();
    }

    private void moveTo(BlockPos pos) {
        colonist.getNavigation().startMovingTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.9);
    }
}
