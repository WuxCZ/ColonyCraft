package cz.wux.colonycraft.client.render;

import cz.wux.colonycraft.entity.GuardEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.util.Identifier;

/**
 * Guard colonist renderer — uses guard texture (64×64 skin PNG).
 */
public class GuardEntityRenderer extends BipedEntityRenderer<GuardEntity, BipedEntityRenderState, BipedEntityModel<BipedEntityRenderState>> {

    private static final Identifier TEXTURE =
            Identifier.of("colonycraft", "textures/entity/guard.png");

    public GuardEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public BipedEntityRenderState createRenderState() {
        return new BipedEntityRenderState();
    }

    @Override
    public Identifier getTexture(BipedEntityRenderState state) { return TEXTURE; }
}
