package cz.wux.colonycraft.client.render;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.entity.GuardEntity;
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
 * Guard colonist renderer — picks texture based on guard type (sword/bow),
 * shows health bar + status.
 */
public class GuardEntityRenderer extends BipedEntityRenderer<GuardEntity, GuardRenderState, BipedEntityModel<GuardRenderState>> {

    private static final Identifier SWORD_TEXTURE =
            Identifier.ofVanilla("textures/entity/illager/vindicator.png");
    private static final Identifier BOW_TEXTURE =
            Identifier.ofVanilla("textures/entity/illager/pillager.png");

    public GuardEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public GuardRenderState createRenderState() {
        return new GuardRenderState();
    }

    @Override
    public void updateRenderState(GuardEntity entity, GuardRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.maxHealth = entity.getMaxHealth();
        state.currentHealth = entity.getHealth();
        state.healthPercent = state.currentHealth / state.maxHealth;
        state.jobName = entity.getGuardJob().displayName;
        state.isBowGuard = entity.getGuardJob() == ColonistJob.GUARD_BOW;
        if (entity.getTarget() != null) {
            state.statusText = "\u2694 Fighting!";
        } else if (state.isBowGuard) {
            state.statusText = "\u263A Watching";
        } else {
            state.statusText = "\u263A Patrolling";
        }
    }

    @Override
    public Identifier getTexture(GuardRenderState state) {
        return state.isBowGuard ? BOW_TEXTURE : SWORD_TEXTURE;
    }

    @Override
    protected boolean hasLabel(GuardEntity entity, double squaredDistanceToCamera) {
        return squaredDistanceToCamera < 256.0;
    }

    @Override
    protected void renderLabelIfPresent(GuardRenderState state, MatrixStack matrices,
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

        if (state.jobName != null && !state.jobName.isEmpty()) {
            state.displayName = Text.literal(state.jobName);
        }
        super.renderLabelIfPresent(state, matrices, queue, cameraState);

        matrices.push();
        matrices.translate(0, -0.15f, 0);
        state.displayName = Text.literal(healthBar.toString()).styled(s -> s.withColor(healthColor));
        super.renderLabelIfPresent(state, matrices, queue, cameraState);
        matrices.pop();

        if (state.statusText != null && !state.statusText.isEmpty()) {
            matrices.push();
            matrices.translate(0, -0.3f, 0);
            state.displayName = Text.literal(state.statusText).styled(s -> s.withColor(0xFFAAAAAA));
            super.renderLabelIfPresent(state, matrices, queue, cameraState);
            matrices.pop();
        }
    }
}
