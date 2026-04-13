package cz.wux.colonycraft.entity;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.entity.goal.GuardPatrolGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Guard colonist — a colonist who defends the colony from monsters.
 *
 * <p>Guards patrol a radius around the colony banner and attack any hostile mob
 * that comes within range. They are armed based on their job:
 * {@link ColonistJob#GUARD_BOW}, {@link ColonistJob#GUARD_CROSSBOW}, or
 * {@link ColonistJob#GUARD_MUSKET}.</p>
 */
public class GuardEntity extends PathAwareEntity {

    private UUID colonyId;
    private BlockPos homePos;
    private ColonistJob guardJob = ColonistJob.GUARD_BOW;

    /** Patrol radius around the banner in blocks. */
    public static final int PATROL_RADIUS = 24;

    public GuardEntity(EntityType<? extends GuardEntity> type, World world) {
        super(type, world);
        this.setCanPickUpLoot(false);
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder createGuardAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0)
                .add(EntityAttributes.GENERIC_ARMOR, 4.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void initGoals() {
        // Attack any nearby hostile mob
        goalSelector.add(1, new ProjectileAttackGoal(this, 1.0, 20, 15.0f));
        goalSelector.add(2, new GuardPatrolGoal(this));
        goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        goalSelector.add(4, new LookAroundGoal(this));

        targetSelector.add(1, new ActiveTargetGoal<>(this, HostileEntity.class, true));
    }

    @Override
    public void shootAt(net.minecraft.entity.LivingEntity target, float pullProgress) {
        // Fire an arrow toward the target
        net.minecraft.entity.projectile.ArrowEntity arrow =
                new net.minecraft.entity.projectile.ArrowEntity(getWorld(), this,
                        new net.minecraft.item.ItemStack(net.minecraft.item.Items.ARROW),
                        null);
        double dx = target.getX() - getX();
        double dy = target.getBodyY(0.3333) - arrow.getY();
        double dz = target.getZ() - getZ();
        double dist = Math.sqrt(dx*dx + dz*dz) * 0.2;
        arrow.setVelocity(dx, dy + dist, dz, 1.6f, 14 - getWorld().getDifficulty().getId() * 4);
        getWorld().spawnEntity(arrow);
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    public UUID    getColonyId()                     { return colonyId; }
    public void    setColonyId(UUID id)              { this.colonyId = id; }
    public BlockPos getHomePos()                     { return homePos; }
    public void    setHomePos(BlockPos p)            { this.homePos = p; }
    public ColonistJob getGuardJob()                 { return guardJob; }
    public void    setGuardJob(ColonistJob j)        { this.guardJob = j; }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (colonyId != null) nbt.putUuid("ColonyId", colonyId);
        nbt.putString("GuardJob", guardJob.name());
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
        try { guardJob = ColonistJob.valueOf(nbt.getString("GuardJob")); }
        catch (Exception e) { guardJob = ColonistJob.GUARD_BOW; }
        if (nbt.contains("HomeX")) homePos = new BlockPos(nbt.getInt("HomeX"), nbt.getInt("HomeY"), nbt.getInt("HomeZ"));
    }
}
