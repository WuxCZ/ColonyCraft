package cz.wux.colonycraft.entity;

import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.registry.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * The nightly attackers. They spawn in waves near the colony banner and
 * path-find toward it.
 *
 * <p>Wave size and monster health scale with {@link ColonyData#getDaysSurvived()}.</p>
 */
public class ColonyMonsterEntity extends HostileEntity {

    private BlockPos targetBanner;
    private UUID     colonyId;

    public ColonyMonsterEntity(EntityType<? extends ColonyMonsterEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createMonsterAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(1, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.add(2, new WalkTowardsNearestVillageGoal(this, 1));  // repurposed below
        goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));

        // Attack colonists & guards as secondary target
        targetSelector.add(1, new ActiveTargetGoal<>(this, ColonistEntity.class, false));
        targetSelector.add(2, new ActiveTargetGoal<>(this, GuardEntity.class, false));
        targetSelector.add(3, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    // ── Wave spawning ─────────────────────────────────────────────────────────

    /**
     * Spawns a wave of monsters around the colony. Wave size:
     * 4 + daysSurvived * 2; health scales every 5 days.
     */
    public static void spawnWave(ServerWorld world, ColonyData colony, BlockPos bannerPos) {
        int days   = colony.getDaysSurvived();
        int count  = 4 + days * 2;
        double baseHp = 20.0 + (days / 5) * 10.0;

        for (int i = 0; i < count; i++) {
            // Spawn 40–64 blocks out from banner, random direction
            double angle  = world.random.nextDouble() * Math.PI * 2;
            double radius = 40 + world.random.nextInt(24);
            double spawnX = bannerPos.getX() + Math.cos(angle) * radius;
            double spawnZ = bannerPos.getZ() + Math.sin(angle) * radius;
            int    spawnY = world.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE,
                    (int) spawnX, (int) spawnZ);

            ColonyMonsterEntity monster = ModEntities.COLONY_MONSTER.create(world, SpawnReason.EVENT);
            if (monster == null) continue;

            monster.refreshPositionAndAngles(spawnX, spawnY, spawnZ, 0, 0);
            monster.targetBanner = bannerPos;
            monster.colonyId = colony.getColonyId();

            // Scale HP
            monster.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                   .setBaseValue(baseHp);
            monster.setHealth((float) baseHp);

            world.spawnEntity(monster);
        }
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    public BlockPos getTargetBanner() { return targetBanner; }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (targetBanner != null) {
            nbt.putInt("TgtX", targetBanner.getX());
            nbt.putInt("TgtY", targetBanner.getY());
            nbt.putInt("TgtZ", targetBanner.getZ());
        }
        if (colonyId != null) nbt.putUuid("ColonyId", colonyId);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("TgtX"))
            targetBanner = new BlockPos(nbt.getInt("TgtX"), nbt.getInt("TgtY"), nbt.getInt("TgtZ"));
        if (nbt.containsUuid("ColonyId")) colonyId = nbt.getUuid("ColonyId");
    }
}
