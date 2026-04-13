package cz.wux.colonycraft.entity;

import cz.wux.colonycraft.blockentity.JobBlockEntity;
import cz.wux.colonycraft.blockentity.StockpileBlockEntity;
import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.entity.goal.*;
import cz.wux.colonycraft.registry.ModBlockEntities;
import cz.wux.colonycraft.registry.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;

/**
 * A colonist NPC. They path-find to their assigned job block, perform work
 * (calling the appropriate production logic), return results to the stockpile,
 * and eat food if hungry.
 *
 * <p>Colonists are passive — they will not attack players or mobs unless
 * a Guard job is set (which spawns a {@link GuardEntity} instead).</p>
 */
public class ColonistEntity extends PathAwareEntity {

    // ── State saved to NBT ─────────────────────────────────────────────────────
    private UUID colonyId;
    private ColonistJob job = ColonistJob.UNEMPLOYED;
    private BlockPos jobBlockPos;     // position of their assigned workstation
    private BlockPos stockpilePos;    // position of their colony's stockpile
    private BlockPos homePos;         // colony banner position (return-home target)
    private int hungerTicks = 0;      // ticks since last meal
    private int workCooldown = 0;     // ticks until next work action

    static final int HUNGER_INTERVAL  = 6000; // ticks between meals (~5 min)
    static final int STARVATION_DEATH = 24000; // ticks without food before death

    public ColonistEntity(EntityType<? extends ColonistEntity> type, World world) {
        super(type, world);
    }

    // ── Attributes ────────────────────────────────────────────────────────────

