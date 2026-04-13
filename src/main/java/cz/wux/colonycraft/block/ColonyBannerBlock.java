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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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

                // Give starting resources to player (starter kit)
                giveStarterKit(player);

                // Spawn first 2 colonists immediately so the colony feels alive
                if (world instanceof ServerWorld sw) {
                    ColonyManager mgr = ColonyManager.get(sw);
                    mgr.getColonyAtBanner(pos).ifPresent(colony -> {
                        // Give colony 60 starting food units
                        colony.addFood(60);
                        mgr.markDirty();
                        // Spawn 2 colonists
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
        // When the banner is broken, the colony data persists so colonists don't vanish
        // (players must explicitly disband via the GUI)
        return super.onBreak(world, pos, state, player);
    }
}
