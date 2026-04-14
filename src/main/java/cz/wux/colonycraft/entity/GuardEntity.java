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

/**
 * Guard colonist — a colonist who defends the colony from monsters.
 *
 * <p>Guards patrol a radius around the colony banner and attack any hostile mob
 * that comes within range. They are armed based on their job:
 * {@link ColonistJob#GUARD_BOW}, {@link ColonistJob#GUARD_CROSSBOW}, or
 * {@link ColonistJob#GUARD_MUSKET}.</p>
 */
public class GuardEntity extends PathAwareEntity implements RangedAttackMob {

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
                .add(EntityAttributes.MAX_HEALTH, 30.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.35)
                .add(EntityAttributes.FOLLOW_RANGE, 40.0)
                .add(EntityAttributes.ARMOR, 4.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void initGoals() {
        // Attack interval differs by weapon: BOW=20t, CROSSBOW=14t, MUSKET=40t
        int attackInterval = switch (guardJob) {
            case GUARD_CROSSBOW -> 14;
            case GUARD_MUSKET   -> 40;
            default             -> 20;
        };
        goalSelector.add(1, new ProjectileAttackGoal(this, 1.0, attackInterval, 15.0f));
        goalSelector.add(2, new GuardPatrolGoal(this));
        goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
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

        // Weapon-specific damage and speed
        float speed  = 1.6f;
        float damage = 2.0f;
        if (guardJob == ColonistJob.GUARD_CROSSBOW) { speed = 2.2f; damage = 3.0f; }
        if (guardJob == ColonistJob.GUARD_MUSKET)   { speed = 3.0f; damage = 7.0f; }

        arrow.setVelocity(dx, dy + dist, dz, speed, 14 - getEntityWorld().getDifficulty().getId() * 4);
        arrow.setDamage(damage);
        getEntityWorld().spawnEntity(arrow);
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    public UUID    getColonyId()                     { return colonyId; }
    public void    setColonyId(UUID id)              { this.colonyId = id; }
    public BlockPos getHomePos()                     { return homePos; }
    public void    setHomePos(BlockPos p)            { this.homePos = p; }
    public ColonistJob getGuardJob()                 { return guardJob; }
    public void    setGuardJob(ColonistJob j)        { this.guardJob = j; }

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
        try { guardJob = ColonistJob.valueOf(view.getString("GuardJob", "GUARD_BOW")); }
        catch (Exception e) { guardJob = ColonistJob.GUARD_BOW; }
        int homeX = view.getInt("HomeX", Integer.MIN_VALUE);
        if (homeX != Integer.MIN_VALUE) homePos = new BlockPos(homeX, view.getInt("HomeY", 0), view.getInt("HomeZ", 0));
    }
}
