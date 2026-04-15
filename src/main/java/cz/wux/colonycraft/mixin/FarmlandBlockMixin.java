package cz.wux.colonycraft.mixin;

import cz.wux.colonycraft.entity.ColonistEntity;
import cz.wux.colonycraft.entity.GuardEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents colonists and guards from trampling farmland when walking on it.
 */
@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin {

    @Inject(method = "onLandedUpon", at = @At("HEAD"), cancellable = true)
    private void colonycraft$preventColonistTrampling(World world, BlockState state, BlockPos pos,
                                                       Entity entity, double fallDistance, CallbackInfo ci) {
        if (entity instanceof ColonistEntity || entity instanceof GuardEntity) {
            ci.cancel();
        }
    }
}
