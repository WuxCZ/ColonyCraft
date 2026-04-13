package cz.wux.colonycraft.block;

import cz.wux.colonycraft.blockentity.ResearchTableBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * The Research Table. A Researcher colonist assigned here generates science
 * points that unlock new job types.
 */
public class ResearchTableBlock extends BlockWithEntity {

    public ResearchTableBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ResearchTableBlockEntity(pos, state);
    }
}
