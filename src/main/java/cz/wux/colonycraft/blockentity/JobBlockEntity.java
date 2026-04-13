package cz.wux.colonycraft.blockentity;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * Block entity shared by every job-block type. Stores:
 * <ul>
 *   <li>The {@link ColonistJob} this workstation provides</li>
 *   <li>The UUID of the colonist currently assigned here (if any)</li>
 *   <li>The colony UUID this block belongs to</li>
 * </ul>
 */
public class JobBlockEntity extends BlockEntity {

    private ColonistJob job = ColonistJob.UNEMPLOYED;
    private UUID assignedColonistId;
    private UUID colonyId;

    public JobBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.JOB_BLOCK, pos, state);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public ColonistJob getJob()                           { return job; }
    public void        setJob(ColonistJob j)              { this.job = j; markDirty(); }

    public boolean hasAssignedColonist()                  { return assignedColonistId != null; }
    public UUID    getAssignedColonistId()                { return assignedColonistId; }
    public void    assignColonist(UUID uuid)              { this.assignedColonistId = uuid; markDirty(); }
    public void    unassignColonist()                     { this.assignedColonistId = null; markDirty(); }

    public UUID getColonyId()                             { return colonyId; }
    public void setColonyId(UUID id)                      { this.colonyId = id; markDirty(); }

    public Text getJobDisplay() {
        return Text.of(job.displayName + " Workstation");
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        super.writeNbt(nbt, wrapperLookup);
        nbt.putString("Job", job.name());
        if (assignedColonistId != null) nbt.putUuid("AssignedColonist", assignedColonistId);
        if (colonyId != null)           nbt.putUuid("ColonyId", colonyId);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        super.readNbt(nbt, wrapperLookup);
        try { job = ColonistJob.valueOf(nbt.getString("Job")); }
        catch (IllegalArgumentException ignored) { job = ColonistJob.UNEMPLOYED; }
        if (nbt.containsUuid("AssignedColonist")) assignedColonistId = nbt.getUuid("AssignedColonist");
        if (nbt.containsUuid("ColonyId"))         colonyId           = nbt.getUuid("ColonyId");
    }
}
