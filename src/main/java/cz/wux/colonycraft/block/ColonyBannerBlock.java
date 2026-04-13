package cz.wux.colonycraft.block;

import cz.wux.colonycraft.blockentity.ColonyBannerBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * The Colony Banner is the player's colony anchor. Right-clicking it opens
 * the colony management GUI. Placing it founds a new colony.
 */
public class ColonyBannerBlock extends BlockWithEntity {

    public ColonyBannerBlock(Settings settings) {
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
        return new ColonyBannerBlockEntity(pos, state);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state,
                            @Nullable net.minecraft.entity.LivingEntity placer,
                            net.minecraft.item.ItemStack itemStack) {
        if (!world.isClient() && placer instanceof ServerPlayerEntity player) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof ColonyBannerBlockEntity banner) {
                banner.initColony(player);
                player.sendMessage(
                        net.minecraft.text.Text.translatable("message.colonycraft.colony_founded",
                                player.getName().getString()), false);
            }
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof ColonyBannerBlockEntity banner) {
            player.openHandledScreen(banner);
        }
        return ActionResult.CONSUME;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // When the banner is broken, the colony data persists so colonists don't vanish
        // (players must explicitly disband via the GUI)
        return super.onBreak(world, pos, state, player);
    }
}
