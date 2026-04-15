package cz.wux.colonycraft.entity.goal;

import cz.wux.colonycraft.blockentity.StockpileBlockEntity;
import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.entity.ColonistEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.EnumSet;

/**
 * Colonists grab their required tool from the stockpile when they don't have one.
 * For example, a Farmer needs a hoe, a Woodcutter needs an axe, a Miner needs a pickaxe.
 */
public class EquipToolGoal extends Goal {

    private static final int COOLDOWN = 200; // Check every 10 seconds

    private final ColonistEntity colonist;
    private int cooldown = 0;

    public EquipToolGoal(ColonistEntity colonist) {
        this.colonist = colonist;
        setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (cooldown-- > 0) return false;
        cooldown = COOLDOWN;
        ColonistJob job = colonist.getColonistJob();
        if (job == ColonistJob.UNEMPLOYED) return false;
        Item needed = getRequiredTool(job);
        if (needed == null) return false;
        // Check if colonist already has the tool in their inventory
        return !hasToolInInventory(needed);
    }

    @Override
    public void start() {
        ColonistJob job = colonist.getColonistJob();
        Item needed = getRequiredTool(job);
        if (needed == null) return;

        colonist.getStockpile().ifPresent(stockpile -> {
            if (stockpile.hasItem(needed, 1)) {
                stockpile.withdrawItem(needed, 1);
                colonist.getColonistInventory().addStack(new ItemStack(needed));
                colonist.setCurrentStatus("📦 Equipped " + needed.getName().getString());
            }
        });
    }

    @Override
    public boolean shouldContinue() {
        return false; // One-shot
    }

    private boolean hasToolInInventory(Item tool) {
        for (int i = 0; i < colonist.getColonistInventory().size(); i++) {
            if (colonist.getColonistInventory().getStack(i).isOf(tool)) return true;
        }
        return false;
    }

    /** Returns the primary tool this job needs, or null if none required. */
    public static Item getRequiredTool(ColonistJob job) {
        return switch (job) {
            case FARMER, BERRY_FARMER, CHICKEN_FARMER -> Items.WOODEN_HOE;
            case WOODCUTTER, FORESTER -> Items.WOODEN_AXE;
            case MINER, STONEMASON, DIGGER -> Items.WOODEN_PICKAXE;
            case FISHERMAN -> Items.FISHING_ROD;
            case BUILDER -> Items.WOODEN_AXE;
            case BLACKSMITH, SMELTER -> Items.IRON_INGOT; // Needs materials
            default -> null;
        };
    }
}
