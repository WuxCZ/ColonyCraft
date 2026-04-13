package cz.wux.colonycraft.client;

import cz.wux.colonycraft.client.render.ColonistEntityRenderer;
import cz.wux.colonycraft.client.render.ColonyMonsterRenderer;
import cz.wux.colonycraft.client.render.GuardEntityRenderer;
import cz.wux.colonycraft.client.screen.ColonyBannerScreen;
import cz.wux.colonycraft.client.screen.StockpileScreen;
import cz.wux.colonycraft.registry.ModEntities;
import cz.wux.colonycraft.registry.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

/**
 * Client-side initializer — registers entity renderers and GUI screens.
 */
public class ColonyCraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Entity renderers
        EntityRendererRegistry.register(ModEntities.COLONIST,   ColonistEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.GUARD,      GuardEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.COLONY_MONSTER, ColonyMonsterRenderer::new);

        // GUI screens
        HandledScreens.register(ModScreenHandlers.STOCKPILE,     StockpileScreen::new);
        HandledScreens.register(ModScreenHandlers.COLONY_BANNER, ColonyBannerScreen::new);
    }
}
