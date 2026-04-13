package cz.wux.colonycraft.screen;

import cz.wux.colonycraft.blockentity.ColonyBannerBlockEntity;
import cz.wux.colonycraft.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

/**
 * Screen handler for the Colony Banner GUI (colony management overview).
 * Contains no item slots — it simply shows colony stats rendered by
 * {@link cz.wux.colonycraft.client.screen.ColonyBannerScreen}.
 */
public class ColonyBannerScreenHandler extends ScreenHandler {

    private final ColonyBannerBlockEntity blockEntity;

    /** Client-side constructor. */
    public ColonyBannerScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, playerInv, null);
    }

    /** Server-side constructor. */
    public ColonyBannerScreenHandler(int syncId, PlayerInventory playerInv,
                                     ColonyBannerBlockEntity blockEntity) {
        super(ModScreenHandlers.COLONY_BANNER, syncId);
        this.blockEntity = blockEntity;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) { return ItemStack.EMPTY; }

    @Override
    public boolean canUse(PlayerEntity player) { return true; }

    public ColonyBannerBlockEntity getBlockEntity() { return blockEntity; }
}
