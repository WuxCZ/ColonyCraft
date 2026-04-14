package cz.wux.colonycraft.block;

import cz.wux.colonycraft.blockentity.ColonyBannerBlockEntity;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.entity.ColonistEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ColonyBannerBlock extends BlockWithEntity {

    // Pole (7-9, 0-16, 7-9) + cross bar (1-15, 15-16, 7-9) + banner cloth (1-15, 6-16, 7.5-8.5)
    private static final VoxelShape POLE = Block.createCuboidShape(7, 0, 7, 9, 16, 9);
    private static final VoxelShape CROSS_BAR = Block.createCuboidShape(1, 15, 7, 15, 16, 9);
    private static final VoxelShape BANNER_LEFT = Block.createCuboidShape(1, 6, 7.5, 7, 16, 8.5);
    private static final VoxelShape BANNER_RIGHT = Block.createCuboidShape(9, 6, 7.5, 15, 16, 8.5);
    private static final VoxelShape SHAPE = VoxelShapes.union(POLE, CROSS_BAR, BANNER_LEFT, BANNER_RIGHT);

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
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
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
                giveStarterKit(player);
                if (world instanceof ServerWorld sw) {
                    ColonyManager mgr = ColonyManager.get(sw);
                    mgr.getColonyAtBanner(pos).ifPresent(colony -> {
                        colony.addFood(60);
                        mgr.markDirty();
                        ColonistEntity.spawnForColony(sw, colony, pos, mgr);
                        ColonistEntity.spawnForColony(sw, colony, pos, mgr);
                    });
                }
            }
        }
    }

    private static void giveStarterKit(ServerPlayerEntity player) {
        player.getInventory().insertStack(new ItemStack(Items.BREAD, 16));
        player.getInventory().insertStack(new ItemStack(Items.WHEAT_SEEDS, 16));
        player.getInventory().insertStack(new ItemStack(Items.OAK_SAPLING, 8));
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient() && player instanceof ServerPlayerEntity sp) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof NamedScreenHandlerFactory factory) {
                sp.openHandledScreen(factory);
            }
        }
        return ActionResult.SUCCESS;
    }
}