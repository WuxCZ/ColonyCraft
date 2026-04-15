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
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

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
                    // Force-load 3x3 chunk area around the colony banner
                    forceLoadChunks(sw, pos, true);
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
        if (!world.isClient() && world instanceof ServerWorld sw) {
            ColonyManager mgr = ColonyManager.get(sw);
            mgr.getColonyAtBanner(pos).ifPresent(colony -> {
                // Remove all colonists belonging to this colony
                for (UUID uuid : new java.util.ArrayList<>(colony.getColonistUuids())) {
                    net.minecraft.entity.Entity entity = sw.getEntity(uuid);
                    if (entity != null) entity.discard();
                }
                mgr.removeColony(colony.getColonyId());
                player.sendMessage(
                        net.minecraft.text.Text.literal("\u00a7c\u00a7l\u26A0 Colony destroyed! All colonists dismissed."),
                        false);
            });
            // Unload forced chunks
            forceLoadChunks(sw, pos, false);
        }
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

    /** Force-load or unload a 3x3 chunk area centered on the colony banner. */
    private static void forceLoadChunks(ServerWorld world, BlockPos bannerPos, boolean load) {
        ChunkPos cp = new ChunkPos(bannerPos);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.setChunkForced(cp.x + dx, cp.z + dz, load);
            }
        }
    }
}