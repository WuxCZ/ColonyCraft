package cz.wux.colonycraft.blockentity;

import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.registry.ModBlockEntities;
import cz.wux.colonycraft.screen.StockpileScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The colony's shared storage. Colonists deposit their produced goods here and
 * withdraw resources they need for their jobs.
 *
 * <p>Has 54 item slots (like a double chest) plus a dedicated food tracker.</p>
 */
public class StockpileBlockEntity extends BlockEntity
        implements Inventory, NamedScreenHandlerFactory {

    public static final int SLOTS = 54;

    /** The set of items counted as food for colony consumption. */
    private static final Set<Item> FOOD_ITEMS = Set.of(
            Items.BREAD, Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.COOKED_CHICKEN,
            Items.COOKED_COD, Items.COOKED_SALMON, Items.COOKED_MUTTON, Items.COOKED_RABBIT,
            Items.SWEET_BERRIES, Items.APPLE, Items.CARROT, Items.BAKED_POTATO,
            Items.PUMPKIN_PIE, Items.COOKIE, Items.MELON_SLICE, Items.MUSHROOM_STEW,
            Items.RABBIT_STEW, Items.BEETROOT_SOUP, Items.HONEY_BOTTLE
    );

    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(SLOTS, ItemStack.EMPTY);
    private UUID colonyId;

    public StockpileBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STOCKPILE, pos, state);
    }

    // ── Colony linkage ────────────────────────────────────────────────────────

    public void setColonyId(UUID id) { this.colonyId = id; }

    // ── Food scanning ─────────────────────────────────────────────────────────

    /**
     * Counts all food items present and updates the colony's food counter.
     * Called periodically from the block's server tick.
     */
    public void syncFoodToColony() {
        if (colonyId == null || !(world instanceof ServerWorld sw)) return;
        ColonyManager.get(sw).getColony(colonyId).ifPresent(colony -> {
            int total = 0;
            for (ItemStack stack : items) {
                if (FOOD_ITEMS.contains(stack.getItem())) {
                    total += stack.getCount();
                }
            }
            // Just update the food counter directly from current inventory
            // (so it reflects what's actually there)
            int current = colony.getFoodUnits();
            int diff = total - current;
            if (diff > 0) colony.addFood(diff);
        });
    }

    /**
     * Tries to consume one food item from the stockpile. Returns true if found.
     */
    public boolean consumeOneFoodItem() {
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (FOOD_ITEMS.contains(stack.getItem()) && !stack.isEmpty()) {
                stack.decrement(1);
                if (stack.isEmpty()) items.set(i, ItemStack.EMPTY);
                markDirty();
                return true;
            }
        }
        return false;
    }

    // ── Item helpers for colonists ─────────────────────────────────────────────

    /** Try to insert an item into the stockpile. Returns remainder count. */
    public int insertItem(ItemStack stack) {
        int remaining = stack.getCount();
        for (int i = 0; i < items.size() && remaining > 0; i++) {
            ItemStack slot = items.get(i);
            if (slot.isEmpty()) {
                int take = Math.min(remaining, stack.getMaxCount());
                items.set(i, new ItemStack(stack.getItem(), take));
                remaining -= take;
                markDirty();
            } else if (ItemStack.areItemsEqual(slot, stack) && slot.getCount() < slot.getMaxCount()) {
                int space = slot.getMaxCount() - slot.getCount();
                int take = Math.min(remaining, space);
                slot.increment(take);
                remaining -= take;
                markDirty();
            }
        }
        return remaining;
    }

    /** Try to withdraw {@code amount} of {@code item}. Returns amount actually taken. */
    public int withdrawItem(Item item, int amount) {
        int taken = 0;
        for (int i = 0; i < items.size() && taken < amount; i++) {
            ItemStack slot = items.get(i);
            if (!slot.isEmpty() && slot.getItem() == item) {
                int take = Math.min(amount - taken, slot.getCount());
                slot.decrement(take);
                if (slot.isEmpty()) items.set(i, ItemStack.EMPTY);
                taken += take;
                markDirty();
            }
        }
        return taken;
    }

    public boolean hasItem(Item item, int amount) {
        int count = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && stack.getItem() == item) count += stack.getCount();
        }
        return count >= amount;
    }

    // ── Inventory ─────────────────────────────────────────────────────────────

    @Override public int size()            { return SLOTS; }
    @Override public boolean isEmpty()     { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getStack(int slot) { return items.get(slot); }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(items, slot, amount);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(items, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) stack.setCount(getMaxCountPerStack());
        markDirty();
    }

    @Override public boolean canPlayerUse(PlayerEntity player) { return true; }
    @Override public void clear() { items.clear(); }

    // ── GUI ───────────────────────────────────────────────────────────────────

    @Override public Text getDisplayName() { return Text.translatable("container.colonycraft.stockpile"); }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new StockpileScreenHandler(syncId, inv, this);
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        super.writeNbt(nbt, wrapperLookup);
        Inventories.writeNbt(nbt, items, wrapperLookup);
        if (colonyId != null) nbt.putUuid("ColonyId", colonyId);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        super.readNbt(nbt, wrapperLookup);
        Inventories.readNbt(nbt, items, wrapperLookup);
        if (nbt.containsUuid("ColonyId")) colonyId = nbt.getUuid("ColonyId");
    }
}
