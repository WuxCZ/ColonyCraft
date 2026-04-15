package cz.wux.colonycraft.entity.goal;

import cz.wux.colonycraft.blockentity.JobBlockEntity;
import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.entity.ColonistEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * Makes a BERRY_FARMER colonist:
 *  1. First, plant sweet berry bushes on empty blocks in the work area.
 *  2. Then harvest mature sweet berry bushes (age >= 2).
 *
 * Planting is prioritized over harvesting — the farmer fills the area with
 * bushes first, then enters a harvest cycle.
 */
public class HarvestBerriesGoal extends Goal {

    private static final int SEARCH_RADIUS = 16;
    private static final int WORK_TICKS    = 25;
    private static final int COOLDOWN      = 35;

    private enum Phase { SEEK_PLANT, PLANTING, SEEK_HARVEST, HARVESTING }

    private final ColonistEntity colonist;
    private BlockPos targetPos;
    private Phase phase = Phase.SEEK_PLANT;
    private int workTick;

    public HarvestBerriesGoal(ColonistEntity colonist) {
        this.colonist = colonist;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return colonist.getColonistJob() == ColonistJob.BERRY_FARMER
                && !colonist.isWorkCoolingDown()
                && !colonist.isHungry()
                && !colonist.isNight();
    }

    @Override
    public void start() {
        workTick = 0;
        // Prioritize planting over harvesting
        targetPos = findPlantableSpot();
        if (targetPos != null) {
            phase = Phase.SEEK_PLANT;
            moveTo(targetPos);
        } else {
            targetPos = findMatureBerry();
            if (targetPos != null) {
                phase = Phase.SEEK_HARVEST;
                moveTo(targetPos);
            }
        }
    }

    @Override
    public void tick() {
        if (targetPos == null) {
            // Prioritize planting
            targetPos = findPlantableSpot();
            if (targetPos != null) {
                phase = Phase.SEEK_PLANT;
                workTick = 0;
                moveTo(targetPos);
            } else {
                targetPos = findMatureBerry();
                if (targetPos != null) {
                    phase = Phase.SEEK_HARVEST;
                    workTick = 0;
                    moveTo(targetPos);
                }
            }
            return;
        }

        double distSq = colonist.squaredDistanceTo(
                targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);

        if (distSq > 6.25) {
            if (!colonist.getNavigation().isFollowingPath()) moveTo(targetPos);
            return;
        }

        // Set descriptive status
        switch (phase) {
            case SEEK_PLANT, PLANTING -> colonist.setCurrentStatus("\uD83C\uDF53 Planting berries");
            case SEEK_HARVEST, HARVESTING -> colonist.setCurrentStatus("\uD83C\uDF53 Harvesting berries");
        }

        workTick++;
        if (workTick % 4 == 0) {
            colonist.swingHand(Hand.MAIN_HAND);
            colonist.getLookControl().lookAt(
                    targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
        }

        if (workTick >= WORK_TICKS) {
            World world = colonist.getEntityWorld();
            switch (phase) {
                case SEEK_PLANT, PLANTING -> {
                    BlockState below = world.getBlockState(targetPos);
                    BlockState at = world.getBlockState(targetPos.up());
                    if ((below.isOf(Blocks.GRASS_BLOCK) || below.isOf(Blocks.DIRT))
                            && at.isAir()) {
                        world.setBlockState(targetPos.up(),
                                Blocks.SWEET_BERRY_BUSH.getDefaultState());
                    }
                }
                case SEEK_HARVEST, HARVESTING -> {
                    BlockState state = world.getBlockState(targetPos);
                    if (state.isOf(Blocks.SWEET_BERRY_BUSH)) {
                        int age = state.get(SweetBerryBushBlock.AGE);
                        if (age >= 2) {
                            int berryCount = (age == 3) ? 3 : 1;
                            colonist.getStockpile().ifPresent(s ->
                                    s.insertItem(new ItemStack(Items.SWEET_BERRIES, berryCount)));
                            // Reset bush to age 1 (grows back)
                            world.setBlockState(targetPos,
                                    state.with(SweetBerryBushBlock.AGE, 1));
                        }
                    }
                }
            }
            workTick = 0;
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
        workTick = 0;
        colonist.getNavigation().stop();
    }

    /** Find an empty spot in the work area where a berry bush can be planted. */
    private BlockPos findPlantableSpot() {
        BlockPos[] bounds = getSearchBounds();
        if (bounds == null) return null;
        World world = colonist.getEntityWorld();
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.iterate(bounds[0], bounds[1])) {
            BlockState below = world.getBlockState(p);
            if (!(below.isOf(Blocks.GRASS_BLOCK) || below.isOf(Blocks.DIRT))) continue;
            if (!world.getBlockState(p.up()).isAir()) continue;
            // Check there's no bush already at this position
            double d = colonist.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
            if (d < bestD) { bestD = d; best = p.toImmutable(); }
        }
        return best;
    }

    /** Find a mature sweet berry bush (age >= 2) in the work area. */
    private BlockPos findMatureBerry() {
        BlockPos[] bounds = getSearchBounds();
        if (bounds == null) return null;
        World world = colonist.getEntityWorld();
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.iterate(bounds[0], bounds[1])) {
            BlockState s = world.getBlockState(p);
            if (s.isOf(Blocks.SWEET_BERRY_BUSH)
                    && s.get(SweetBerryBushBlock.AGE) >= 2) {
                double d = colonist.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                if (d < bestD) { bestD = d; best = p.toImmutable(); }
            }
        }
        return best;
    }

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
}
