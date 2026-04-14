package cz.wux.colonycraft.entity;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.entity.goal.GuardPatrolGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class GuardEntity extends PathAwareEntity implements RangedAttackMob {

    private UUID colonyId;
    private BlockPos homePos;
    private ColonistJob guardJob = ColonistJob.GUARD;

    public static final int PATROL_RADIUS = 24;

    public GuardEntity(EntityType<? extends GuardEntity> type, World world) {
        super(type, world);
        this.setCanPickUpLoot(false);
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder createGuardAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 30.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.FOLLOW_RANGE, 40.0)
                .add(EntityAttributes.ARMOR, 4.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(1, new ProjectileAttackGoal(this, 1.0, 20, 15.0f));
        goalSelector.add(4, new LookAroundGoal(this));
        targetSelector.add(1, new ActiveTargetGoal<>(this, HostileEntity.class, true));
    }

    @Override
    public void shootAt(net.minecraft.entity.LivingEntity target, float pullProgress) {
        net.minecraft.entity.projectile.ArrowEntity arrow =
                new net.minecraft.entity.projectile.ArrowEntity(getEntityWorld(), this,
                        new net.minecraft.item.ItemStack(net.minecraft.item.Items.ARROW),
                        null);
        double dx = target.getX() - getX();
        double dy = target.getBodyY(0.3333) - arrow.getY();
        double dz = target.getZ() - getZ();
        double dist = Math.sqrt(dx*dx + dz*dz) * 0.2;

        arrow.setVelocity(dx, dy + dist, dz, 1.6f, 14 - getEntityWorld().getDifficulty().getId() * 4);
        arrow.setDamage(3.0);
        getEntityWorld().playSound(null, getBlockPos(),
                net.minecraft.sound.SoundEvents.ENTITY_ARROW_SHOOT,
                net.minecraft.sound.SoundCategory.NEUTRAL, 1.0f, 1.0f);
        getEntityWorld().spawnEntity(arrow);
    }

    public UUID    getColonyId()               { return colonyId; }
    public void    setColonyId(UUID id)        { this.colonyId = id; }
    public BlockPos getHomePos()               { return homePos; }
    public void    setHomePos(BlockPos p)      { this.homePos = p; }
    public ColonistJob getGuardJob()           { return guardJob; }
    public void    setGuardJob(ColonistJob j)  { this.guardJob = j; }

    @Override
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (colonyId != null) view.putIntArray("ColonyId", Uuids.toIntArray(colonyId));
        view.putString("GuardJob", guardJob.name());
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
        try { guardJob = ColonistJob.valueOf(view.getString("GuardJob", "GUARD")); }
        catch (Exception e) { guardJob = ColonistJob.GUARD; }
        int homeX = view.getInt("HomeX", Integer.MIN_VALUE);
        if (homeX != Integer.MIN_VALUE) homePos = new BlockPos(homeX, view.getInt("HomeY", 0), view.getInt("HomeZ", 0));
    }
}