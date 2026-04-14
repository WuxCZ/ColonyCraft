package cz.wux.colonycraft.entity.goal;

import cz.wux.colonycraft.blockentity.JobBlockEntity;
import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.entity.ColonistEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * Make a FORESTER colonist plant oak saplings on nearby grass/dirt
 * so woodcutters always have trees to fell.
 * If a sapling spot is found and seeds are available in stockpile, plant one.
 */
public class PlantSaplingsGoal extends Goal {

    private static final int SEARCH_RADIUS = 24;
    private static final int WORK_TICKS    = 25;
    private static final int COOLDOWN      = 60;

    private final ColonistEntity colonist;
    private BlockPos targetPos;
    private int workTick;

    public PlantSaplingsGoal(ColonistEntity colonist) {
        this.colonist = colonist;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return colonist.getColonistJob() == ColonistJob.FORESTER
                && !colonist.isWorkCoolingDown()
                && !colonist.isHungry()
                && !colonist.isNight()
                && colonist.getStockpile().map(s -> s.hasItem(Items.OAK_SAPLING, 1)).orElse(false);
    }

    @Override
    public void start() {
        targetPos = findPlantingSpot();
        workTick  = 0;
        if (targetPos != null) moveTo(targetPos);
    }

    @Override
    public void tick() {
        if (targetPos == null) {
            targetPos = findPlantingSpot();
            if (targetPos != null) { moveTo(targetPos); workTick = 0; }
            return;
        }

        double distSq = colonist.squaredDistanceTo(
                targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);

        if (distSq > 6.25) {
            if (!colonist.getNavigation().isFollowingPath()) moveTo(targetPos);
            return;
        }

        workTick++;
        if (workTick % 4 == 0) {
            colonist.swingHand(Hand.MAIN_HAND);
            colonist.getLookControl().lookAt(
                    targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
        }

        if (workTick >= WORK_TICKS) {
            World world    = colonist.getEntityWorld();
            BlockState gnd = world.getBlockState(targetPos.down());
            BlockState air = world.getBlockState(targetPos);
            boolean hasSapling = colonist.getStockpile()
                    .map(s -> s.hasItem(Items.OAK_SAPLING, 1)).orElse(false);
            if (isGround(gnd) && air.isAir() && hasSapling) {
                colonist.getStockpile().ifPresent(s -> s.withdrawItem(Items.OAK_SAPLING, 1));
                world.setBlockState(targetPos, Blocks.OAK_SAPLING.getDefaultState());
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

    private BlockPos findPlantingSpot() {
        BlockPos[] bounds = getSearchBounds();
        if (bounds == null) return null;

        World world = colonist.getEntityWorld();
        for (BlockPos p : BlockPos.iterate(bounds[0], bounds[1])) {
            if (!world.getBlockState(p).isAir()) continue;
            if (!isGround(world.getBlockState(p.down()))) continue;
            if (world.getLightLevel(p) < 8 && !world.isSkyVisible(p)) continue;
            return p.toImmutable();
        }
        return null;
    }

    /** Returns [min, max] — CS-style area if defined, else default radius. */
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

    private boolean isGround(BlockState s) {
        return s.isOf(Blocks.GRASS_BLOCK) || s.isOf(Blocks.DIRT) || s.isOf(Blocks.COARSE_DIRT);
    }

    private void moveTo(BlockPos pos) {
        colonist.getNavigation().startMovingTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.9);
    }

    /** Called by woodcutter deposit so forester stock of saplings can grow. */
    public static void depositSaplings(ColonistEntity colonist, int count) {
        colonist.getStockpile().ifPresent(s -> s.insertItem(new ItemStack(Items.OAK_SAPLING, count)));
    }
}
