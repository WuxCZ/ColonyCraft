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
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
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

    public static final int HUNGER_INTERVAL  = 6000; // ticks between meals (~5 min)
    public static final int STARVATION_DEATH = 24000; // ticks without food before death

    public ColonistEntity(EntityType<? extends ColonistEntity> type, World world) {
        super(type, world);
    }

    // ── Attributes ────────────────────────────────────────────────────────────

    public static DefaultAttributeContainer.Builder createColonistAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    // ── Goal registration ─────────────────────────────────────────────────────

    @Override
    protected void initGoals() {
        // Priority 1 – return home when starving / night
        goalSelector.add(1, new ReturnToColonyGoal(this));
        // Priority 2 – eat food if hungry
        goalSelector.add(2, new ColonistEatGoal(this));
        // Priority 3a – physical jobs: these check job in canStart()
        goalSelector.add(3, new ChopTreeGoal(this));       // WOODCUTTER
        goalSelector.add(3, new HarvestCropsGoal(this));   // FARMER
        goalSelector.add(3, new MineBlocksGoal(this));     // MINER
        goalSelector.add(3, new PlantSaplingsGoal(this));  // FORESTER
        // Priority 3b – recipe-based jobs (cook, smelter, etc.)
        goalSelector.add(4, new WorkAtJobGoal(this));
        // Priority 5 – wander near home
        goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        // Priority 6 – look at nearby player
        goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        // Priority 7 – look around randomly
        goalSelector.add(7, new LookAroundGoal(this));
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        if (getEntityWorld().isClient()) return;

        // Hunger tracking
        hungerTicks++;
        if (hungerTicks >= STARVATION_DEATH) {
            if (getEntityWorld() instanceof ServerWorld sw) { this.kill(sw); }
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
        var be = getEntityWorld().getBlockEntity(stockpilePos);
        return (be instanceof StockpileBlockEntity s) ? Optional.of(s) : Optional.empty();
    }

    /** Returns the JobBlockEntity for this colonist's workstation, if loaded. */
    public Optional<JobBlockEntity> getJobBlock() {
        if (jobBlockPos == null) return Optional.empty();
        var be = getEntityWorld().getBlockEntity(jobBlockPos);
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
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (colonyId != null)   view.putIntArray("ColonyId", Uuids.toIntArray(colonyId));
        view.putString("Job", job.name());
        view.putInt("HungerTicks", hungerTicks);
        if (jobBlockPos != null) {
            view.putInt("JobX", jobBlockPos.getX());
            view.putInt("JobY", jobBlockPos.getY());
            view.putInt("JobZ", jobBlockPos.getZ());
        }
        if (stockpilePos != null) {
            view.putInt("StockX", stockpilePos.getX());
            view.putInt("StockY", stockpilePos.getY());
            view.putInt("StockZ", stockpilePos.getZ());
        }
        if (homePos != null) {
            view.putInt("HomeX", homePos.getX());
            view.putInt("HomeY", homePos.getY());
            view.putInt("HomeZ", homePos.getZ());
        }
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);
        colonyId = view.getOptionalIntArray("ColonyId").map(Uuids::toUuid).orElse(null);
        try { job = ColonistJob.valueOf(view.getString("Job", "UNEMPLOYED")); }
        catch (Exception e) { job = ColonistJob.UNEMPLOYED; }
        hungerTicks = view.getInt("HungerTicks", 0);
        int jobX = view.getInt("JobX", Integer.MIN_VALUE);
        if (jobX != Integer.MIN_VALUE) jobBlockPos = new BlockPos(jobX, view.getInt("JobY", 0), view.getInt("JobZ", 0));
        int stockX = view.getInt("StockX", Integer.MIN_VALUE);
        if (stockX != Integer.MIN_VALUE) stockpilePos = new BlockPos(stockX, view.getInt("StockY", 0), view.getInt("StockZ", 0));
        int homeX = view.getInt("HomeX", Integer.MIN_VALUE);
        if (homeX != Integer.MIN_VALUE) homePos = new BlockPos(homeX, view.getInt("HomeY", 0), view.getInt("HomeZ", 0));
    }
}
