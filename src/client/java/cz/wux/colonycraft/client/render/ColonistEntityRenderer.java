package cz.wux.colonycraft.client.render;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.entity.ColonistEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;

public class ColonistEntityRenderer
        extends BipedEntityRenderer<ColonistEntity, ColonistRenderState, BipedEntityModel<ColonistRenderState>> {

    public static final Identifier DEFAULT_TEXTURE =
            Identifier.ofVanilla("textures/entity/player/wide/steve.png");

    private static final Map<ColonistJob, Identifier> JOB_TEXTURES = new EnumMap<>(ColonistJob.class);

    static {
        put(ColonistJob.WOODCUTTER,     "textures/entity/player/wide/kai.png");
        put(ColonistJob.FORESTER,       "textures/entity/player/wide/makena.png");
        put(ColonistJob.MINER,          "textures/entity/player/wide/noor.png");
        put(ColonistJob.FARMER,         "textures/entity/player/slim/alex.png");
        put(ColonistJob.BERRY_FARMER,   "textures/entity/player/slim/sunny.png");
        put(ColonistJob.FISHERMAN,      "textures/entity/player/wide/zuri.png");
        put(ColonistJob.WATER_GATHERER, "textures/entity/player/slim/efe.png");
        put(ColonistJob.COOK,           "textures/entity/player/wide/ari.png");
        put(ColonistJob.SMELTER,        "textures/entity/player/wide/noor.png");
        put(ColonistJob.BLACKSMITH,     "textures/entity/player/wide/kai.png");
        put(ColonistJob.TANNER,         "textures/entity/player/wide/zuri.png");
        put(ColonistJob.TAILOR,         "textures/entity/player/slim/sunny.png");
        put(ColonistJob.FLETCHER,       "textures/entity/player/slim/efe.png");
        put(ColonistJob.STONEMASON,     "textures/entity/player/wide/makena.png");
        put(ColonistJob.COMPOSTER,      "textures/entity/player/wide/ari.png");
        put(ColonistJob.GRINDER,        "textures/entity/player/wide/steve.png");
        put(ColonistJob.POTTER,         "textures/entity/player/wide/ari.png");
        put(ColonistJob.ALCHEMIST,      "textures/entity/player/slim/efe.png");
        put(ColonistJob.GLASSBLOWER,    "textures/entity/player/slim/sunny.png");
        put(ColonistJob.BEEKEEPER,      "textures/entity/player/slim/alex.png");
        put(ColonistJob.CHICKEN_FARMER, "textures/entity/player/wide/makena.png");
        put(ColonistJob.RESEARCHER,     "textures/entity/player/wide/noor.png");
        put(ColonistJob.GUARD_SWORD,    "textures/entity/illager/vindicator.png");
        put(ColonistJob.GUARD_BOW,      "textures/entity/illager/pillager.png");
    }

    private static void put(ColonistJob job, String vanillaPath) {
        JOB_TEXTURES.put(job, Identifier.ofVanilla(vanillaPath));
    }

    public ColonistEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public ColonistRenderState createRenderState() {
        return new ColonistRenderState();
    }

    @Override
    public void updateRenderState(ColonistEntity entity, ColonistRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.jobTexture = JOB_TEXTURES.getOrDefault(entity.getColonistJob(), DEFAULT_TEXTURE);
        state.maxHealth = entity.getMaxHealth();
        state.currentHealth = entity.getHealth();
        state.healthPercent = state.currentHealth / state.maxHealth;
        state.statusText = entity.getCurrentStatus();
        state.jobName = entity.getColonistJob().displayName;
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

        // Render job name on top line
        if (state.jobName != null && !state.jobName.isEmpty()) {
            state.displayName = Text.literal(state.jobName);
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