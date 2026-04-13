package cz.wux.colonycraft.client.render;

import cz.wux.colonycraft.entity.ColonyMonsterEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.util.Identifier;

/**
 * Colony monster renderer — uses a skeleton-based model with a custom texture.
 */
public class ColonyMonsterRenderer extends MobEntityRenderer<ColonyMonsterEntity, SkeletonEntityModel<ColonyMonsterEntity>> {

    // Use vanilla skeleton texture — no custom PNG needed
    private static final Identifier TEXTURE =
            Identifier.of("minecraft", "textures/entity/skeleton/skeleton.png");

    public ColonyMonsterRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new SkeletonEntityModel<>(ctx.getPart(EntityModelLayers.SKELETON)), 0.5f);
    }

    @Override
    public Identifier getTexture(ColonyMonsterEntity entity) { return TEXTURE; }
}
