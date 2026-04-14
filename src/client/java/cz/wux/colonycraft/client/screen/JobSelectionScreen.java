package cz.wux.colonycraft.client.screen;

import cz.wux.colonycraft.blockentity.JobBlockEntity;
import cz.wux.colonycraft.item.AreaWandItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class JobSelectionScreen extends Screen {

    private static final int PANEL_W = 260;
    private static final int PANEL_H = 220;
    private final List<JobEntry> jobs = new ArrayList<>();
    private int scrollOffset = 0;

    public JobSelectionScreen() {
        super(Text.literal("Assign Work Area"));
    }

    @Override
    protected void init() {
        super.init();
        jobs.clear();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getServer() == null || mc.player == null) return;

        ServerWorld world = mc.getServer().getOverworld();
        BlockPos playerPos = mc.player.getBlockPos();

        for (BlockPos bp : BlockPos.iterate(
                playerPos.add(-32, -8, -32),
                playerPos.add(32, 8, 32))) {
            var be = world.getBlockEntity(bp);
            if (be instanceof JobBlockEntity jb) {
                jobs.add(new JobEntry(
                    jb.getJob().displayName,
                    bp.toImmutable(),
                    jb.hasAssignedColonist(),
                    jb.hasArea(),
                    (int) Math.sqrt(playerPos.getSquaredDistance(bp))
                ));
            }
        }
        jobs.sort((a, b) -> {
            int aw = (a.hasColonist ? 2 : 0) + (a.hasArea ? 1 : 0);
            int bw = (b.hasColonist ? 2 : 0) + (b.hasArea ? 1 : 0);
            if (aw != bw) return Integer.compare(aw, bw);
            return Integer.compare(a.distance, b.distance);
        });

        rebuildButtons();
    }

    private void rebuildButtons() {
        clearChildren();
        int cx = (width - PANEL_W) / 2;
        int cy = (height - PANEL_H) / 2;

        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7cCancel"), btn -> close())
            .dimensions(cx + PANEL_W - 66, cy + PANEL_H - 26, 56, 20).build());

        if (jobs.size() > 8) {
            addDrawableChild(ButtonWidget.builder(Text.literal("\u25B2"), btn -> {
                scrollOffset = Math.max(0, scrollOffset - 3);
                rebuildButtons();
            }).dimensions(cx + PANEL_W - 18, cy + 38, 14, 14).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("\u25BC"), btn -> {
                scrollOffset = Math.min(jobs.size() - 1, scrollOffset + 3);
                rebuildButtons();
            }).dimensions(cx + PANEL_W - 18, cy + PANEL_H - 46, 14, 14).build());
        }

        int listY = cy + 40;
        int maxVisible = Math.min(8, (cy + PANEL_H - 52 - listY) / 18);
        int end = Math.min(jobs.size(), scrollOffset + maxVisible);
        for (int i = scrollOffset; i < end; i++) {
            final JobEntry je = jobs.get(i);
            String icon = je.hasArea ? "\u2713 " : "\u25CB ";
            String label = icon + je.jobName + " (" + je.distance + "m)";
            if (je.hasColonist) label += " \u2666";
            addDrawableChild(ButtonWidget.builder(Text.literal(label), btn -> {
                assignAreaToJob(je);
                close();
            }).dimensions(cx + 6, listY, PANEL_W - 28, 16).build());
            listY += 18;
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

        ctx.drawText(textRenderer, Text.literal("\u00a7b\u00a7l\u2692 Assign Work Area"), cx + 8, cy + 8, 0xFF55AAFF, true);
        ctx.fill(cx + 4, cy + 22, cx + PANEL_W - 4, cy + 23, 0x4455AAFF);

        BlockPos[] sel = getSelection();
        if (sel != null && sel[0] != null && sel[1] != null) {
            int w = Math.abs(sel[1].getX() - sel[0].getX()) + 1;
            int h = Math.abs(sel[1].getY() - sel[0].getY()) + 1;
            int d = Math.abs(sel[1].getZ() - sel[0].getZ()) + 1;
            ctx.drawText(textRenderer, Text.literal("\u00a77Area: \u00a7e" + w + "\u00d7" + h + "\u00d7" + d + " blocks"),
                cx + 8, cy + 26, 0xFFDDDDDD, false);
        }

        if (jobs.isEmpty()) {
            ctx.drawText(textRenderer, Text.literal("\u00a7cNo job blocks found nearby!"),
                cx + 8, cy + 50, 0xFFFF5555, false);
            ctx.drawText(textRenderer, Text.literal("\u00a77Place a job block first, then try again."),
                cx + 8, cy + 62, 0xFFAAAAAA, false);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void assignAreaToJob(JobEntry entry) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getServer() == null || mc.player == null) return;

        BlockPos[] sel = getSelection();
        if (sel == null || sel[0] == null || sel[1] == null) return;

        mc.getServer().execute(() -> {
            ServerWorld world = mc.getServer().getOverworld();
            var be = world.getBlockEntity(entry.pos);
            if (be instanceof JobBlockEntity jb) {
                BlockPos min = new BlockPos(
                    Math.min(sel[0].getX(), sel[1].getX()),
                    Math.min(sel[0].getY(), sel[1].getY()),
                    Math.min(sel[0].getZ(), sel[1].getZ()));
                BlockPos max = new BlockPos(
                    Math.max(sel[0].getX(), sel[1].getX()),
                    Math.max(sel[0].getY(), sel[1].getY()),
                    Math.max(sel[0].getZ(), sel[1].getZ()));
                jb.setArea(min, max);
                int w = max.getX() - min.getX() + 1;
                int d = max.getZ() - min.getZ() + 1;
                mc.player.sendMessage(Text.literal(
                    "\u00a7a[" + jb.getJob().displayName + "] \u00a7fWork area set: \u00a7e" + w + "\u00d7" + d +
                    " \u00a77blocks"), false);
            }
            AreaWandItem.clearSelection(mc.player.getUuid());
        });
    }

    private BlockPos[] getSelection() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return null;
        return AreaWandItem.getSelection(mc.player.getUuid());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private record JobEntry(String jobName, BlockPos pos, boolean hasColonist, boolean hasArea, int distance) {}
}