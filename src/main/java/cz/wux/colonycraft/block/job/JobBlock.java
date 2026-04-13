package cz.wux.colonycraft.block.job;

import cz.wux.colonycraft.blockentity.JobBlockEntity;
import cz.wux.colonycraft.data.ColonistJob;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract base for all Colony Survival job-workstation blocks.
 *
 * <p>Each subclass declares its {@link ColonistJob}. A colonist with that job
 * will path-find to this block and perform their work here.</p>
 *
 * <p>Right-clicking shows the assigned colonist's name (or "Unassigned").</p>
 */
public abstract class JobBlock extends BlockWithEntity {

    protected JobBlock() {
        super(AbstractBlock.Settings.create()
                .mapColor(MapColor.OAK_TAN)
                .strength(2.0f, 6.0f)
                .sounds(BlockSoundGroup.WOOD));
    }

    protected JobBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    /** Which colonist job this workstation supports. */
    public abstract ColonistJob getJob();

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        JobBlockEntity be = new JobBlockEntity(pos, state);
        be.setJob(getJob());
        return be;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof JobBlockEntity job) {
            String info = job.hasAssignedColonist()
                    ? "§aAssigned colonist: " + job.getAssignedColonistId()
                    : "§7Unassigned — a colonist will claim this automatically.";
            player.sendMessage(Text.of("[" + getJob().displayName + "] " + info), false);
        }
        return ActionResult.CONSUME;
    }
}
