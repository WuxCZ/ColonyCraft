package cz.wux.colonycraft.entity;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.entity.goal.GuardPatrolGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class GuardEntity extends PathAwareEntity implements RangedAttackMob {

    /** Random guard name pool. */
    private static final String[] GUARD_NAMES = {
        "Alaric", "Baldur", "Cassius", "Drake", "Erik", "Flint", "Gareth", "Harald",
        "Ingrid", "Jarek", "Katla", "Leif", "Magnus", "Njord", "Olaf", "Ragnar",
        "Sigrid", "Tormund", "Ulric", "Valka", "Wolf", "Astrid", "Bjorn", "Dagny",
        "Einar", "Freydis", "Gunnar", "Hilda", "Ivar", "Jorunn", "Knut", "Liv"
    };

    private UUID colonyId;
    private BlockPos homePos;
    private ColonistJob guardJob = ColonistJob.GUARD_SWORD;
    private boolean goalsConfigured = false;
    private String guardName;
    private String currentStatus = "\u2694 Guarding";

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
                .add(EntityAttributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    protected void initGoals() {
        // Universal goals only — combat/patrol goals added by configureGoals()
        goalSelector.add(4, new LookAroundGoal(this));
        goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        targetSelector.add(1, new ActiveTargetGoal<>(this, HostileEntity.class, true));
        targetSelector.add(2, new ActiveTargetGoal<>(this, SlimeEntity.class, true));
    }

    /** Configures combat goals and equipment based on guard type. */
    private void configureGoals() {
        if (goalsConfigured) return;
        goalsConfigured = true;

        if (guardJob == ColonistJob.GUARD_BOW) {
            // Stationary archer — ranged attack, no patrol
            goalSelector.add(1, new ProjectileAttackGoal(this, 1.0, 20, 15.0f));
            this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        } else {
            // Melee patrol — sword + leather armor
            goalSelector.add(1, new MeleeAttackGoal(this, 1.2, true));
            goalSelector.add(3, new GuardPatrolGoal(this));
            this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
            this.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
            this.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
            this.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
            this.equipStack(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
        }
    }

    @Override
    public void shootAt(net.minecraft.entity.LivingEntity target, float pullProgress) {
        net.minecraft.entity.projectile.ArrowEntity arrow =
                new net.minecraft.entity.projectile.ArrowEntity(getEntityWorld(), this,
                        new ItemStack(Items.ARROW), null);
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
    public void    setGuardJob(ColonistJob j)  {
        this.guardJob = j;
        configureGoals();
    }

    public String getGuardName()               { return guardName; }
    public void   setGuardName(String name)    { this.guardName = name; }
    public String getCurrentStatus()           { return currentStatus; }
    public void   setCurrentStatus(String s)   { this.currentStatus = s; }

    /** Assign a random name if none set yet. */
    public void assignRandomName() {
        if (guardName == null || guardName.isEmpty()) {
            guardName = GUARD_NAMES[getEntityWorld().random.nextInt(GUARD_NAMES.length)];
        }
    }

    /** Update display name with guard name + job type. */
    public void updateDisplayName() {
        String label = guardJob.displayName;
        if (guardName != null && !guardName.isEmpty()) {
            label = guardName + " - " + label;
        }
        this.setCustomName(net.minecraft.text.Text.literal(label));
        this.setCustomNameVisible(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (getEntityWorld().isClient()) return;
        // Update status based on state
        if (getTarget() != null && getTarget().isAlive()) {
            currentStatus = "\u00a7c\u2694 Fighting!";
        } else if (isNight()) {
            currentStatus = "\u263E Resting";
        } else {
            currentStatus = "\u2694 Patrolling";
        }
    }

    public boolean isNight() {
        long time = getEntityWorld().getTimeOfDay() % 24000L;
        return time >= 12786;
    }

    @Override
    public void onDeath(net.minecraft.entity.damage.DamageSource source) {
        super.onDeath(source);
        if (getEntityWorld() instanceof net.minecraft.server.world.ServerWorld sw && colonyId != null) {
            cz.wux.colonycraft.data.ColonyManager mgr = cz.wux.colonycraft.data.ColonyManager.get(sw.getServer());
            mgr.getColony(colonyId).ifPresent(colony -> {
                colony.removeColonist(getUuid());
                mgr.markDirty();
            });
            // Unassign from guard tower
            if (homePos != null) {
                var be = sw.getBlockEntity(homePos);
                if (be instanceof cz.wux.colonycraft.blockentity.JobBlockEntity jb
                        && getUuid().equals(jb.getAssignedColonistId())) {
                    jb.unassignColonist();
                }
            }
            String name = getCustomName() != null ? getCustomName().getString() : "Guard";
            for (var player : sw.getPlayers()) {
                if (player.squaredDistanceTo(getX(), getY(), getZ()) < 10000) {
                    player.sendMessage(net.minecraft.text.Text.literal(
                            "\u00a7c\u2620 " + name + " has fallen in battle!"), false);
                }
            }
        }
    }

    @Override
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (colonyId != null) view.putIntArray("ColonyId", Uuids.toIntArray(colonyId));
        view.putString("GuardJob", guardJob.name());
        if (guardName != null) view.putString("GuardName", guardName);
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
        guardName = view.getString("GuardName", null);
        guardJob = ColonistJob.fromString(view.getString("GuardJob", "GUARD_SWORD"));
        configureGoals();
        updateDisplayName();
        int homeX = view.getInt("HomeX", Integer.MIN_VALUE);
        if (homeX != Integer.MIN_VALUE) homePos = new BlockPos(homeX, view.getInt("HomeY", 0), view.getInt("HomeZ", 0));
    }
}