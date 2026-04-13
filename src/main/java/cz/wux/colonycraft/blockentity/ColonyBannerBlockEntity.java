package cz.wux.colonycraft.blockentity;

import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.registry.ModBlockEntities;
import cz.wux.colonycraft.screen.ColonyBannerScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;
import java.util.UUID;

/**
 * Block entity for the Colony Banner block.
 * Stores the colony UUID so it can look up {@link ColonyData} at runtime.
 */
public class ColonyBannerBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {

    private UUID colonyId;

    public ColonyBannerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COLONY_BANNER, pos, state);
    }

    // ── Colony ────────────────────────────────────────────────────────────────

    public void initColony(ServerPlayerEntity player) {
        ColonyManager mgr = ColonyManager.get((ServerWorld) world);
        // Only create if not already present at this position
        if (mgr.getColonyAtBanner(pos).isEmpty()) {
            ColonyData data = mgr.createColony(player.getUuid(), player.getName().getString(), pos);
            this.colonyId = data.getColonyId();
            mgr.markDirty();
            markDirty();
        }
    }

    public Optional<ColonyData> getColony() {
        if (colonyId == null || !(world instanceof ServerWorld sw)) return Optional.empty();
        return ColonyManager.get(sw).getColony(colonyId);
    }

    public UUID getColonyId() { return colonyId; }

    // ── GUI ───────────────────────────────────────────────────────────────────

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.colonycraft.colony_banner");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, net.minecraft.entity.player.PlayerEntity player) {
        return new ColonyBannerScreenHandler(syncId, playerInventory, this);
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        super.writeNbt(nbt, wrapperLookup);
        if (colonyId != null) nbt.putUuid("ColonyId", colonyId);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        super.readNbt(nbt, wrapperLookup);
        if (nbt.containsUuid("ColonyId")) colonyId = nbt.getUuid("ColonyId");
    }
}
