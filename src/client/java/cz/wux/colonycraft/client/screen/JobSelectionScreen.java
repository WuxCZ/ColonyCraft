package cz.wux.colonycraft.client.screen;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.item.AreaWandItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Job type selection screen — shown FIRST when using the wand.
 * Lists all unlocked jobs that use work areas (usesArea() == true).
 * After selecting a job type, the wand switches to corner-selection mode.
 */
public class JobSelectionScreen extends Screen {

    private static final int PANEL_W = 260;
    private static final int PANEL_H = 320;
    private final List<ColonistJob> areaJobs = new ArrayList<>();
    private int scrollOffset = 0;

    public JobSelectionScreen() {
        super(Text.literal("Select Job Type"));
    }

    @Override
    protected void init() {
        super.init();
        areaJobs.clear();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getServer() == null) return;

        // Get unlocked jobs from colony data
        Collection<ColonyData> colonies = ColonyManager.get(mc.getServer()).getAllColonies();
        if (colonies.isEmpty()) return;
        ColonyData colony = colonies.iterator().next();

        for (ColonistJob job : ColonistJob.values()) {
            if (job == ColonistJob.UNEMPLOYED) continue;
            if (!job.usesArea()) continue;
            if (!colony.isJobUnlocked(job)) continue;
            areaJobs.add(job);
        }

        rebuildButtons();
    }

    private void rebuildButtons() {
        clearChildren();
        int cx = (width - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;

        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7cCancel"), btn -> close())
            .dimensions(cx + PANEL_W - 66, cy + PANEL_H - 26, 56, 20).build());

        if (areaJobs.size() > 12) {
            addDrawableChild(ButtonWidget.builder(Text.literal("\u25B2"), btn -> {
                scrollOffset = Math.max(0, scrollOffset - 3);
                rebuildButtons();
            }).dimensions(cx + PANEL_W - 18, cy + 40, 14, 14).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("\u25BC"), btn -> {
                scrollOffset = Math.min(areaJobs.size() - 1, scrollOffset + 3);
                rebuildButtons();
            }).dimensions(cx + PANEL_W - 18, cy + PANEL_H - 46, 14, 14).build());
        }

        int listY = cy + 40;
        int maxVisible = Math.min(12, (cy + PANEL_H - 52 - listY) / 22);
        int end = Math.min(areaJobs.size(), scrollOffset + maxVisible);
        for (int i = scrollOffset; i < end; i++) {
            final ColonistJob job = areaJobs.get(i);
            String label = job.displayName;
            if (job.maxAreaSize > 0) {
                label += " \u00a77(max " + job.maxAreaSize + "\u00d7" + job.maxAreaSize + ")";
            }
            if (!job.requiresBlock) {
                label += " \u00a7a\u2605";
            }
            addDrawableChild(ButtonWidget.builder(Text.literal(label), btn -> {
                selectJob(job);
                close();
            }).dimensions(cx + 6, listY, PANEL_W - 28, 20).build());
            listY += 22;
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xC0101010);
        int cx = (width - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;

        ctx.fill(cx, cy, cx + PANEL_W, cy + PANEL_H, 0xEE101828);
        ctx.fill(cx, cy, cx + PANEL_W, cy + 2, 0xFF4488FF);
        ctx.fill(cx, cy + PANEL_H - 2, cx + PANEL_W, cy + PANEL_H, 0xFF4488FF);
        ctx.fill(cx, cy, cx + 2, cy + PANEL_H, 0xFF4488FF);
        ctx.fill(cx + PANEL_W - 2, cy, cx + PANEL_W, cy + PANEL_H, 0xFF4488FF);

        ctx.drawText(textRenderer, Text.literal("\u00a7b\u00a7l\u2692 Select Job Type"), cx + 8, cy + 8, 0xFF55AAFF, true);
        ctx.fill(cx + 4, cy + 22, cx + PANEL_W - 4, cy + 23, 0x4455AAFF);
        ctx.drawText(textRenderer, Text.literal("\u00a77Select a job, then mark 2 corners for the work area."),
            cx + 8, cy + 27, 0xFFAAAAAA, false);

        if (areaJobs.isEmpty()) {
            ctx.drawText(textRenderer, Text.literal("\u00a7cNo area-based jobs unlocked!"),
                cx + 8, cy + 50, 0xFFFF5555, false);
            ctx.drawText(textRenderer, Text.literal("\u00a77Research more jobs through Science."),
                cx + 8, cy + 62, 0xFFAAAAAA, false);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void selectJob(ColonistJob job) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        AreaWandItem.setSelectedJob(mc.player.getUuid(), job);
        mc.player.sendMessage(Text.literal(
            "\u00a7a[Wand] \u00a7fSelected: \u00a7e" + job.displayName +
            (job.maxAreaSize > 0 ? " \u00a77(max " + job.maxAreaSize + "\u00d7" + job.maxAreaSize + ")" : "") +
            " \u00a77\u2014 now right-click 2 corners to define the work area."), false);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}