package cz.wux.colonycraft.client;

import cz.wux.colonycraft.client.render.AreaWandRenderer;
import cz.wux.colonycraft.client.render.ColonistEntityRenderer;
import cz.wux.colonycraft.client.render.ColonyBorderRenderer;
import cz.wux.colonycraft.client.render.ColonyMonsterRenderer;
import cz.wux.colonycraft.client.render.GuardEntityRenderer;
import cz.wux.colonycraft.client.screen.ColonyBannerScreen;
import cz.wux.colonycraft.client.screen.ColonyManagementScreen;
import cz.wux.colonycraft.client.screen.GuidebookScreen;
import cz.wux.colonycraft.client.screen.ResearchScreen;
import cz.wux.colonycraft.client.screen.StockpileScreen;
import cz.wux.colonycraft.item.GuidebookItem;
import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.registry.ModEntities;
import cz.wux.colonycraft.registry.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;

public class ColonyCraftClient implements ClientModInitializer {

    private static KeyBinding colonyManagementKey;

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.COLONIST,       ColonistEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.GUARD,          GuardEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.COLONY_MONSTER, ColonyMonsterRenderer::new);

        HandledScreens.register(ModScreenHandlers.STOCKPILE,     StockpileScreen::new);
        HandledScreens.register(ModScreenHandlers.COLONY_BANNER, ColonyBannerScreen::new);
        HandledScreens.register(ModScreenHandlers.RESEARCH,      ResearchScreen::new);

        // Wire up guidebook to open GUI screen instead of chat
        GuidebookItem.clientScreenOpener = () -> MinecraftClient.getInstance().setScreen(new GuidebookScreen());

        // ';' key -> Colony Management Screen (like Colony Survival)
        KeyBinding.Category ccCategory = new KeyBinding.Category(Identifier.of("colonycraft", "colonycraft"));
        colonyManagementKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.colonycraft.management",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_SEMICOLON,
                ccCategory
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (colonyManagementKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ColonyManagementScreen());
                }
            }
            // Colony border particle rendering
            ColonyBorderRenderer.tick(client);
            // Area wand visualization
            AreaWandRenderer.tick(client);
        });

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
