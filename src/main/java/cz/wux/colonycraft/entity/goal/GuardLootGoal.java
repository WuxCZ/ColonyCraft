package cz.wux.colonycraft.entity.goal;

import cz.wux.colonycraft.blockentity.StockpileBlockEntity;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.entity.GuardEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Makes guards pick up item drops near them and deposit in the colony stockpile.
 */
public class GuardLootGoal extends Goal {

    private static final double PICKUP_RADIUS = 8.0;
    private static final double DEPOSIT_RANGE = 3.0;
    private static final int COOLDOWN = 60;

    private final GuardEntity guard;
    private ItemEntity targetItem;
    private boolean goingToStockpile;
    private ItemStack heldLoot = ItemStack.EMPTY;
    private int cooldown = 0;

    public GuardLootGoal(GuardEntity guard) {
        this.guard = guard;
        setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (cooldown-- > 0) return false;
        if (guard.getTarget() != null && guard.getTarget().isAlive()) return false;

        // If holding loot, go deposit it
        if (!heldLoot.isEmpty()) {
            goingToStockpile = true;
            return true;
        }

        // Look for nearby item drops
        Box searchBox = guard.getBoundingBox().expand(PICKUP_RADIUS);
        List<ItemEntity> items = guard.getEntityWorld().getEntitiesByClass(
                ItemEntity.class, searchBox, e -> !e.cannotPickup() && e.isAlive());
        if (!items.isEmpty()) {
            targetItem = items.get(0);
            goingToStockpile = false;
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        // If enemy appears, abort looting
        if (guard.getTarget() != null && guard.getTarget().isAlive()) return;

        if (goingToStockpile) {
            // Navigate to stockpile and deposit
            getStockpile().ifPresent(stockpile -> {
                var pos = stockpile.getPos();
                double dist = guard.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                if (dist > DEPOSIT_RANGE * DEPOSIT_RANGE) {
                    guard.getNavigation().startMovingTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 1.0);
                } else {
                    // Deposit loot
                    stockpile.insertItem(heldLoot);
                    heldLoot = ItemStack.EMPTY;
                    goingToStockpile = false;
                    cooldown = COOLDOWN;
                }
            });
            if (getStockpile().isEmpty()) {
                // No stockpile — just drop the item
                heldLoot = ItemStack.EMPTY;
                cooldown = COOLDOWN;
            }
        } else if (targetItem != null && targetItem.isAlive()) {
            double dist = guard.squaredDistanceTo(targetItem);
            if (dist > 2.25) {
                guard.getNavigation().startMovingTo(targetItem, 1.0);
            } else {
                // Pick up item
                heldLoot = targetItem.getStack().copy();
                targetItem.discard();
                targetItem = null;
                goingToStockpile = true;
            }
        } else {
            cooldown = COOLDOWN;
        }
    }

    @Override
    public boolean shouldContinue() {
        if (guard.getTarget() != null && guard.getTarget().isAlive()) return false;
        return goingToStockpile || (targetItem != null && targetItem.isAlive());
    }

    @Override
    public void stop() {
        targetItem = null;
        cooldown = COOLDOWN;
    }

    private Optional<StockpileBlockEntity> getStockpile() {
        if (guard.getColonyId() == null) return Optional.empty();
        if (!(guard.getEntityWorld() instanceof ServerWorld sw)) return Optional.empty();
        var colonyOpt = ColonyManager.get(sw.getServer()).getColony(guard.getColonyId());
        if (colonyOpt.isEmpty() || colonyOpt.get().getStockpilePos() == null) return Optional.empty();
        var be = sw.getBlockEntity(colonyOpt.get().getStockpilePos());
        return (be instanceof StockpileBlockEntity s) ? Optional.of(s) : Optional.empty();
    }
}
