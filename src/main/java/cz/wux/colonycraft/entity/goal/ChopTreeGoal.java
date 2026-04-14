package cz.wux.colonycraft.entity.goal;

import cz.wux.colonycraft.blockentity.JobBlockEntity;
import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.entity.ColonistEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * Makes a WOODCUTTER colonist physically walk to the nearest log block,
 * chop it down (break it + whole trunk), and deposit the logs into the stockpile.
 * Mirrors Colony Survival's woodcutter behaviour.
 */
public class ChopTreeGoal extends Goal {

    private static final int SEARCH_RADIUS = 28;
    private static final int CHOP_TICKS    = 35; // ticks to chop one log (~1.75 s)
    private static final int COOLDOWN      = 40;

    private final ColonistEntity colonist;
    private BlockPos targetLog;
    private int chopTick;

    public ChopTreeGoal(ColonistEntity colonist) {
        this.colonist = colonist;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return colonist.getColonistJob() == ColonistJob.WOODCUTTER
                && !colonist.isWorkCoolingDown()
                && !colonist.isHungry()
                && colonist.getStockpile().isPresent();
    }

    @Override
    public void start() {
        targetLog  = findNearestLog();
        chopTick   = 0;
        if (targetLog != null) {
            moveToLog(targetLog);
        }
    }

    @Override
    public void tick() {
        if (targetLog == null) {
            targetLog = findNearestLog();
            if (targetLog != null) moveToLog(targetLog);
            return;
        }

        World world = colonist.getEntityWorld();
        // Re-validate: log may have been broken by another colonist
        if (!world.getBlockState(targetLog).isIn(BlockTags.LOGS)) {
            targetLog = findNearestLog();
            chopTick  = 0;
            return;
        }

        double distSq = colonist.squaredDistanceTo(
                targetLog.getX() + 0.5, targetLog.getY() + 0.5, targetLog.getZ() + 0.5);

        if (distSq > 9.0) {
            // Still moving — re-path if stuck
            if (!colonist.getNavigation().isFollowingPath()) {
                moveToLog(targetLog);
            }
            return;
        }

        // Close enough — perform chopping
        chopTick++;
        // Swing animation every 5 ticks
        if (chopTick % 5 == 0) {
            colonist.swingHand(Hand.MAIN_HAND);
            colonist.getLookControl().lookAt(
                    targetLog.getX() + 0.5, targetLog.getY() + 0.5, targetLog.getZ() + 0.5);
        }

        if (chopTick >= CHOP_TICKS) {
            chopLog(world);
            chopTick = 0;
            targetLog = null;
            colonist.startWorkCooldown(COOLDOWN);
        }
    }

    /** Break the target log (and any logs directly above it = whole trunk) then deposit. */
    private void chopLog(World world) {
        if (targetLog == null) return;

        BlockPos cursor = targetLog;
        int maxHeight = 16;
        int logsChopped = 0;

        while (maxHeight-- > 0 && world.getBlockState(cursor).isIn(BlockTags.LOGS)) {
            BlockState state = world.getBlockState(cursor);
            net.minecraft.item.Item logItem = state.getBlock().asItem();
            // Break without dropping (we manage drops ourselves)
            world.breakBlock(cursor, false, colonist);
            // Insert one log per broken block
            colonist.getStockpile().ifPresent(s -> s.insertItem(new ItemStack(logItem, 1)));
            logsChopped++;
            cursor = cursor.up();
        }

        // Also look for logs left/right that are part of a wider canopy trunk
        // (simplified: just chop straight up which handles standard tree trunks)
    }

    private BlockPos findNearestLog() {
        BlockPos[] bounds = getSearchBounds();
        if (bounds == null) return null;

        World world   = colonist.getEntityWorld();
        BlockPos best = null;
        double bestD  = Double.MAX_VALUE;

        for (BlockPos p : BlockPos.iterate(bounds[0], bounds[1])) {
            if (!world.getBlockState(p).isIn(BlockTags.LOGS)) continue;
            double d = colonist.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
            if (d < bestD) {
                bestD = d;
                best  = p.toImmutable();
            }
        }
        return best;
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
        return new BlockPos[]{ center.add(-r, -4, -r), center.add(r, 20, r) };
    }

    private void moveToLog(BlockPos log) {
        // Navigate to a position adjacent (same Y or one below) to avoid jumping
        colonist.getNavigation().startMovingTo(
                log.getX() + 0.5, log.getY(), log.getZ() + 0.5, 0.9);
    }

    @Override
    public boolean shouldContinue() {
        return !colonist.isWorkCoolingDown() && !colonist.isHungry();
    }

    @Override
    public void stop() {
        targetLog = null;
        chopTick  = 0;
        colonist.getNavigation().stop();
    }
}
