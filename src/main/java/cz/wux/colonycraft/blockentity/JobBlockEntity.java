package cz.wux.colonycraft.blockentity;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
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
    /** CS-style work area: colonist searches within this rectangle instead of a fixed radius. */
    private BlockPos areaMin;
    private BlockPos areaMax;

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

    public BlockPos getAreaMin()                          { return areaMin; }
    public BlockPos getAreaMax()                          { return areaMax; }
    public boolean  hasArea()                             { return areaMin != null && areaMax != null; }
    public void setArea(BlockPos min, BlockPos max)       {
        this.areaMin = min; this.areaMax = max; markDirty();
    }

    public Text getJobDisplay() {
        return Text.of(job.displayName + " Workstation");
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void writeData(WriteView view) {
        view.putString("Job", job.name());
        if (assignedColonistId != null) view.putIntArray("AssignedColonist", Uuids.toIntArray(assignedColonistId));
        if (colonyId != null)           view.putIntArray("ColonyId", Uuids.toIntArray(colonyId));
        if (areaMin != null) {
            view.putInt("AreaMinX", areaMin.getX()); view.putInt("AreaMinY", areaMin.getY()); view.putInt("AreaMinZ", areaMin.getZ());
        }
        if (areaMax != null) {
            view.putInt("AreaMaxX", areaMax.getX()); view.putInt("AreaMaxY", areaMax.getY()); view.putInt("AreaMaxZ", areaMax.getZ());
        }
    }

    @Override
    protected void readData(ReadView view) {
        try { job = ColonistJob.valueOf(view.getString("Job", "UNEMPLOYED")); }
        catch (IllegalArgumentException ignored) { job = ColonistJob.UNEMPLOYED; }
        assignedColonistId = view.getOptionalIntArray("AssignedColonist").map(Uuids::toUuid).orElse(null);
        colonyId           = view.getOptionalIntArray("ColonyId").map(Uuids::toUuid).orElse(null);        int aMinX = view.getInt("AreaMinX", Integer.MIN_VALUE);
        if (aMinX != Integer.MIN_VALUE)
            areaMin = new BlockPos(aMinX, view.getInt("AreaMinY", 0), view.getInt("AreaMinZ", 0));
        int aMaxX = view.getInt("AreaMaxX", Integer.MIN_VALUE);
        if (aMaxX != Integer.MIN_VALUE)
            areaMax = new BlockPos(aMaxX, view.getInt("AreaMaxY", 0), view.getInt("AreaMaxZ", 0));    }
}
