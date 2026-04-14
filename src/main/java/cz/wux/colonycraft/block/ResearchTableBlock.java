package cz.wux.colonycraft.block;

import com.mojang.serialization.MapCodec;
import cz.wux.colonycraft.blockentity.ResearchTableBlockEntity;
import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.registry.ModScreenHandlers;
import cz.wux.colonycraft.screen.ResearchScreenHandler;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ResearchTableBlock extends BlockWithEntity {

    public ResearchTableBlock(Settings settings) {
        super(settings);
    }

    @Override
    public MapCodec<? extends BlockWithEntity> getCodec() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ResearchTableBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (!(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;

        ServerWorld sw = (ServerWorld) world;
        ColonyManager mgr = ColonyManager.get(sw);
        Optional<ColonyData> colonyOpt = mgr.getAllColonies().stream().findFirst();
        if (colonyOpt.isEmpty()) {
            sp.sendMessage(Text.literal("\u00a7cNo colony found. Found a colony first."), true);
            return ActionResult.FAIL;
        }

        ColonyData colony = colonyOpt.get();
        PropertyDelegate delegate = new ArrayPropertyDelegate(2);
        delegate.set(0, colony.getSciencePoints());
        delegate.set(1, colony.getDaysSurvived());

        sp.openHandledScreen(new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() { return Text.literal("Research Tree"); }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity p) {
                return new ResearchScreenHandler(syncId, inv, colony.getColonyId(), delegate);
            }
        });
        return ActionResult.CONSUME;
    }
}
