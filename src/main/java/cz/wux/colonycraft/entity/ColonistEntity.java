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

public class ColonistEntity extends PathAwareEntity {

    /** Random colonist name pool — Colony Survival style. */
    private static final String[] COLONIST_NAMES = {
        "Aiden", "Bjorn", "Cora", "Darius", "Elara", "Finn", "Greta", "Hugo",
        "Iris", "Jasper", "Kira", "Liam", "Mira", "Nolan", "Opal", "Pavel",
        "Quinn", "Rosa", "Sven", "Thea", "Ulf", "Vera", "Wren", "Xander",
        "Yara", "Zane", "Ada", "Boris", "Clara", "Dante", "Eva", "Felix",
        "Hana", "Ivan", "Jana", "Karl", "Lea", "Milo", "Nina", "Otto",
        "Petra", "Rolf", "Sara", "Tomas", "Ursa", "Viktor", "Wanda", "Yuri",
        "Zelda", "Axel", "Bela", "Cyrus", "Diana", "Emil", "Freya", "Georg",
        "Helga", "Igor", "Julia", "Klaus", "Lena", "Marco", "Nora", "Oskar"
    };

    private UUID colonyId;
    private ColonistJob job = ColonistJob.UNEMPLOYED;
    private BlockPos jobBlockPos;
    private BlockPos stockpilePos;
    private BlockPos homePos;
    private int hungerTicks = 0;
    private int workCooldown = 0;
    private String colonistName;

    /** Current activity status for thought bubble rendering. */
    private String currentStatus = "Idle";

    public static final int HUNGER_INTERVAL  = 6000;
    public static final int STARVATION_DEATH = 24000;

    public ColonistEntity(EntityType<? extends ColonistEntity> type, World world) {
        super(type, world);
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder createColonistAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(1, new ReturnToColonyGoal(this));
        goalSelector.add(1, new ColonistSleepGoal(this));
        goalSelector.add(2, new ColonistEatGoal(this));
        goalSelector.add(3, new ChopTreeGoal(this));
        goalSelector.add(3, new HarvestCropsGoal(this));
        goalSelector.add(3, new MineBlocksGoal(this));
        goalSelector.add(3, new PlantSaplingsGoal(this));
        goalSelector.add(4, new WorkAtJobGoal(this));
        goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        goalSelector.add(7, new LookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (getEntityWorld().isClient()) return;

        hungerTicks++;
        if (hungerTicks >= STARVATION_DEATH) {
            if (getEntityWorld() instanceof ServerWorld sw) { this.kill(sw); }
            return;
        }

        if (workCooldown > 0) workCooldown--;

        // Dynamically update stockpilePos from colony data if not set
        if (stockpilePos == null && colonyId != null && getEntityWorld() instanceof ServerWorld sw) {
            ColonyManager.get(sw.getServer()).getColony(colonyId).ifPresent(colony -> {
                if (colony.getStockpilePos() != null) {
                    stockpilePos = colony.getStockpilePos();
                }
            });
        }

        // Update status for thought bubble
        if (getPose() == net.minecraft.entity.EntityPose.SLEEPING) {
            currentStatus = "\u263E Sleeping";
        } else if (hungerTicks > HUNGER_INTERVAL) {
            currentStatus = "\u2620 Hungry!";
        } else if (workCooldown > 0) {
            currentStatus = "\u2692 Working";
        } else if (job == ColonistJob.UNEMPLOYED) {
            currentStatus = "\u2639 No job";
        } else if (isNight()) {
            currentStatus = "\u263E Going to bed";
        } else {
            currentStatus = "\u2692 " + job.displayName;
        }
    }

    // -- Status --
    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String s) { this.currentStatus = s; }

    // -- Colonist name --
    public String getColonistName() { return colonistName; }
    public void setColonistName(String name) { this.colonistName = name; }

    /** Assign a random name if none set yet. */
    public void assignRandomName() {
        if (colonistName == null || colonistName.isEmpty()) {
            colonistName = COLONIST_NAMES[getEntityWorld().random.nextInt(COLONIST_NAMES.length)];
        }
    }