    public static DefaultAttributeContainer.Builder createColonistAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.0);
    }

    // ── Goal registration ─────────────────────────────────────────────────────

    @Override
    protected void initGoals() {
        // Priority 1 – return home when starving / night
        goalSelector.add(1, new ReturnToColonyGoal(this));
        // Priority 2 – eat food if hungry
        goalSelector.add(2, new ColonistEatGoal(this));
        // Priority 3 – work at job block
        goalSelector.add(3, new WorkAtJobGoal(this));
        // Priority 4 – wander near home
        goalSelector.add(4, new WanderAroundFarGoal(this, 0.8));
        // Priority 5 – look at nearby player
        goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        // Priority 6 – look around randomly
        goalSelector.add(6, new LookAroundGoal(this));
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) return;

        // Hunger tracking
        hungerTicks++;
        if (hungerTicks >= STARVATION_DEATH) {
            this.kill();
            return;
        }

        // Work cooldown
        if (workCooldown > 0) workCooldown--;
    }

    // ── Colony linkage ────────────────────────────────────────────────────────

    public UUID getColonyId()                         { return colonyId; }
    public void setColonyId(UUID id)                  { this.colonyId = id; }

    public ColonistJob getColonistJob()               { return job; }
    public void setColonistJob(ColonistJob j)         { this.job = j; }

    public BlockPos getJobBlockPos()                  { return jobBlockPos; }
    public void setJobBlockPos(BlockPos p)            { this.jobBlockPos = p; }

    public BlockPos getStockpilePos()                 { return stockpilePos; }
    public void setStockpilePos(BlockPos p)           { this.stockpilePos = p; }

    public BlockPos getHomePos()                      { return homePos; }
    public void setHomePos(BlockPos p)                { this.homePos = p; }

    public int  getHungerTicks()                      { return hungerTicks; }
    public void resetHunger()                         { hungerTicks = 0; }

    public boolean isWorkCoolingDown()                { return workCooldown > 0; }
    public void    startWorkCooldown(int ticks)       { workCooldown = ticks; }

    public boolean isHungry() { return hungerTicks > HUNGER_INTERVAL; }

    /** Returns the StockpileBlockEntity for this colonist's colony, if loaded. */
    public Optional<StockpileBlockEntity> getStockpile() {
        if (stockpilePos == null) return Optional.empty();
        var be = getWorld().getBlockEntity(stockpilePos);
        return (be instanceof StockpileBlockEntity s) ? Optional.of(s) : Optional.empty();
    }

    /** Returns the JobBlockEntity for this colonist's workstation, if loaded. */
    public Optional<JobBlockEntity> getJobBlock() {
        if (jobBlockPos == null) return Optional.empty();
        var be = getWorld().getBlockEntity(jobBlockPos);
        return (be instanceof JobBlockEntity j) ? Optional.of(j) : Optional.empty();
    }

    // ── Spawning helper ───────────────────────────────────────────────────────

    /**
     * Spawns a new colonist for the given colony near the banner position.
     * Tries to assign the first unoccupied job block it can find within 32 blocks.
     */
    public static void spawnForColony(ServerWorld world, ColonyData colony,
                                      BlockPos bannerPos, ColonyManager mgr) {
        ColonistEntity colonist = ModEntities.COLONIST.create(world, SpawnReason.MOB_SUMMONED);
        if (colonist == null) return;

        // Find a spot near the banner
        BlockPos spawnPos = bannerPos.add(
                world.random.nextInt(7) - 3, 1, world.random.nextInt(7) - 3);

        colonist.refreshPositionAndAngles(
                spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
        colonist.setColonyId(colony.getColonyId());
        colonist.setHomePos(bannerPos);
        if (colony.getStockpilePos() != null) colonist.setStockpilePos(colony.getStockpilePos());

        // Auto-assign to an unclaimed job block nearby
        autoAssignJob(world, colonist, colony, bannerPos);

        world.spawnEntity(colonist);
        colony.addColonist(colonist.getUuid());
        mgr.markDirty();
    }

    private static void autoAssignJob(ServerWorld world, ColonistEntity colonist,
                                      ColonyData colony, BlockPos bannerPos) {
        // Scan a 32-block radius for unoccupied job blocks
        for (BlockPos candidate : BlockPos.iterate(bannerPos.add(-16, -4, -16),
                                                    bannerPos.add(16, 4, 16))) {
            var be = world.getBlockEntity(candidate);
            if (be instanceof JobBlockEntity jb
                    && !jb.hasAssignedColonist()
                    && colony.isJobUnlocked(jb.getJob())) {
                jb.assignColonist(colonist.getUuid());
                colonist.setColonistJob(jb.getJob());
                colonist.setJobBlockPos(candidate.toImmutable());
                return;
            }
        }
        // No job block found — colonist stays unemployed and wanders
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (colonyId != null)   nbt.putUuid("ColonyId", colonyId);
        nbt.putString("Job", job.name());
        nbt.putInt("HungerTicks", hungerTicks);
        if (jobBlockPos != null) {
            nbt.putInt("JobX", jobBlockPos.getX());
            nbt.putInt("JobY", jobBlockPos.getY());
            nbt.putInt("JobZ", jobBlockPos.getZ());
        }
        if (stockpilePos != null) {
            nbt.putInt("StockX", stockpilePos.getX());
            nbt.putInt("StockY", stockpilePos.getY());
            nbt.putInt("StockZ", stockpilePos.getZ());
        }
        if (homePos != null) {
            nbt.putInt("HomeX", homePos.getX());
            nbt.putInt("HomeY", homePos.getY());
            nbt.putInt("HomeZ", homePos.getZ());
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.containsUuid("ColonyId")) colonyId = nbt.getUuid("ColonyId");
        try { job = ColonistJob.valueOf(nbt.getString("Job")); }
        catch (Exception e) { job = ColonistJob.UNEMPLOYED; }
        hungerTicks = nbt.getInt("HungerTicks");
        if (nbt.contains("JobX")) jobBlockPos = new BlockPos(nbt.getInt("JobX"), nbt.getInt("JobY"), nbt.getInt("JobZ"));
        if (nbt.contains("StockX")) stockpilePos = new BlockPos(nbt.getInt("StockX"), nbt.getInt("StockY"), nbt.getInt("StockZ"));
        if (nbt.contains("HomeX")) homePos = new BlockPos(nbt.getInt("HomeX"), nbt.getInt("HomeY"), nbt.getInt("HomeZ"));
    }
}
