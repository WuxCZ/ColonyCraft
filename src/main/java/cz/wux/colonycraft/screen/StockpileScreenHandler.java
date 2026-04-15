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
 * ScreenHandler for the Stockpile (10 pages × 54 slots = 540 slots).
 * Shows one page at a time with page navigation.
 */
public class StockpileScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private int currentPage = 0;

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

        // Add all 540 stockpile slots — only page 0 is visible initially
        for (int i = 0; i < StockpileBlockEntity.SLOTS; i++) {
            int page = i / StockpileBlockEntity.SLOTS_PER_PAGE;
            int pageIndex = i % StockpileBlockEntity.SLOTS_PER_PAGE;
            int row = pageIndex / 9;
            int col = pageIndex % 9;
            addSlot(new PagedSlot(inv, i, 8 + col * 18, 18 + row * 18, page, this));
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

    public int getCurrentPage() { return currentPage; }
    public int getTotalPages() { return StockpileBlockEntity.TOTAL_PAGES; }

    /** Switch to a different page. */
    public void setPage(int page) {
        this.currentPage = Math.max(0, Math.min(page, StockpileBlockEntity.TOTAL_PAGES - 1));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot.hasStack()) {
            ItemStack original = slot.getStack();
            newStack = original.copy();
            if (slotIndex < StockpileBlockEntity.SLOTS) {
                // From stockpile to player
                if (!insertItem(original, StockpileBlockEntity.SLOTS, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player to stockpile — try current page first, then all
                int start = currentPage * StockpileBlockEntity.SLOTS_PER_PAGE;
                int end = start + StockpileBlockEntity.SLOTS_PER_PAGE;
                if (!insertItem(original, start, end, false)) {
                    if (!insertItem(original, 0, StockpileBlockEntity.SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
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

    /** Slot that is only enabled/visible when its page matches the current page. */
    private static class PagedSlot extends Slot {
        private final int page;
        private final StockpileScreenHandler handler;

        public PagedSlot(Inventory inventory, int index, int x, int y, int page, StockpileScreenHandler handler) {
            super(inventory, index, x, y);
            this.page = page;
            this.handler = handler;
        }

        @Override
        public boolean isEnabled() {
            return page == handler.currentPage;
        }
    }
}
