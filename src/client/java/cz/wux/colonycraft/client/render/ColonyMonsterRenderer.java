package cz.wux.colonycraft.client.render;

import cz.wux.colonycraft.entity.ColonyMonsterEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Colony monster renderer — shows health bar above head.
 */
public class ColonyMonsterRenderer extends BipedEntityRenderer<ColonyMonsterEntity, MonsterRenderState, BipedEntityModel<MonsterRenderState>> {

    private static final Identifier TEXTURE =
            Identifier.of("minecraft", "textures/entity/skeleton/skeleton.png");

    public ColonyMonsterRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.SKELETON)), 0.5f);
    }

    @Override
    public MonsterRenderState createRenderState() {
        return new MonsterRenderState();
    }

    @Override
    public void updateRenderState(ColonyMonsterEntity entity, MonsterRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.maxHealth = entity.getMaxHealth();
        state.currentHealth = entity.getHealth();
        state.healthPercent = state.currentHealth / state.maxHealth;
        if (entity.getTarget() != null) {
            state.statusText = "\u2620 Attacking";
        } else {
            state.statusText = "\u2620 Raiding";
        }
    }

    @Override
    public Identifier getTexture(MonsterRenderState state) { return TEXTURE; }

    @Override
    protected boolean hasLabel(ColonyMonsterEntity entity, double squaredDistanceToCamera) {
        return squaredDistanceToCamera < 256.0;
    }

    @Override
    protected void renderLabelIfPresent(MonsterRenderState state, MatrixStack matrices,
                                        OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        int totalHearts = 10;
        int filledHearts = Math.max(0, Math.round(state.healthPercent * totalHearts));
        StringBuilder healthBar = new StringBuilder();
        for (int i = 0; i < filledHearts; i++) healthBar.append('\u2764');
        for (int i = filledHearts; i < totalHearts; i++) healthBar.append('\u2661');

        int healthColor;
        if (state.healthPercent > 0.5f) {
            healthColor = 0xFF55FF55;
        } else if (state.healthPercent > 0.25f) {
            healthColor = 0xFFFFFF55;
        } else {
            healthColor = 0xFFFF5555;
        }

        // Render health bar
        state.displayName = Text.literal(healthBar.toString()).styled(s -> s.withColor(healthColor));
        super.renderLabelIfPresent(state, matrices, queue, cameraState);

        // Render status text below
        if (state.statusText != null && !state.statusText.isEmpty()) {
            matrices.push();
            matrices.translate(0, -0.15f, 0);
            state.displayName = Text.literal(state.statusText).styled(s -> s.withColor(0xFFFF5555));
            super.renderLabelIfPresent(state, matrices, queue, cameraState);
            matrices.pop();
        }
    }
}
