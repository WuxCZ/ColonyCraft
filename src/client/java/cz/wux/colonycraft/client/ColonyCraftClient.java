package cz.wux.colonycraft.client;

import cz.wux.colonycraft.client.render.AreaWandRenderer;
import cz.wux.colonycraft.client.render.ColonistEntityRenderer;
import cz.wux.colonycraft.client.render.ColonyBorderRenderer;
import cz.wux.colonycraft.client.render.ColonyMonsterRenderer;
import cz.wux.colonycraft.client.render.GuardEntityRenderer;
import cz.wux.colonycraft.client.screen.ColonyBannerScreen;
import cz.wux.colonycraft.client.screen.ColonyManagementScreen;
import cz.wux.colonycraft.client.screen.GuidebookScreen;
import cz.wux.colonycraft.client.screen.JobSelectionScreen;
import cz.wux.colonycraft.client.screen.ResearchScreen;
import cz.wux.colonycraft.client.screen.StockpileScreen;
import cz.wux.colonycraft.item.GuidebookItem;
import cz.wux.colonycraft.item.AreaWandItem;
import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.registry.ModEntities;
import cz.wux.colonycraft.registry.ModItems;
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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
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

        // Guidebook still works if right-clicked (but not in starter kit)
        GuidebookItem.clientScreenOpener = () -> MinecraftClient.getInstance().setScreen(new GuidebookScreen());

        // Area wand -> job selection screen (shown FIRST before area selection)
        AreaWandItem.clientAreaCompleteHandler = () -> MinecraftClient.getInstance().execute(() ->
            MinecraftClient.getInstance().setScreen(new JobSelectionScreen()));

        // Colony management key (;)
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
            ColonyBorderRenderer.tick(client);
            AreaWandRenderer.tick(client);
        });

        // HUD overlay
        HudRenderCallback.EVENT.register((drawContext, deltaTick) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.getServer() == null || mc.player == null) return;
            if (mc.currentScreen != null) return;
            Collection<ColonyData> colonies = ColonyManager.get(mc.getServer()).getAllColonies();
            if (colonies.isEmpty()) return;
            ColonyData colony = colonies.iterator().next();

            // -- Wave night vignette (red, semi-transparent border during active wave time) --
            if (mc.world != null) {
                long dayTimeVignette = mc.world.getTimeOfDay() % 24000;
                boolean isWaveNight = dayTimeVignette >= 13000 || dayTimeVignette < 500;
                if (isWaveNight && colony.getColonistCount() > 0) {
                    int sw = drawContext.getScaledWindowWidth();
                    int sh = drawContext.getScaledWindowHeight();
                    int vm = 40; // vignette margin/thickness
                    long blink = System.currentTimeMillis() / 800;
                    int alpha = (blink % 2 == 0) ? 0x55 : 0x33;
                    int col = (alpha << 24) | 0x440000;
                    // Top, bottom, left, right bars
                    drawContext.fill(0, 0, sw, vm, col);
                    drawContext.fill(0, sh - vm, sw, sh, col);
                    drawContext.fill(0, vm, vm, sh - vm, col);
                    drawContext.fill(sw - vm, vm, sw, sh - vm, col);
                }
            }

            int x = 6, y = 6;
            drawContext.fill(x - 3, y - 3, x + 130, y + 57, 0x88000000);
            drawContext.drawText(mc.textRenderer, Text.literal("\u00a76\u2654 Colony"),                                                        x, y,      0xFFFFD700, true);
            drawContext.drawText(mc.textRenderer, Text.literal("\u00a7aFood: \u00a7f" + colony.getFoodUnits()),                                 x, y + 11, 0xFFFFFFFF, true);
            drawContext.drawText(mc.textRenderer, Text.literal("\u00a7bPop:  \u00a7f" + colony.getColonistCount() + "/" + colony.getPopulationCap()), x, y + 22, 0xFFFFFFFF, true);
            drawContext.drawText(mc.textRenderer, Text.literal("\u00a7eSci:  \u00a7f" + colony.getSciencePoints()),                             x, y + 33, 0xFFFFFFFF, true);
            drawContext.drawText(mc.textRenderer, Text.literal("\u00a7cDay:  \u00a7f" + colony.getDaysSurvived()),                              x, y + 44, 0xFFFFFFFF, true);

            // Low food warning
            if (colony.getFoodUnits() <= 10 && colony.getColonistCount() > 0) {
                long blink = System.currentTimeMillis() / 500;
                if (blink % 2 == 0) {
                    drawContext.drawText(mc.textRenderer, Text.literal("\u00a7c\u00a7l\u26A0 LOW FOOD!"), x, y + 55, 0xFFFF0000, true);
                }
            }
            // Pop cap warning
            if (!colony.canSpawnMoreColonists() && colony.getColonistCount() > 0) {
                drawContext.drawText(mc.textRenderer, Text.literal("\u00a7e\u26A0 Beds!"), x + 80, y + 22, 0xFFFFFF00, true);
            }

            // Wave countdown timer (shows from dusk until wave spawn)
            if (mc.world != null) {
                long dayTimeHud = mc.world.getTimeOfDay() % 24000;
                if (dayTimeHud >= 11000 && dayTimeHud < 13000) {
                    int ticksUntilWave = (int)(13000 - dayTimeHud);
                    int secondsUntilWave = ticksUntilWave / 20;
                    int minutes = secondsUntilWave / 60;
                    int seconds = secondsUntilWave % 60;
                    String countdownText = String.format("\u00a7c\u2694 Wave in %d:%02d", minutes, seconds);
                    int cY = y + 66;
                    drawContext.fill(x - 3, cY - 2, x + 100, cY + 11, 0xAA330000);
                    if (ticksUntilWave < 1000) {
                        long blink = System.currentTimeMillis() / 250;
                        if (blink % 2 == 0) {
                            drawContext.drawText(mc.textRenderer, Text.literal(countdownText), x, cY, 0xFFFF4444, true);
                        }
                    } else {
                        drawContext.drawText(mc.textRenderer, Text.literal(countdownText), x, cY, 0xFFFFAAAA, true);
                    }
                }
            }

            // -- Area size display above hotbar during wand selection --
            boolean holdingWand = mc.player.getMainHandStack().isOf(ModItems.AREA_WAND)
                               || mc.player.getOffHandStack().isOf(ModItems.AREA_WAND);
            if (holdingWand) {
                BlockPos[] sel = AreaWandItem.getSelection(mc.player.getUuid());
                ColonistJob selectedJob = AreaWandItem.getSelectedJob(mc.player.getUuid());
                if (sel != null && sel[0] != null && selectedJob != null) {
                    BlockPos other;
                    if (sel[1] != null) {
                        other = sel[1];
                    } else if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                        other = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
                    } else {
                        other = null;
                    }
                    if (other != null) {
                        int w = Math.abs(other.getX() - sel[0].getX()) + 1;
                        int d = Math.abs(other.getZ() - sel[0].getZ()) + 1;
                        String sizeText = "\u00a7a" + selectedJob.displayName + ": \u00a7e" + w + "\u00d7" + d;
                        if (selectedJob.maxAreaSize > 0 && (w > selectedJob.maxAreaSize || d > selectedJob.maxAreaSize)) {
                            sizeText = "\u00a7c" + selectedJob.displayName + ": \u00a74" + w + "\u00d7" + d + " \u00a7c(max " + selectedJob.maxAreaSize + ")";
                        }
                        int textWidth = mc.textRenderer.getWidth(Text.literal(sizeText));
                        int hx = drawContext.getScaledWindowWidth() / 2 - textWidth / 2;
                        int hy = drawContext.getScaledWindowHeight() - 59;
                        drawContext.fill(hx - 4, hy - 2, hx + textWidth + 4, hy + 11, 0xAA000000);
                        drawContext.drawText(mc.textRenderer, Text.literal(sizeText), hx, hy, 0xFFFFFFFF, true);
                    }
                }
            }
        });
    }
}