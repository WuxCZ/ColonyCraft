package cz.wux.colonycraft.client.render;

import cz.wux.colonycraft.entity.ColonistEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.util.Identifier;

/**
 * Renders colonists using the biped (humanoid) model with a custom texture.
 * The texture at {@code assets/colonycraft/textures/entity/colonist.png} is a
 * 64×64 skin-format PNG.
 */
public class ColonistEntityRenderer extends BipedEntityRenderer<ColonistEntity, BipedEntityRenderState, BipedEntityModel<BipedEntityRenderState>> {

    private static final Identifier TEXTURE =
            Identifier.of("colonycraft", "textures/entity/colonist.png");

    public ColonistEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER)),
                0.5f);
    }

    @Override
    public BipedEntityRenderState createRenderState() {
        return new BipedEntityRenderState();
    }

    @Override
    public Identifier getTexture(BipedEntityRenderState state) {
        return TEXTURE;
    }
}