    // -- Colony linkage --
    public UUID getColonyId() { return colonyId; }
    public void setColonyId(UUID id) { this.colonyId = id; }

    public ColonistJob getColonistJob() { return job; }
    public void setColonistJob(ColonistJob j) {
        this.job = j;
        updateDisplayName();
    }

    /** Rebuilds the visible custom name from colonist name + job. */
    private void updateDisplayName() {
        String label = (job == ColonistJob.UNEMPLOYED) ? "Colonist" : job.displayName;
        if (colonistName != null && !colonistName.isEmpty()) {
            label = colonistName + " - " + label;
        }
        this.setCustomName(net.minecraft.text.Text.literal(label));
        this.setCustomNameVisible(true);
    }

    public BlockPos getJobBlockPos() { return jobBlockPos; }
    public void setJobBlockPos(BlockPos p) { this.jobBlockPos = p; }

    public BlockPos getStockpilePos() { return stockpilePos; }
    public void setStockpilePos(BlockPos p) { this.stockpilePos = p; }

    public BlockPos getHomePos() { return homePos; }
    public void setHomePos(BlockPos p) { this.homePos = p; }

    public int getHungerTicks() { return hungerTicks; }
    public void resetHunger() { hungerTicks = 0; }

    public boolean isWorkCoolingDown() { return workCooldown > 0; }
    public void startWorkCooldown(int ticks) { workCooldown = ticks; }

    public boolean isHungry() { return hungerTicks > HUNGER_INTERVAL; }

    public boolean isNight() {
        long time = getEntityWorld().getTimeOfDay() % 24000L;
        return time >= 12786;
    }

    public Optional<StockpileBlockEntity> getStockpile() {
        if (stockpilePos == null) return Optional.empty();
        var be = getEntityWorld().getBlockEntity(stockpilePos);
        return (be instanceof StockpileBlockEntity s) ? Optional.of(s) : Optional.empty();
    }

    public Optional<JobBlockEntity> getJobBlock() {
        if (jobBlockPos == null) return Optional.empty();
        var be = getEntityWorld().getBlockEntity(jobBlockPos);
        return (be instanceof JobBlockEntity j) ? Optional.of(j) : Optional.empty();
    }

