package cz.wux.colonycraft.entity.goal;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.entity.ColonistEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * Makes a MINER colonist find and mine stone / ores near the miner hut,
 * then deposit the result in the stockpile.
 * Searches downward from the miner hut to simulate underground mining.
 */
public class MineBlocksGoal extends Goal {

    private static final int SEARCH_RADIUS = 12;
    private static final int MINE_TICKS    = 50; // ~2.5 s per block
    private static final int COOLDOWN      = 40;

    private final ColonistEntity colonist;
    private BlockPos targetBlock;
    private int mineTick;

    public MineBlocksGoal(ColonistEntity colonist) {
        this.colonist = colonist;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return colonist.getColonistJob() == ColonistJob.MINER
                && !colonist.isWorkCoolingDown()
                && !colonist.isHungry()
                && colonist.getStockpile().isPresent();
    }

    @Override
    public void start() {
        targetBlock = findMineTarget();
        mineTick    = 0;
        if (targetBlock != null) moveTo(targetBlock);
    }

    @Override
    public void tick() {
        if (targetBlock == null) {
            targetBlock = findMineTarget();
            if (targetBlock != null) { moveTo(targetBlock); mineTick = 0; }
            return;
        }

        World world = colonist.getEntityWorld();
        if (!isMineableBlock(world.getBlockState(targetBlock))) {
            targetBlock = findMineTarget();
            mineTick    = 0;
            return;
        }

        double distSq = colonist.squaredDistanceTo(
                targetBlock.getX() + 0.5, targetBlock.getY() + 0.5, targetBlock.getZ() + 0.5);

        if (distSq > 9.0) {
            if (!colonist.getNavigation().isFollowingPath()) moveTo(targetBlock);
            return;
        }

        mineTick++;
        if (mineTick % 5 == 0) {
            colonist.swingHand(Hand.MAIN_HAND);
            colonist.getLookControl().lookAt(
                    targetBlock.getX() + 0.5, targetBlock.getY() + 0.5, targetBlock.getZ() + 0.5);
        }

        if (mineTick >= MINE_TICKS) {
            BlockState state = world.getBlockState(targetBlock);
            ItemStack drop   = getDropFor(state);
            world.breakBlock(targetBlock, false, colonist);
            if (!drop.isEmpty()) {
                colonist.getStockpile().ifPresent(s -> s.insertItem(drop));
            }
            mineTick    = 0;
            targetBlock = null;
            colonist.startWorkCooldown(COOLDOWN);
        }
    }

    @Override
    public boolean shouldContinue() {
        return !colonist.isWorkCoolingDown() && !colonist.isHungry();
    }

    @Override
    public void stop() {
        targetBlock = null;
        mineTick    = 0;
        colonist.getNavigation().stop();
    }

    private BlockPos findMineTarget() {
        BlockPos center = colonist.getJobBlockPos();
        if (center == null) center = colonist.getHomePos();
        if (center == null) return null;

        World world  = colonist.getEntityWorld();
        int r        = SEARCH_RADIUS;
        // Search downward preferentially (miners go underground)
        BlockPos best   = null;
        double bestD    = Double.MAX_VALUE;
        // Prioritise ores, fallback to stone
        // First pass: ores
        for (BlockPos p : BlockPos.iterate(
                center.add(-r, -16, -r), center.add(r, 2, r))) {
            BlockState s = world.getBlockState(p);
            if (s.isIn(BlockTags.COAL_ORES) || s.isIn(BlockTags.IRON_ORES)
                    || s.isIn(BlockTags.STONE_ORE_REPLACEABLES)) {
                if (!s.isIn(BlockTags.STONE_ORE_REPLACEABLES)) { // actual ore
                    double d = colonist.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                    if (d < bestD) { bestD = d; best = p.toImmutable(); }
                }
            }
        }
        if (best != null) return best;

        // Second pass: plain stone / deepslate
        for (BlockPos p : BlockPos.iterate(
                center.add(-r, -16, -r), center.add(r, 2, r))) {
            BlockState s = world.getBlockState(p);
            if (s.isOf(Blocks.STONE) || s.isOf(Blocks.DEEPSLATE) || s.isOf(Blocks.COBBLESTONE)) {
                double d = colonist.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                if (d < bestD) { bestD = d; best = p.toImmutable(); }
            }
        }
        return best;
    }

    private boolean isMineableBlock(BlockState state) {
        return state.isOf(Blocks.STONE) || state.isOf(Blocks.DEEPSLATE)
                || state.isOf(Blocks.COBBLESTONE)
                || state.isIn(BlockTags.COAL_ORES)
                || state.isIn(BlockTags.IRON_ORES);
    }

    private ItemStack getDropFor(BlockState state) {
        if (state.isIn(BlockTags.COAL_ORES))  return new ItemStack(Items.COAL, 1);
        if (state.isIn(BlockTags.IRON_ORES))  return new ItemStack(Items.RAW_IRON, 1);
        if (state.isOf(Blocks.STONE))         return new ItemStack(Items.COBBLESTONE, 1);
        if (state.isOf(Blocks.DEEPSLATE))     return new ItemStack(Items.COBBLED_DEEPSLATE, 1);
        if (state.isOf(Blocks.COBBLESTONE))   return new ItemStack(Items.COBBLESTONE, 1);
        return ItemStack.EMPTY;
    }

    private void moveTo(BlockPos pos) {
        colonist.getNavigation().startMovingTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.9);
    }
}
