package cz.wux.colonycraft.entity.goal;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * Makes a hostile entity path-find to a target block (the colony banner)
 * and break it. This is the Colony Survival-like "destroy the flag" mechanic.
 * The entity will prioritize attacking players/colonists if they're nearby,
 * but will head for the banner when idle.
 */
public class DestroyBannerGoal extends Goal {

    private final HostileEntity mob;
    private final BlockPos bannerPos;
    private int breakTicks = 0;
    private static final int BREAK_TIME = 100; // 5 seconds to break the banner

    public DestroyBannerGoal(HostileEntity mob, BlockPos bannerPos) {
        this.mob = mob;
        this.bannerPos = bannerPos;
        setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        // Only path to banner when not already targeting a living entity
        return mob.getTarget() == null && bannerPos != null
                && mob.getEntityWorld().getBlockState(bannerPos).getBlock()
                    instanceof cz.wux.colonycraft.block.ColonyBannerBlock;
    }

    @Override
    public void start() {
        mob.getNavigation().startMovingTo(
                bannerPos.getX() + 0.5, bannerPos.getY(), bannerPos.getZ() + 0.5, 1.0);
        breakTicks = 0;
    }

    @Override
    public void tick() {
        if (bannerPos == null) return;

        // If target appeared, stop going to banner
        if (mob.getTarget() != null && mob.getTarget().isAlive()) return;

        double distSq = mob.squaredDistanceTo(
                bannerPos.getX() + 0.5, bannerPos.getY() + 0.5, bannerPos.getZ() + 0.5);

        if (distSq > 4.0) {
            if (!mob.getNavigation().isFollowingPath()) {
                mob.getNavigation().startMovingTo(
                        bannerPos.getX() + 0.5, bannerPos.getY(), bannerPos.getZ() + 0.5, 1.0);
            }
            return;
        }

        // Close enough — start breaking the banner
        breakTicks++;
        if (breakTicks % 10 == 0) {
            mob.swingHand(net.minecraft.util.Hand.MAIN_HAND);
            mob.getLookControl().lookAt(
                    bannerPos.getX() + 0.5, bannerPos.getY() + 0.5, bannerPos.getZ() + 0.5);
        }

        // Visual break progress particles
        World world = mob.getEntityWorld();
        if (breakTicks % 20 == 0 && world instanceof ServerWorld sw) {
            BlockState state = sw.getBlockState(bannerPos);
            sw.addBlockBreakParticles(bannerPos, state);
            sw.playSound(null, bannerPos,
                    net.minecraft.sound.SoundEvents.BLOCK_WOOD_HIT,
                    net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 0.8f);
        }

        if (breakTicks >= BREAK_TIME) {
            // Destroy the banner!
            if (world instanceof ServerWorld sw) {
                sw.breakBlock(bannerPos, true, mob);
                // Notify players
                for (var player : sw.getPlayers()) {
                    if (player.squaredDistanceTo(bannerPos.getX(), bannerPos.getY(), bannerPos.getZ()) < 10000) {
                        player.sendMessage(net.minecraft.text.Text.literal(
                                "\u00a74\u00a7l\u2620 THE COLONY BANNER HAS BEEN DESTROYED!"), false);
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldContinue() {
        if (bannerPos == null) return false;
        // Continue until banner is broken or mob has a combat target
        return mob.getEntityWorld().getBlockState(bannerPos).getBlock()
                instanceof cz.wux.colonycraft.block.ColonyBannerBlock
                && (mob.getTarget() == null || !mob.getTarget().isAlive());
    }

    @Override
    public void stop() {
        breakTicks = 0;
    }
}
