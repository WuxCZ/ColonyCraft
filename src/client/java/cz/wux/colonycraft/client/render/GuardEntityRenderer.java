package cz.wux.colonycraft.client.render;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.entity.GuardEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.IllagerEntityModel;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

/**
 * Guard colonist renderer - illager model with weapons visible.
 * Uses NEUTRAL state so arms are visible and can hold weapons.
 */
public class GuardEntityRenderer extends MobEntityRenderer<GuardEntity, GuardRenderState, IllagerEntityModel<GuardRenderState>> {

    private static final Identifier SWORD_TEXTURE =
            Identifier.ofVanilla("textures/entity/illager/vindicator.png");
    private static final Identifier BOW_TEXTURE =
            Identifier.ofVanilla("textures/entity/illager/pillager.png");

    public GuardEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new IllagerEntityModel<>(ctx.getPart(EntityModelLayers.PILLAGER)), 0.5f);
        this.addFeature(new HeldItemFeatureRenderer<>(this));
    }

    @Override
    public GuardRenderState createRenderState() {
        return new GuardRenderState();
    }

    @Override
    public void updateRenderState(GuardEntity entity, GuardRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        ArmedEntityRenderState.updateRenderState(entity, state, this.itemModelResolver, tickDelta);
        // Illager fields - NEUTRAL so arms are visible (holding weapons)
        state.illagerMainArm = Arm.RIGHT;
        state.hasVehicle = entity.hasVehicle();
        state.handSwingProgress = entity.getHandSwingProgress(tickDelta);
        if (entity.getTarget() != null) {
            state.illagerState = IllagerEntity.State.ATTACKING;
        } else {
            state.illagerState = IllagerEntity.State.NEUTRAL;
        }
        // Custom fields
        state.maxHealth = entity.getMaxHealth();
        state.currentHealth = entity.getHealth();
        state.healthPercent = state.currentHealth / state.maxHealth;
        state.jobName = entity.getGuardJob().displayName;
        state.guardName = entity.getGuardName() != null ? entity.getGuardName() : "";
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
            String label = state.jobName;
            if (state.guardName != null && !state.guardName.isEmpty()) {
                label = state.guardName + " - " + label;
            }
            state.displayName = Text.literal(label);
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