package cz.wux.colonycraft.block;

import com.mojang.serialization.MapCodec;
import cz.wux.colonycraft.blockentity.StockpileBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Stockpile block — the colony's shared chest.
 * All colonists deposit and withdraw items here.
 */
public class StockpileBlock extends BlockWithEntity {

    public StockpileBlock(Settings settings) {
        super(settings);
    }

    @Override
    public MapCodec<? extends BlockWithEntity> getCodec() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StockpileBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;

        NamedScreenHandlerFactory factory = state.createScreenHandlerFactory(world, pos);
        if (factory != null) {
            player.openHandledScreen(factory);
        }
        return ActionResult.CONSUME;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!world.isClient() && be instanceof StockpileBlockEntity stockpile) {
            // Drop all contents
            for (int i = 0; i < stockpile.size(); i++) {
                net.minecraft.item.ItemStack stack = stockpile.getStack(i);
                if (!stack.isEmpty()) {
                    net.minecraft.util.ItemScatterer.spawn(world,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                }
            }
            stockpile.clear();
        }
        return super.onBreak(world, pos, state, player);
    }
}
