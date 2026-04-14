package cz.wux.colonycraft.client.screen;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.entity.ColonistEntity;
import cz.wux.colonycraft.blockentity.JobBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.*;

/**
 * Colony Management Screen opened by pressing ';' (like Colony Survival).
 * Shows live colony stats, colonist list with jobs, and a Recruit button.
 */
public class ColonyManagementScreen extends Screen {

    private ColonyData colony;
    private int scrollOffset = 0;
    private static final int LINE_HEIGHT = 12;
    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 220;

    public ColonyManagementScreen() {
        super(Text.literal("Colony Management"));
    }

    @Override
    protected void init() {
        super.init();
        refreshColony();

        int cx = (width - PANEL_WIDTH) / 2;
        int cy = (height - PANEL_HEIGHT) / 2;

        // Recruit button
        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7a+ Recruit Colonist"), btn -> recruitColonist())
                .dimensions(cx + PANEL_WIDTH - 130, cy + PANEL_HEIGHT - 28, 120, 20)
                .build());

        // Scroll buttons
        addDrawableChild(ButtonWidget.builder(Text.literal("\u25B2"), btn -> { scrollOffset = Math.max(0, scrollOffset - 3); })
                .dimensions(cx + PANEL_WIDTH - 18, cy + 60, 14, 14)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("\u25BC"), btn -> { scrollOffset += 3; })
                .dimensions(cx + PANEL_WIDTH - 18, cy + PANEL_HEIGHT - 46, 14, 14)
                .build());

        // Guide button
        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7e\u2756 Guide"), btn -> {
            MinecraftClient.getInstance().setScreen(new GuidebookScreen());
        }).dimensions(cx + 8, cy + PANEL_HEIGHT - 28, 70, 20).build());
    }

    private void refreshColony() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getServer() != null) {
            colony = ColonyManager.get(mc.getServer()).getAllColonies().stream().findFirst().orElse(null);
        }
    }

    private void recruitColonist() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getServer() == null || colony == null) return;
        mc.getServer().execute(() -> {
            ServerWorld world = mc.getServer().getOverworld();
            ColonyManager mgr = ColonyManager.get(mc.getServer());

            if (!colony.canSpawnMoreColonists()) return;
            if (colony.getFoodUnits() < 5) return;

            colony.addFood(-5);
            ColonistEntity.spawnForColony(world, colony, colony.getBannerPos(), mgr);
        });
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Manual dark overlay instead of renderBackground (avoids double-blur crash in 1.21.11)
        ctx.fill(0, 0, this.width, this.height, 0xC0101010);
        refreshColony();

        int cx = (width - PANEL_WIDTH) / 2;
        int cy = (height - PANEL_HEIGHT) / 2;

        // Background panel
        ctx.fill(cx, cy, cx + PANEL_WIDTH, cy + PANEL_HEIGHT, 0xEE101828);
        // Border
        ctx.fill(cx, cy, cx + PANEL_WIDTH, cy + 2, 0xFFFFD700);
        ctx.fill(cx, cy + PANEL_HEIGHT - 2, cx + PANEL_WIDTH, cy + PANEL_HEIGHT, 0xFFFFD700);
        ctx.fill(cx, cy, cx + 2, cy + PANEL_HEIGHT, 0xFFFFD700);
        ctx.fill(cx + PANEL_WIDTH - 2, cy, cx + PANEL_WIDTH, cy + PANEL_HEIGHT, 0xFFFFD700);

        // Title
        ctx.drawText(textRenderer, "\u00a76\u00a7l\u2654 Colony Management", cx + 8, cy + 8, 0xFFD700, true);
        ctx.fill(cx + 4, cy + 22, cx + PANEL_WIDTH - 4, cy + 23, 0x88FFD700);

        if (colony == null) {
            ctx.drawText(textRenderer, "\u00a77No colony found. Place a Colony Banner first.", cx + 8, cy + 32, 0xAAAAAA, false);
            super.render(ctx, mouseX, mouseY, delta);
            return;
        }

        // Stats bar
        int sy = cy + 28;
        ctx.drawText(textRenderer, "\u00a77Owner: \u00a7f" + colony.getOwnerName(), cx + 8, sy, 0xFFFFFF, false);
        ctx.drawText(textRenderer, "\u00a7aPop: \u00a7f" + colony.getColonistCount() + "/" + colony.getPopulationCap(), cx + 130, sy, 0xFFFFFF, false);
        ctx.drawText(textRenderer, "\u00a7eFood: \u00a7f" + colony.getFoodUnits(), cx + 210, sy, 0xFFFFFF, false);
        sy += 12;
        ctx.drawText(textRenderer, "\u00a7bScience: \u00a7f" + colony.getSciencePoints(), cx + 8, sy, 0xFFFFFF, false);
        ctx.drawText(textRenderer, "\u00a7cDay: \u00a7f" + colony.getDaysSurvived(), cx + 130, sy, 0xFFFFFF, false);

        int recruitCost = 5;
        boolean canRecruit = colony.canSpawnMoreColonists() && colony.getFoodUnits() >= recruitCost;
        ctx.drawText(textRenderer, canRecruit ? "\u00a7a(Recruit costs 5 food)" : "\u00a7c(Can't recruit - need food/cap)", cx + 8, cy + PANEL_HEIGHT - 24, 0x888888, false);

        // Divider
        ctx.fill(cx + 4, sy + 14, cx + PANEL_WIDTH - 4, sy + 15, 0x44FFFFFF);

        // Colonist list header
        int listY = sy + 18;
        ctx.drawText(textRenderer, "\u00a7n\u00a77#  Name / Job", cx + 8, listY, 0xBBBBBB, false);
        ctx.drawText(textRenderer, "\u00a7n\u00a77Status", cx + 200, listY, 0xBBBBBB, false);
        listY += LINE_HEIGHT + 2;

        // Colonist entries
        List<ColonistInfo> colonists = gatherColonistInfo();
        int maxVisible = (cy + PANEL_HEIGHT - 50 - listY) / LINE_HEIGHT;
        int end = Math.min(colonists.size(), scrollOffset + maxVisible);
        for (int i = scrollOffset; i < end; i++) {
            ColonistInfo ci = colonists.get(i);
            String idx = String.format("\u00a78%2d  ", i + 1);
            String jobColor = ci.isGuard ? "\u00a7c" : (ci.job.equals("Unemployed") ? "\u00a78" : "\u00a7a");
            ctx.drawText(textRenderer, idx + jobColor + ci.job, cx + 8, listY, 0xFFFFFF, false);
            String status = ci.isHungry ? "\u00a7c\u2620 Hungry" : (ci.isWorking ? "\u00a7a\u2692 Working" : "\u00a77\u25CB Idle");
            ctx.drawText(textRenderer, status, cx + 200, listY, 0xFFFFFF, false);
            listY += LINE_HEIGHT;
        }

        if (colonists.isEmpty()) {
            ctx.drawText(textRenderer, "\u00a78No colonists yet.", cx + 8, listY, 0x888888, false);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private List<ColonistInfo> gatherColonistInfo() {
        List<ColonistInfo> list = new ArrayList<>();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getServer() == null || colony == null) return list;

        ServerWorld world = mc.getServer().getOverworld();
        for (UUID uuid : colony.getColonistUuids()) {
            Entity e = world.getEntity(uuid);
            if (e instanceof ColonistEntity ce) {
                ColonistInfo ci = new ColonistInfo();
                ci.job = ce.getColonistJob().displayName;
                ci.isGuard = false;
                ci.isHungry = ce.isHungry();
                ci.isWorking = ce.isWorkCoolingDown();
                list.add(ci);
            } else if (e instanceof cz.wux.colonycraft.entity.GuardEntity ge) {
                ColonistInfo ci = new ColonistInfo();
                ci.job = ge.getGuardJob().displayName;
                ci.isGuard = true;
                ci.isHungry = false;
                ci.isWorking = true;
                list.add(ci);
            } else {
                ColonistInfo ci = new ColonistInfo();
                ci.job = "Unknown (unloaded)";
                ci.isGuard = false;
                ci.isHungry = false;
                ci.isWorking = false;
                list.add(ci);
            }
        }
        return list;
    }

    private static class ColonistInfo {
        String job;
        boolean isGuard;
        boolean isHungry;
        boolean isWorking;
    }
}