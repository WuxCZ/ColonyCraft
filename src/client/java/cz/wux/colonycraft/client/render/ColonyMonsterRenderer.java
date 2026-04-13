package cz.wux.colonycraft.client.render;

import cz.wux.colonycraft.entity.ColonyMonsterEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.util.Identifier;

/**
 * Colony monster renderer — uses a skeleton-based model with a custom texture.
 */
public class ColonyMonsterRenderer extends BipedEntityRenderer<ColonyMonsterEntity, BipedEntityRenderState, BipedEntityModel<BipedEntityRenderState>> {

    // Use vanilla skeleton texture — no custom PNG needed
    private static final Identifier TEXTURE =
            Identifier.of("minecraft", "textures/entity/skeleton/skeleton.png");

    public ColonyMonsterRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.SKELETON)), 0.5f);
    }

    @Override
    public BipedEntityRenderState createRenderState() {
        return new BipedEntityRenderState();
    }

    @Override
    public Identifier getTexture(BipedEntityRenderState state) { return TEXTURE; }
}
