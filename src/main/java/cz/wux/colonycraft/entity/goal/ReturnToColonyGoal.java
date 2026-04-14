package cz.wux.colonycraft.entity.goal;

import cz.wux.colonycraft.entity.ColonistEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

/**
 * Makes the colonist return to a safe position near the colony banner when:
 * <ul>
 *   <li>It is night and the colonist is not a guard or far from home</li>
 *   <li>The colonist is close to starving</li>
 * </ul>
 */
public class ReturnToColonyGoal extends Goal {

    private final ColonistEntity colonist;

    public ReturnToColonyGoal(ColonistEntity colonist) {
        this.colonist = colonist;
        setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        BlockPos home = colonist.getHomePos();
        if (home == null) return false;

        boolean starving = colonist.getHungerTicks() >=
                (int)(ColonistEntity.HUNGER_INTERVAL * 1.5);
        boolean nightTime = colonist.isNight();
        double distSq = colonist.squaredDistanceTo(home.getX() + 0.5,
                home.getY() + 0.5, home.getZ() + 0.5);

        return (nightTime || starving) && distSq > 4 * 4;
    }

    @Override
    public void start() {
        BlockPos home = colonist.getHomePos();
        if (home != null) {
            colonist.getNavigation().startMovingTo(
                    home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 1.0);
        }
    }

    @Override
    public boolean shouldContinue() {
        // Stop once we're close to the banner
        BlockPos home = colonist.getHomePos();
        if (home == null) return false;
        double distSq = colonist.squaredDistanceTo(home.getX() + 0.5,
                home.getY() + 0.5, home.getZ() + 0.5);
        return distSq > 9.0;
    }
}
