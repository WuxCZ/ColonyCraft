package cz.wux.colonycraft.screen;

import cz.wux.colonycraft.blockentity.StockpileBlockEntity;
import cz.wux.colonycraft.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

/**
 * ScreenHandler for the Stockpile (6×9 = 54 slots).
 * Mirrors a double-chest layout.
 */
public class StockpileScreenHandler extends ScreenHandler {

    private final Inventory inventory;

    /** Client-side constructor (called by ScreenHandlerType). */
    public StockpileScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, playerInv, new SimpleInventory(StockpileBlockEntity.SLOTS));
    }

    /** Server-side constructor. */
    public StockpileScreenHandler(int syncId, PlayerInventory playerInv, Inventory inv) {
        super(ModScreenHandlers.STOCKPILE, syncId);
        checkSize(inv, StockpileBlockEntity.SLOTS);
        this.inventory = inv;
        inv.onOpen(playerInv.player);

        // Add 54 stockpile slots (6 rows × 9 columns)
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Player inventory (3 rows)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        // Player hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 198));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot.hasStack()) {
            ItemStack original = slot.getStack();
            newStack = original.copy();
            if (slotIndex < StockpileBlockEntity.SLOTS) {
                if (!insertItem(original, StockpileBlockEntity.SLOTS, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!insertItem(original, 0, StockpileBlockEntity.SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (original.isEmpty()) slot.setStack(ItemStack.EMPTY);
            else slot.markDirty();
        }
        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }
}
