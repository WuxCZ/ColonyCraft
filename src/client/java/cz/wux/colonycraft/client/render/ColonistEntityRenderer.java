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
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;

public class ColonistEntityRenderer
        extends MobEntityRenderer<ColonistEntity, ColonistRenderState, IllagerEntityModel<ColonistRenderState>> {

    public static final Identifier DEFAULT_TEXTURE =
            Identifier.of("colonycraft", "textures/entity/colonist.png");

    private static final Map<ColonistJob, Identifier> JOB_TEXTURES = new EnumMap<>(ColonistJob.class);

    static {
        // Each job has its own custom-tinted texture
        put(ColonistJob.UNEMPLOYED,      "colonist");
        put(ColonistJob.WOODCUTTER,      "woodcutter");
        put(ColonistJob.FORESTER,        "forester");
        put(ColonistJob.MINER,           "miner");
        put(ColonistJob.FARMER,          "farmer");
        put(ColonistJob.BERRY_FARMER,    "berry_farmer");
        put(ColonistJob.FISHERMAN,       "fisherman");
        put(ColonistJob.WATER_GATHERER,  "water_gatherer");
        put(ColonistJob.COOK,            "cook");
        put(ColonistJob.SMELTER,         "smelter");
        put(ColonistJob.BLACKSMITH,      "blacksmith");
        put(ColonistJob.TANNER,          "tanner");
        put(ColonistJob.TAILOR,          "tailor");
        put(ColonistJob.FLETCHER,        "fletcher");
        put(ColonistJob.STONEMASON,      "stonemason");
        put(ColonistJob.COMPOSTER,       "composter");
        put(ColonistJob.GRINDER,         "grinder");
        put(ColonistJob.POTTER,          "potter");
        put(ColonistJob.ALCHEMIST,       "alchemist");
        put(ColonistJob.GLASSBLOWER,     "glassblower");
        put(ColonistJob.BEEKEEPER,       "beekeeper");
        put(ColonistJob.CHICKEN_FARMER,  "chicken_farmer");
        put(ColonistJob.BUILDER,         "builder");
        put(ColonistJob.DIGGER,          "digger");
        put(ColonistJob.SHEPHERD,        "shepherd");
        put(ColonistJob.COW_HERDER,      "cow_herder");
        put(ColonistJob.RESEARCHER,      "researcher");
        put(ColonistJob.GUARD_SWORD,     "guard_sword");
        put(ColonistJob.GUARD_BOW,       "guard_bow");
    }

    private static void put(ColonistJob job, String texName) {
        JOB_TEXTURES.put(job, Identifier.of("colonycraft", "textures/entity/" + texName + ".png"));
    }

    public ColonistEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new IllagerEntityModel<>(ctx.getPart(EntityModelLayers.PILLAGER)), 0.5f);
        // No HeldItemFeatureRenderer — colonists don't hold visible items
    }

    @Override
    public ColonistRenderState createRenderState() {
        return new ColonistRenderState();
    }

    @Override
    public void updateRenderState(ColonistEntity entity, ColonistRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        // Illager-specific fields
        state.illagerState = IllagerEntity.State.NEUTRAL; // arms visible and natural
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
        state.speechBubble = entity.getSpeechBubble();
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

        // Render speech bubble above everything (with distinctive golden color)
        if (state.speechBubble != null && !state.speechBubble.isEmpty()) {
            matrices.push();
            matrices.translate(0, 0.25f, 0);
            state.displayName = Text.literal("§e§o◆ " + state.speechBubble + " ◆"
            ).styled(s -> s.withColor(0xFFFFDD55));
            super.renderLabelIfPresent(state, matrices, queue, cameraState);
            matrices.pop();
        }
    }
}