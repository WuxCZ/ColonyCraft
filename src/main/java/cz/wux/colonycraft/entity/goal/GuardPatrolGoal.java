package cz.wux.colonycraft.entity.goal;

import cz.wux.colonycraft.entity.GuardEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.Random;

/**
 * Guard patrol goal: guard picks a random point within
 * {@link GuardEntity#PATROL_RADIUS} of the colony banner and walks to it.
 * When it arrives it waits briefly then picks a new point.
 */
public class GuardPatrolGoal extends Goal {

    private final GuardEntity guard;
    private BlockPos patrolTarget;
    private int waitTicks = 0;

    public GuardPatrolGoal(GuardEntity guard) {
        this.guard = guard;
        setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return guard.getHomePos() != null && waitTicks <= 0 && guard.getTarget() == null;
    }

    @Override
    public void start() {
        if (guard.isNight()) {
            // Return home at night
            BlockPos home = guard.getHomePos();
            if (home != null) {
                patrolTarget = home;
                guard.getNavigation().startMovingTo(
                        home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 1.0);
            }
        } else {
            pickNewTarget();
        }
    }

    @Override
    public void tick() {
        if (waitTicks > 0) { waitTicks--; return; }
        if (patrolTarget == null) {
            if (guard.isNight()) {
                // Stay near home at night
                BlockPos home = guard.getHomePos();
                if (home != null) {
                    double distHome = guard.squaredDistanceTo(home.getX() + 0.5, home.getY() + 0.5, home.getZ() + 0.5);
                    if (distHome > 25.0) {
                        patrolTarget = home;
                        guard.getNavigation().startMovingTo(
                                home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 1.0);
                    } else {
                        waitTicks = 100; // Stay still near home
                    }
                }
            } else {
                pickNewTarget();
            }
            return;
        }
        double distSq = guard.squaredDistanceTo(
                patrolTarget.getX() + 0.5, patrolTarget.getY() + 0.5, patrolTarget.getZ() + 0.5);
        if (distSq <= 4.0) {
            waitTicks = 60 + guard.getRandom().nextInt(60); // wait 3–6 s at patrol point
            patrolTarget = null;
        }
    }

    @Override
    public boolean shouldContinue() {
        return patrolTarget != null && guard.getTarget() == null;
    }

    private void pickNewTarget() {
        BlockPos home = guard.getHomePos();
        if (home == null) return;
        int r = GuardEntity.PATROL_RADIUS;
        Random rng = new Random();
        int dx = rng.nextInt(r * 2) - r;
        int dz = rng.nextInt(r * 2) - r;
        patrolTarget = home.add(dx, 0, dz);
        guard.getNavigation().startMovingTo(
                patrolTarget.getX() + 0.5, patrolTarget.getY(), patrolTarget.getZ() + 0.5, 1.0);
    }
}
