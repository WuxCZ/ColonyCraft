package cz.wux.colonycraft.entity;

import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.registry.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Wave spawner utility and fallback entity.
 * Waves now spawn vanilla Zombies and Skeletons that burn at sunrise.
 * Wave size scales with colonist count (like Colony Survival).
 */
public class ColonyMonsterEntity extends HostileEntity {

    private BlockPos targetBanner;
    private UUID     colonyId;

    public ColonyMonsterEntity(EntityType<? extends ColonyMonsterEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createMonsterAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(1, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.add(2, new WanderAroundFarGoal(this, 1.0));
        goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        targetSelector.add(1, new ActiveTargetGoal<>(this, ColonistEntity.class, false));
        targetSelector.add(2, new ActiveTargetGoal<>(this, GuardEntity.class, false));
        targetSelector.add(3, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    /**
     * Spawns a wave of vanilla zombies, skeletons and spiders.
     * Colony Survival-like scaling: wave size grows aggressively with colony size and days.
     */
    public static void spawnWave(ServerWorld world, ColonyData colony, BlockPos bannerPos) {
        int colonists = colony.getColonistCount();
        int days = colony.getDaysSurvived();
        // Aggressive Colony Survival-like scaling
        int count = Math.max(6, Math.min(100, (int)(colonists * 2.5) + days * 2));
        
        for (int i = 0; i < count; i++) {
            double angle  = world.random.nextDouble() * Math.PI * 2;
            double radius = 40 + world.random.nextInt(24);
            double spawnX = bannerPos.getX() + Math.cos(angle) * radius;
            double spawnZ = bannerPos.getZ() + Math.sin(angle) * radius;
            int    spawnY = world.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE,
                    (int) spawnX, (int) spawnZ);

            float roll = world.random.nextFloat();
            if (days >= 5 && roll < 0.15f) {
                // 15% spiders after day 5
                net.minecraft.entity.mob.SpiderEntity spider =
                        EntityType.SPIDER.create(world, SpawnReason.EVENT);
                if (spider == null) continue;
                spider.refreshPositionAndAngles(spawnX, spawnY, spawnZ,
                    world.random.nextFloat() * 360f, 0);
                double hp = 16.0 + Math.min(days * 1.5, 50.0);
                spider.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(hp);
                spider.setHealth((float) hp);
                world.spawnEntity(spider);
            } else if (roll < 0.55f) {
                // ~40% zombies
                ZombieEntity zombie = EntityType.ZOMBIE.create(world, SpawnReason.EVENT);
                if (zombie == null) continue;
                zombie.refreshPositionAndAngles(spawnX, spawnY, spawnZ, 
                    world.random.nextFloat() * 360f, 0);
                double hp = 20.0 + Math.min(days * 2.0, 80.0);
                zombie.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(hp);
                zombie.setHealth((float) hp);
                world.spawnEntity(zombie);
            } else {
                // ~45% skeletons
                SkeletonEntity skeleton = EntityType.SKELETON.create(world, SpawnReason.EVENT);
                if (skeleton == null) continue;
                skeleton.refreshPositionAndAngles(spawnX, spawnY, spawnZ,
                    world.random.nextFloat() * 360f, 0);
                double hp = 20.0 + Math.min(days * 1.5, 60.0);
                skeleton.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(hp);
                skeleton.setHealth((float) hp);
                world.spawnEntity(skeleton);
            }
        }
    }

    public BlockPos getTargetBanner() { return targetBanner; }

    @Override
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (targetBanner != null) {
            view.putInt("TgtX", targetBanner.getX());
            view.putInt("TgtY", targetBanner.getY());
            view.putInt("TgtZ", targetBanner.getZ());
        }
        if (colonyId != null) view.putIntArray("ColonyId", Uuids.toIntArray(colonyId));
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);
        int tgtX = view.getInt("TgtX", Integer.MIN_VALUE);
        if (tgtX != Integer.MIN_VALUE)
            targetBanner = new BlockPos(tgtX, view.getInt("TgtY", 0), view.getInt("TgtZ", 0));
        colonyId = view.getOptionalIntArray("ColonyId").map(Uuids::toUuid).orElse(null);
    }
}