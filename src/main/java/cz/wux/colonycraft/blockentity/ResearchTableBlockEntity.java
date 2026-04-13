package cz.wux.colonycraft.blockentity;

import cz.wux.colonycraft.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import java.util.UUID;

/**
 * Block entity for the Research Table. Accumulates science points that are
 * periodically transferred to the colony (handled by {@link cz.wux.colonycraft.entity.ColonistEntity}).
 */
public class ResearchTableBlockEntity extends BlockEntity {

    private int pendingScience = 0;
    private UUID colonyId;

    public ResearchTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESEARCH_TABLE, pos, state);
    }

    public void addScience(int amount) { pendingScience += amount; markDirty(); }

    /** Drains and returns the accumulated science points. */
    public int drainScience() {
        int s = pendingScience;
        pendingScience = 0;
        markDirty();
        return s;
    }

    public UUID getColonyId()                { return colonyId; }
    public void setColonyId(UUID id)         { this.colonyId = id; markDirty(); }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        super.writeNbt(nbt, wrapperLookup);
        nbt.putInt("PendingScience", pendingScience);
        if (colonyId != null) nbt.putUuid("ColonyId", colonyId);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        super.readNbt(nbt, wrapperLookup);
        pendingScience = nbt.getInt("PendingScience");
        if (nbt.containsUuid("ColonyId")) colonyId = nbt.getUuid("ColonyId");
    }
}
