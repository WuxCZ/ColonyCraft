package cz.wux.colonycraft.client.render;

import cz.wux.colonycraft.entity.GuardEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

/**
 * Guard colonist renderer — uses guard texture (64×64 skin PNG).
 */
public class GuardEntityRenderer extends BipedEntityRenderer<GuardEntity, BipedEntityModel<GuardEntity>> {

    private static final Identifier TEXTURE =
            Identifier.of("colonycraft", "textures/entity/guard.png");

    public GuardEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(GuardEntity entity) { return TEXTURE; }
}
