package cz.wux.colonycraft.client.render;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.entity.ColonistEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;

public class ColonistEntityRenderer
        extends BipedEntityRenderer<ColonistEntity, ColonistRenderState, BipedEntityModel<ColonistRenderState>> {

    public static final Identifier DEFAULT_TEXTURE =
            Identifier.ofVanilla("textures/entity/player/wide/steve.png");

    private static final Map<ColonistJob, Identifier> JOB_TEXTURES = new EnumMap<>(ColonistJob.class);

    static {
        put(ColonistJob.WOODCUTTER,     "textures/entity/zombie/zombie.png");
        put(ColonistJob.FORESTER,       "textures/entity/illager/pillager.png");
        put(ColonistJob.MINER,          "textures/entity/zombie/husk.png");
        put(ColonistJob.FARMER,         "textures/entity/player/slim/alex.png");
        put(ColonistJob.BERRY_FARMER,   "textures/entity/player/slim/alex.png");
        put(ColonistJob.FISHERMAN,      "textures/entity/player/wide/steve.png");
        put(ColonistJob.WATER_GATHERER, "textures/entity/player/wide/steve.png");
        put(ColonistJob.COOK,           "textures/entity/illager/vindicator.png");
        put(ColonistJob.SMELTER,        "textures/entity/zombie/zombie_villager.png");
        put(ColonistJob.BLACKSMITH,     "textures/entity/illager/vindicator.png");
        put(ColonistJob.TANNER,         "textures/entity/zombie/drowned.png");
        put(ColonistJob.TAILOR,         "textures/entity/player/slim/alex.png");
        put(ColonistJob.FLETCHER,       "textures/entity/illager/pillager.png");
        put(ColonistJob.STONEMASON,     "textures/entity/zombie/husk.png");
        put(ColonistJob.COMPOSTER,      "textures/entity/illager/pillager.png");
        put(ColonistJob.GRINDER,        "textures/entity/zombie/zombie.png");
        put(ColonistJob.POTTER,         "textures/entity/player/wide/steve.png");
        put(ColonistJob.ALCHEMIST,      "textures/entity/illager/evoker.png");
        put(ColonistJob.GLASSBLOWER,    "textures/entity/player/wide/steve.png");
        put(ColonistJob.BEEKEEPER,      "textures/entity/player/slim/alex.png");
        put(ColonistJob.CHICKEN_FARMER, "textures/entity/illager/pillager.png");
        put(ColonistJob.RESEARCHER,     "textures/entity/illager/evoker.png");
        put(ColonistJob.GUARD_BOW,      "textures/entity/illager/vindicator.png");
        put(ColonistJob.GUARD_CROSSBOW, "textures/entity/illager/vindicator.png");
        put(ColonistJob.GUARD_MUSKET,   "textures/entity/illager/vindicator.png");
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
    }

    @Override
    public Identifier getTexture(ColonistRenderState state) {
        return state.jobTexture;
    }
}