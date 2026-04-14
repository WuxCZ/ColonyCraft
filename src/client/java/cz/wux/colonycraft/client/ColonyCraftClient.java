package cz.wux.colonycraft.client;

import cz.wux.colonycraft.client.render.ColonistEntityRenderer;
import cz.wux.colonycraft.client.render.ColonyMonsterRenderer;
import cz.wux.colonycraft.client.render.GuardEntityRenderer;
import cz.wux.colonycraft.client.screen.ColonyBannerScreen;
import cz.wux.colonycraft.client.screen.ResearchScreen;
import cz.wux.colonycraft.client.screen.StockpileScreen;
import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.registry.ModEntities;
import cz.wux.colonycraft.registry.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

import java.util.Collection;

public class ColonyCraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.COLONIST,       ColonistEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.GUARD,          GuardEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.COLONY_MONSTER, ColonyMonsterRenderer::new);

        HandledScreens.register(ModScreenHandlers.STOCKPILE,     StockpileScreen::new);
        HandledScreens.register(ModScreenHandlers.COLONY_BANNER, ColonyBannerScreen::new);
        HandledScreens.register(ModScreenHandlers.RESEARCH,      ResearchScreen::new);

        // Colony HUD overlay
        HudRenderCallback.EVENT.register((drawContext, deltaTick) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.getServer() == null || mc.player == null) return;
            Collection<ColonyData> colonies = ColonyManager.get(mc.getServer()).getAllColonies();
            if (colonies.isEmpty()) return;
            ColonyData colony = colonies.iterator().next();
            int x = 6, y = 6;
            drawContext.drawText(mc.textRenderer, "\u00a76[Colony] \u00a7f" + colony.getOwnerName(), x, y,      0xFFFFFF, true);
            drawContext.drawText(mc.textRenderer, "\u00a7aFood: \u00a7f"    + colony.getFoodUnits(),            x, y + 11, 0xFFFFFF, true);
            drawContext.drawText(mc.textRenderer, "\u00a7bPop:  \u00a7f"    + colony.getColonistCount() + "/" + colony.getPopulationCap(), x, y + 22, 0xFFFFFF, true);
            drawContext.drawText(mc.textRenderer, "\u00a7eSci:  \u00a7f"    + colony.getSciencePoints(),        x, y + 33, 0xFFFFFF, true);
            drawContext.drawText(mc.textRenderer, "\u00a7cDay:  \u00a7f"    + colony.getDaysSurvived(),         x, y + 44, 0xFFFFFF, true);
        });
    }
}
