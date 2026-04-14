package cz.wux.colonycraft.entity.goal;

import cz.wux.colonycraft.entity.ColonistEntity;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * Makes colonists find a nearby bed at night and sleep in it.
 * They lie down (set sleeping pose) and get up at dawn.
 */
public class ColonistSleepGoal extends Goal {

    private final ColonistEntity colonist;
    private BlockPos bedPos;
    private boolean sleeping = false;

    public ColonistSleepGoal(ColonistEntity colonist) {
        this.colonist = colonist;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
    }

    @Override
    public boolean canStart() {
        if (!colonist.isNight()) return false;
        if (colonist.getHomePos() == null) return false;
        bedPos = findNearbyBed();
        return bedPos != null;
    }

    @Override
    public void start() {
        sleeping = false;
        if (bedPos != null) {
            colonist.getNavigation().startMovingTo(
                    bedPos.getX() + 0.5, bedPos.getY(), bedPos.getZ() + 0.5, 1.0);
        }
    }

    @Override
    public void tick() {
        if (bedPos == null) return;

        World world = colonist.getEntityWorld();
        BlockState state = world.getBlockState(bedPos);

        // Bed might have been broken
        if (!(state.getBlock() instanceof BedBlock)) {
            bedPos = findNearbyBed();
            if (bedPos == null) return;
            colonist.getNavigation().startMovingTo(
                    bedPos.getX() + 0.5, bedPos.getY(), bedPos.getZ() + 0.5, 1.0);
            return;
        }

        double distSq = colonist.squaredDistanceTo(
                bedPos.getX() + 0.5, bedPos.getY() + 0.5, bedPos.getZ() + 0.5);

        if (distSq <= 4.0 && !sleeping) {
            // Arrived at bed — lie down
            sleeping = true;
            colonist.getNavigation().stop();
            // Mark bed as occupied
            world.setBlockState(bedPos, state.with(BedBlock.OCCUPIED, true));
            // Position colonist on the bed surface
            colonist.refreshPositionAndAngles(
                    bedPos.getX() + 0.5, bedPos.getY() + 0.5625, bedPos.getZ() + 0.5,
                    colonist.getYaw(), 0);
            colonist.setPose(EntityPose.SLEEPING);
            colonist.setCurrentStatus("\u263E Sleeping");
        }

        if (sleeping) {
            // Stay still
            colonist.getNavigation().stop();
            colonist.setVelocity(0, colonist.getVelocity().y, 0);
        }
    }

    @Override
    public boolean shouldContinue() {
        // Continue sleeping until dawn
        return colonist.isNight() && bedPos != null;
    }

    @Override
    public void stop() {
        if (sleeping) {
            colonist.setPose(EntityPose.STANDING);
            // Unmark bed as occupied
            if (bedPos != null) {
                World world = colonist.getEntityWorld();
                BlockState state = world.getBlockState(bedPos);
                if (state.getBlock() instanceof BedBlock) {
                    world.setBlockState(bedPos, state.with(BedBlock.OCCUPIED, false));
                }
            }
            sleeping = false;
            colonist.setCurrentStatus("\u25CB Idle");
        }
        bedPos = null;
    }

    private BlockPos findNearbyBed() {
        BlockPos home = colonist.getHomePos();
        if (home == null) return null;
        World world = colonist.getEntityWorld();

        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos bp : BlockPos.iterate(home.add(-24, -4, -24), home.add(24, 4, 24))) {
            BlockState state = world.getBlockState(bp);
            if (state.getBlock() instanceof BedBlock
                    && state.get(BedBlock.PART) == BedPart.HEAD
                    && !state.get(BedBlock.OCCUPIED)) {
                double dist = colonist.squaredDistanceTo(bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = bp.toImmutable();
                }
            }
        }
        return best;
    }
}
