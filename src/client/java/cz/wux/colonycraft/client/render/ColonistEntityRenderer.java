package cz.wux.colonycraft.client.render;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.entity.ColonistEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.IllagerEntityModel;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;

public class ColonistEntityRenderer
        extends MobEntityRenderer<ColonistEntity, ColonistRenderState, IllagerEntityModel<ColonistRenderState>> {

    public static final Identifier DEFAULT_TEXTURE =
            Identifier.ofVanilla("textures/entity/illager/pillager.png");

    private static final Map<ColonistJob, Identifier> JOB_TEXTURES = new EnumMap<>(ColonistJob.class);

    static {
        // Outdoor / resource gathering - pillager (gray-brown outdoor outfit)
        put(ColonistJob.UNEMPLOYED,      "textures/entity/illager/pillager.png");
        put(ColonistJob.WOODCUTTER,      "textures/entity/illager/vindicator.png");
        put(ColonistJob.FORESTER,        "textures/entity/illager/pillager.png");
        put(ColonistJob.MINER,           "textures/entity/illager/vindicator.png");
        put(ColonistJob.FARMER,          "textures/entity/illager/pillager.png");
        put(ColonistJob.BERRY_FARMER,    "textures/entity/illager/evoker.png");
        put(ColonistJob.FISHERMAN,       "textures/entity/illager/illusioner.png");
        put(ColonistJob.WATER_GATHERER,  "textures/entity/illager/pillager.png");
        // Processing / crafting - vindicator/evoker (indoor craft look)
        put(ColonistJob.COOK,            "textures/entity/illager/evoker.png");
        put(ColonistJob.SMELTER,         "textures/entity/illager/vindicator.png");
        put(ColonistJob.BLACKSMITH,      "textures/entity/illager/vindicator.png");
        put(ColonistJob.TANNER,          "textures/entity/illager/evoker.png");
        put(ColonistJob.TAILOR,          "textures/entity/illager/illusioner.png");
        put(ColonistJob.FLETCHER,        "textures/entity/illager/vindicator.png");
        put(ColonistJob.STONEMASON,      "textures/entity/illager/vindicator.png");
        put(ColonistJob.COMPOSTER,       "textures/entity/illager/pillager.png");
        put(ColonistJob.GRINDER,         "textures/entity/illager/evoker.png");
        put(ColonistJob.POTTER,          "textures/entity/illager/illusioner.png");
        put(ColonistJob.ALCHEMIST,       "textures/entity/illager/evoker.png");
        put(ColonistJob.GLASSBLOWER,     "textures/entity/illager/illusioner.png");
        put(ColonistJob.BEEKEEPER,       "textures/entity/illager/pillager.png");
        put(ColonistJob.CHICKEN_FARMER,  "textures/entity/illager/evoker.png");
        // Knowledge - illusioner (blue scholarly robe)
        put(ColonistJob.RESEARCHER,      "textures/entity/illager/illusioner.png");
        // Guards - keep vindicator/pillager
        put(ColonistJob.GUARD_SWORD,     "textures/entity/illager/vindicator.png");
        put(ColonistJob.GUARD_BOW,       "textures/entity/illager/pillager.png");
    }

    private static void put(ColonistJob job, String vanillaPath) {
        JOB_TEXTURES.put(job, Identifier.ofVanilla(vanillaPath));
    }

    public ColonistEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new IllagerEntityModel<>(ctx.getPart(EntityModelLayers.PILLAGER)), 0.5f);
    }

    @Override
    public ColonistRenderState createRenderState() {
        return new ColonistRenderState();
    }

    @Override
    public void updateRenderState(ColonistEntity entity, ColonistRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        ArmedEntityRenderState.updateRenderState(entity, state, this.itemModelResolver, tickDelta);
        // Illager-specific fields
        state.illagerState = IllagerEntity.State.CROSSED; // arms folded - villager-like pose
        state.illagerMainArm = Arm.RIGHT;
        state.hasVehicle = entity.hasVehicle();
        state.handSwingProgress = entity.getHandSwingProgress(tickDelta);
        // Custom fields
        state.jobTexture = JOB_TEXTURES.getOrDefault(entity.getColonistJob(), DEFAULT_TEXTURE);
        state.maxHealth = entity.getMaxHealth();
        state.currentHealth = entity.getHealth();
        state.healthPercent = state.currentHealth / state.maxHealth;
        state.statusText = entity.getCurrentStatus();
        state.jobName = entity.getColonistJob().displayName;
        state.colonistName = entity.getColonistName() != null ? entity.getColonistName() : "";
    }

    @Override
    public Identifier getTexture(ColonistRenderState state) {
        return state.jobTexture;
    }

    @Override
    protected boolean hasLabel(ColonistEntity entity, double squaredDistanceToCamera) {
        return squaredDistanceToCamera < 256.0;
    }

    @Override
    protected void renderLabelIfPresent(ColonistRenderState state, MatrixStack matrices,
                                        OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        // Build health bar as text: green/red hearts
        int totalHearts = 10;
        int filledHearts = Math.max(0, Math.round(state.healthPercent * totalHearts));
        StringBuilder healthBar = new StringBuilder();
        for (int i = 0; i < filledHearts; i++) healthBar.append('\u2764');
        for (int i = filledHearts; i < totalHearts; i++) healthBar.append('\u2661');

        // Determine health bar color
        int healthColor;
        if (state.healthPercent > 0.5f) {
            healthColor = 0xFF55FF55; // green
        } else if (state.healthPercent > 0.25f) {
            healthColor = 0xFFFFFF55; // yellow
        } else {
            healthColor = 0xFFFF5555; // red
        }

        // Render colonist name + job name on top line
        if (state.jobName != null && !state.jobName.isEmpty()) {
            String label = state.jobName;
            if (state.colonistName != null && !state.colonistName.isEmpty()) {
                label = state.colonistName + " - " + label;
            }
            state.displayName = Text.literal(label);
        }
        super.renderLabelIfPresent(state, matrices, queue, cameraState);

        // Render health bar below job name
        matrices.push();
        matrices.translate(0, -0.15f, 0);
        state.displayName = Text.literal(healthBar.toString()).styled(s -> s.withColor(healthColor));
        super.renderLabelIfPresent(state, matrices, queue, cameraState);
        matrices.pop();

        // Render status text below health bar
        if (state.statusText != null && !state.statusText.isEmpty()) {
            matrices.push();
            matrices.translate(0, -0.3f, 0);
            state.displayName = Text.literal(state.statusText).styled(s -> s.withColor(0xFFAAAAAA));
            super.renderLabelIfPresent(state, matrices, queue, cameraState);
            matrices.pop();
        }
    }
}