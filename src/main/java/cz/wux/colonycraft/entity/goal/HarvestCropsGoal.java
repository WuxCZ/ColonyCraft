package cz.wux.colonycraft.entity.goal;

import cz.wux.colonycraft.blockentity.JobBlockEntity;
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

    private enum Phase { SEEK_HARVEST, HARVESTING, SEEK_PLANT, PLANTING, SEEK_TILL, TILLING }

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
                && !colonist.isNight()
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
            } else {
                targetPos = findTillableBlock();
                if (targetPos != null) {
                    phase = Phase.SEEK_TILL;
                    moveTo(targetPos);
                }
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
                } else {
                    targetPos = findTillableBlock();
                    if (targetPos != null) {
                        phase    = Phase.SEEK_TILL;
                        workTick = 0;
                        moveTo(targetPos);
                    }
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
                case SEEK_TILL, TILLING -> {
                    BlockState tillState = world.getBlockState(targetPos);
                    if (tillState.isOf(Blocks.GRASS_BLOCK) || tillState.isOf(Blocks.DIRT)) {
                        world.setBlockState(targetPos, Blocks.FARMLAND.getDefaultState());
                        world.playSound(null, targetPos,
                                net.minecraft.sound.SoundEvents.ITEM_HOE_TILL,
                                net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);
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
        return !colonist.isWorkCoolingDown() && !colonist.isHungry() && !colonist.isNight();
    }

    @Override
    public void stop() {
        targetPos = null;
        workTick  = 0;
        colonist.getNavigation().stop();
    }

    private BlockPos findMatureWheat() {
        BlockPos[] bounds = getSearchBounds();
        if (bounds == null) return null;
        World world = colonist.getEntityWorld();
        BlockPos best = null;
        double bestD  = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.iterate(bounds[0], bounds[1])) {
            BlockState s = world.getBlockState(p);
            if (s.getBlock() instanceof CropBlock crop && crop.isMature(s)) {
                double d = colonist.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                if (d < bestD) { bestD = d; best = p.toImmutable(); }
            }
        }
        return best;
    }

    private BlockPos findEmptyFarmland() {
        BlockPos[] bounds = getSearchBounds();
        if (bounds == null) return null;
        boolean hasSeed = colonist.getStockpile().map(s -> s.hasItem(Items.WHEAT_SEEDS, 1)).orElse(false);
        if (!hasSeed) return null;
        World world = colonist.getEntityWorld();
        BlockPos best = null;
        double bestD  = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.iterate(bounds[0], bounds[1])) {
            if (!world.getBlockState(p.down()).isOf(Blocks.FARMLAND)) continue;
            if (!world.getBlockState(p).isAir()) continue;
            double d = colonist.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
            if (d < bestD) { bestD = d; best = p.toImmutable(); }
        }
        return best;
    }

    /** Returns [min, max] search bounds — uses CS-style area from JobBlockEntity if defined,
     *  otherwise falls back to a default radius around the job block. */
    private BlockPos[] getSearchBounds() {
        var jobBlock = colonist.getJobBlock();
        if (jobBlock.isPresent() && jobBlock.get().hasArea()) {
            return new BlockPos[]{ jobBlock.get().getAreaMin(), jobBlock.get().getAreaMax() };
        }
        BlockPos center = colonist.getJobBlockPos();
        if (center == null) center = colonist.getHomePos();
        if (center == null) return null;
        int r = SEARCH_RADIUS;
        return new BlockPos[]{ center.add(-r, -2, -r), center.add(r, 4, r) };
    }

    private void moveTo(BlockPos pos) {
        colonist.getNavigation().startMovingTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.9);
    }

    /** Find grass/dirt blocks in the work area that can be tilled into farmland. */
    private BlockPos findTillableBlock() {
        BlockPos[] bounds = getSearchBounds();
        if (bounds == null) return null;
        World world = colonist.getEntityWorld();
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.iterate(bounds[0], bounds[1])) {
            BlockState s = world.getBlockState(p);
            if ((s.isOf(Blocks.GRASS_BLOCK) || s.isOf(Blocks.DIRT))
                    && world.getBlockState(p.up()).isAir()) {
                double d = colonist.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                if (d < bestD) { bestD = d; best = p.toImmutable(); }
            }
        }
        return best;
    }
}