    // -- Spawning --
    public static void spawnForColony(ServerWorld world, ColonyData colony,
                                      BlockPos bannerPos, ColonyManager mgr) {
        JobBlockEntity targetJob = findUnclaimedJob(world, colony, bannerPos);

        if (targetJob != null && targetJob.getJob().isGuard()) {
            GuardEntity guard = ModEntities.GUARD.create(world, SpawnReason.MOB_SUMMONED);
            if (guard == null) return;

            BlockPos spawnPos = bannerPos.add(
                    world.random.nextInt(7) - 3, 1, world.random.nextInt(7) - 3);
            guard.refreshPositionAndAngles(
                    spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
            guard.setColonyId(colony.getColonyId());
            guard.setHomePos(bannerPos);
            guard.setGuardJob(targetJob.getJob());
            targetJob.assignColonist(guard.getUuid());
            guard.assignRandomName();
            guard.updateDisplayName();

            world.spawnEntity(guard);
            world.playSound(null, bannerPos, cz.wux.colonycraft.registry.ModSounds.COLONIST_SPAWN,
                    net.minecraft.sound.SoundCategory.NEUTRAL, 1.0f, 0.9f + world.random.nextFloat() * 0.2f);
            colony.addColonist(guard.getUuid());
            mgr.markDirty();
            return;
        }

        ColonistEntity colonist = ModEntities.COLONIST.create(world, SpawnReason.MOB_SUMMONED);
        if (colonist == null) return;

        BlockPos spawnPos = bannerPos.add(
                world.random.nextInt(7) - 3, 1, world.random.nextInt(7) - 3);
        colonist.refreshPositionAndAngles(
                spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
        colonist.setColonyId(colony.getColonyId());
        colonist.setHomePos(bannerPos);
        if (colony.getStockpilePos() != null) colonist.setStockpilePos(colony.getStockpilePos());
        colonist.assignRandomName();

        if (targetJob != null) {
            targetJob.assignColonist(colonist.getUuid());
            colonist.setColonistJob(targetJob.getJob());
            colonist.setJobBlockPos(BlockPos.ofFloored(
                    targetJob.getPos().getX(), targetJob.getPos().getY(), targetJob.getPos().getZ()));
        } else {
            colonist.updateDisplayName();
        }

        world.spawnEntity(colonist);
        world.playSound(null, bannerPos, cz.wux.colonycraft.registry.ModSounds.COLONIST_SPAWN,
                net.minecraft.sound.SoundCategory.NEUTRAL, 1.0f, 0.9f + world.random.nextFloat() * 0.2f);
        colony.addColonist(colonist.getUuid());
        mgr.markDirty();
    }

    private static JobBlockEntity findUnclaimedJob(ServerWorld world, ColonyData colony, BlockPos bannerPos) {
        for (BlockPos candidate : BlockPos.iterate(bannerPos.add(-48, -8, -48),
                                                    bannerPos.add(48, 8, 48))) {
            var be = world.getBlockEntity(candidate);
            if (be instanceof JobBlockEntity jb
                    && !jb.hasAssignedColonist()
                    && colony.isJobUnlocked(jb.getJob())) {
                return jb;
            }
        }
        return null;
    }

    // -- NBT --

    @Override
    public void onDeath(net.minecraft.entity.damage.DamageSource source) {
        super.onDeath(source);
        if (getEntityWorld() instanceof ServerWorld sw && colonyId != null) {
            ColonyManager mgr = ColonyManager.get(sw.getServer());
            mgr.getColony(colonyId).ifPresent(colony -> {
                colony.removeColonist(getUuid());
                mgr.markDirty();
            });
            if (jobBlockPos != null) {
                var be = sw.getBlockEntity(jobBlockPos);
                if (be instanceof JobBlockEntity jb && getUuid().equals(jb.getAssignedColonistId())) {
                    jb.unassignColonist();
                }
            }
            String name = getCustomName() != null ? getCustomName().getString() : "Colonist";
            for (var player : sw.getPlayers()) {
                if (player.squaredDistanceTo(getX(), getY(), getZ()) < 10000) {
                    player.sendMessage(net.minecraft.text.Text.literal(
                            "\u00a7c\u2620 " + name + " has died!"), false);
                }
            }
        }
    }

    @Override
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (colonyId != null) view.putIntArray("ColonyId", Uuids.toIntArray(colonyId));
        view.putString("Job", job.name());
        view.putInt("HungerTicks", hungerTicks);
        if (colonistName != null) view.putString("ColonistName", colonistName);
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
        colonistName = view.getString("ColonistName", null);
        colonyId = view.getOptionalIntArray("ColonyId").map(Uuids::toUuid).orElse(null);
        setColonistJob(ColonistJob.fromString(view.getString("Job", "UNEMPLOYED")));
        hungerTicks = view.getInt("HungerTicks", 0);
        int jobX = view.getInt("JobX", Integer.MIN_VALUE);
        if (jobX != Integer.MIN_VALUE) jobBlockPos = new BlockPos(jobX, view.getInt("JobY", 0), view.getInt("JobZ", 0));
        int stockX = view.getInt("StockX", Integer.MIN_VALUE);
        if (stockX != Integer.MIN_VALUE) stockpilePos = new BlockPos(stockX, view.getInt("StockY", 0), view.getInt("StockZ", 0));
        int homeX = view.getInt("HomeX", Integer.MIN_VALUE);
        if (homeX != Integer.MIN_VALUE) homePos = new BlockPos(homeX, view.getInt("HomeY", 0), view.getInt("HomeZ", 0));
    }
}