package cz.wux.colonycraft.entity.goal;

import cz.wux.colonycraft.blockentity.StockpileBlockEntity;
import cz.wux.colonycraft.entity.ColonistEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

/**
 * Makes the colonist path-find to the stockpile and consume one food item when
 * hungry. After eating, hunger is reset.
 */
public class ColonistEatGoal extends Goal {

    private final ColonistEntity colonist;
    private int eatCooldown = 0;

    public ColonistEatGoal(ColonistEntity colonist) {
        this.colonist = colonist;
        setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return eatCooldown <= 0 && colonist.isHungry() && colonist.getStockpilePos() != null;
    }

    @Override
    public void start() {
        BlockPos sp = colonist.getStockpilePos();
        if (sp != null) {
            colonist.getNavigation().startMovingTo(
                    sp.getX() + 0.5, sp.getY(), sp.getZ() + 0.5, 1.0);
        }
    }

    @Override
    public void tick() {
        BlockPos sp = colonist.getStockpilePos();
        if (sp == null) return;
        double distSq = colonist.getPos().squaredDistanceTo(sp.getX() + 0.5, sp.getY() + 0.5, sp.getZ() + 0.5);
        if (distSq <= 4.0) {
            // Close enough to eat
            StockpileBlockEntity stockpile = (StockpileBlockEntity) colonist.getWorld().getBlockEntity(sp);
            if (stockpile != null && stockpile.consumeOneFoodItem()) {
                colonist.resetHunger();
                colonist.heal(4.0f); // eating restores health too
                eatCooldown = 200;   // don't re-trigger eat goal for 10s
            }
        }
    }

    @Override
    public void tick(long l) {
        super.tick(l);
        if (eatCooldown > 0) eatCooldown--;
    }

    @Override
    public boolean shouldContinue() {
        return colonist.isHungry() && eatCooldown <= 0;
    }
}
