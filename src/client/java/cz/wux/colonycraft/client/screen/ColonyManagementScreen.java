package cz.wux.colonycraft.client.screen;

import cz.wux.colonycraft.blockentity.JobBlockEntity;
import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.entity.ColonistEntity;
import cz.wux.colonycraft.entity.GuardEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class ColonyManagementScreen extends Screen {

    private ColonyData colony;
    private int scrollOffset = 0;
    private static final int LINE_HEIGHT = 14;
    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 260;

    public ColonyManagementScreen() {
        super(Text.literal("Colony Management"));
    }

    @Override
    protected void init() {
        super.init();
        refreshColony();
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearChildren();
        int cx = (width - PANEL_WIDTH) / 2;
        int cy = (height - PANEL_HEIGHT) / 2;

        // Recruit button
        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7a+ Recruit"), btn -> recruitColonist())
                .dimensions(cx + PANEL_WIDTH - 90, cy + PANEL_HEIGHT - 28, 80, 20)
                .build());

        // Auto-assign button
        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7b\u2692 Auto-Assign"), btn -> autoAssignJobs())
                .dimensions(cx + PANEL_WIDTH - 180, cy + PANEL_HEIGHT - 28, 85, 20)
                .build());

        // Guide button
        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7e\u2756 Guide"), btn -> {
            MinecraftClient.getInstance().setScreen(new GuidebookScreen());
        }).dimensions(cx + 8, cy + PANEL_HEIGHT - 28, 65, 20).build());

        // Party invite button
        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7d\u2764 Party"), btn -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && mc.getServer() != null && colony != null) {
                mc.player.sendMessage(Text.literal("\u00a7dParty members: " + colony.getMembers().size() +
                    ". Use /colonycraft invite <player> to add members."), false);
            }
        }).dimensions(cx + 78, cy + PANEL_HEIGHT - 28, 60, 20).build());

        // Scroll buttons
        addDrawableChild(ButtonWidget.builder(Text.literal("\u25B2"), btn -> {
            scrollOffset = Math.max(0, scrollOffset - 3);
            rebuildWidgets();
        }).dimensions(cx + PANEL_WIDTH - 18, cy + 70, 14, 14).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("\u25BC"), btn -> {
            scrollOffset += 3;
            rebuildWidgets();
        }).dimensions(cx + PANEL_WIDTH - 18, cy + PANEL_HEIGHT - 46, 14, 14).build());

        // Colonist job assignment buttons
        if (colony != null) {
            List<ColonistInfo> colonists = gatherColonistInfo();
            int listY = getListStartY();
            int maxVisible = (getListEndY() - listY) / LINE_HEIGHT;
            int end = Math.min(colonists.size(), scrollOffset + maxVisible);
            for (int i = scrollOffset; i < end; i++) {
                final ColonistInfo ci = colonists.get(i);
                if (ci.entityUuid != null && !ci.isGuard && !ci.job.equals("Unknown (unloaded)")) {
                    addDrawableChild(ButtonWidget.builder(Text.literal("\u21BB"), btn -> {
                        cycleJob(ci.entityUuid);
                        rebuildWidgets();
                    }).dimensions(cx + PANEL_WIDTH - 36, listY, 14, 12).build());
                }
                listY += LINE_HEIGHT;
            }
        }
    }

    private int getListStartY() {
        return (height - PANEL_HEIGHT) / 2 + 82;
    }

    private int getListEndY() {
        return (height - PANEL_HEIGHT) / 2 + PANEL_HEIGHT - 50;
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
            // Consume 5 food from stockpile
            BlockPos sp = colony.getStockpilePos();
            if (sp != null) {
                var be = world.getBlockEntity(sp);
                if (be instanceof cz.wux.colonycraft.blockentity.StockpileBlockEntity stockpile) {
                    for (int i = 0; i < 5; i++) stockpile.consumeOneFoodItem();
                    stockpile.syncFoodToColony();
                }
            }
            ColonistEntity.spawnForColony(world, colony, colony.getBannerPos(), mgr);
        });
    }

    /** Auto-assign unemployed colonists to nearest unclaimed job blocks. */
    private void autoAssignJobs() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getServer() == null || colony == null) return;
        mc.getServer().execute(() -> {
            ServerWorld world = mc.getServer().getOverworld();
            BlockPos banner = colony.getBannerPos();
            int assigned = 0;
            for (UUID uuid : colony.getColonistUuids()) {
                Entity e = world.getEntity(uuid);
                if (e instanceof ColonistEntity ce && ce.getColonistJob() == ColonistJob.UNEMPLOYED) {
                    // Find nearest unclaimed job block
                    for (BlockPos bp : BlockPos.iterate(banner.add(-16, -4, -16), banner.add(16, 4, 16))) {
                        var be = world.getBlockEntity(bp);
                        if (be instanceof JobBlockEntity jb && !jb.hasAssignedColonist()
                                && colony.isJobUnlocked(jb.getJob()) && !jb.getJob().isGuard()) {
                            jb.assignColonist(ce.getUuid());
                            ce.setColonistJob(jb.getJob());
                            ce.setJobBlockPos(bp.toImmutable());
                            assigned++;
                            break;
                        }
                    }
                }
            }
            if (mc.player != null) {
                mc.player.sendMessage(Text.literal("\u00a7aAuto-assigned " + assigned + " colonists to jobs."), false);
            }
        });
    }

    /** Cycle a colonist's job to the next available unassigned job block. */
    private void cycleJob(UUID entityUuid) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getServer() == null || colony == null) return;
        mc.getServer().execute(() -> {
            ServerWorld world = mc.getServer().getOverworld();
            Entity e = world.getEntity(entityUuid);
            if (!(e instanceof ColonistEntity ce)) return;

            // Unassign from current job block
            BlockPos oldJobPos = ce.getJobBlockPos();
            if (oldJobPos != null) {
                var oldBe = world.getBlockEntity(oldJobPos);
                if (oldBe instanceof JobBlockEntity jb) jb.unassignColonist();
            }

            // Find next unassigned job block
            BlockPos banner = colony.getBannerPos();
            for (BlockPos bp : BlockPos.iterate(banner.add(-16, -4, -16), banner.add(16, 4, 16))) {
                var be = world.getBlockEntity(bp);
                if (be instanceof JobBlockEntity jb && !jb.hasAssignedColonist()
                        && colony.isJobUnlocked(jb.getJob()) && !jb.getJob().isGuard()) {
                    jb.assignColonist(ce.getUuid());
                    ce.setColonistJob(jb.getJob());
                    ce.setJobBlockPos(bp.toImmutable());
                    return;
                }
            }
            // No job found - set to unemployed
            ce.setColonistJob(ColonistJob.UNEMPLOYED);
            ce.setJobBlockPos(null);
        });
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0xC0101010);
        refreshColony();

        int cx = (width - PANEL_WIDTH) / 2;
        int cy = (height - PANEL_HEIGHT) / 2;

        // Panel
        ctx.fill(cx, cy, cx + PANEL_WIDTH, cy + PANEL_HEIGHT, 0xEE101828);
        ctx.fill(cx, cy, cx + PANEL_WIDTH, cy + 2, 0xFFFFD700);
        ctx.fill(cx, cy + PANEL_HEIGHT - 2, cx + PANEL_WIDTH, cy + PANEL_HEIGHT, 0xFFFFD700);
        ctx.fill(cx, cy, cx + 2, cy + PANEL_HEIGHT, 0xFFFFD700);
        ctx.fill(cx + PANEL_WIDTH - 2, cy, cx + PANEL_WIDTH, cy + PANEL_HEIGHT, 0xFFFFD700);

        ctx.drawText(textRenderer, Text.literal("\u00a76\u00a7l\u2654 Colony Management"), cx + 8, cy + 8, 0xFFFFD700, true);
        ctx.fill(cx + 4, cy + 22, cx + PANEL_WIDTH - 4, cy + 23, 0x88FFD700);

        if (colony == null) {
            ctx.drawText(textRenderer, Text.literal("\u00a77No colony found. Place a Colony Banner first."), cx + 8, cy + 32, 0xFFAAAAAA, false);
            super.render(ctx, mouseX, mouseY, delta);
            return;
        }

        // Stats
        int sy = cy + 28;
        ctx.drawText(textRenderer, Text.literal("\u00a77Owner: \u00a7f" + colony.getOwnerName()), cx + 8, sy, 0xFFFFFFFF, false);
        ctx.drawText(textRenderer, Text.literal("\u00a7dParty: \u00a7f" + colony.getMembers().size()), cx + 200, sy, 0xFFFFFFFF, false);
        sy += 12;
        ctx.drawText(textRenderer, Text.literal("\u00a7aPop: \u00a7f" + colony.getColonistCount() + "/" + colony.getPopulationCap()), cx + 8, sy, 0xFFFFFFFF, false);
        ctx.drawText(textRenderer, Text.literal("\u00a7eFood: \u00a7f" + colony.getFoodUnits()), cx + 110, sy, 0xFFFFFFFF, false);
        ctx.drawText(textRenderer, Text.literal("\u00a7bSci: \u00a7f" + colony.getSciencePoints()), cx + 200, sy, 0xFFFFFFFF, false);
        ctx.drawText(textRenderer, Text.literal("\u00a7cDay: \u00a7f" + colony.getDaysSurvived()), cx + 280, sy, 0xFFFFFFFF, false);

        sy += 14;
        boolean canRecruit = colony.canSpawnMoreColonists() && colony.getFoodUnits() >= 5;
        ctx.drawText(textRenderer, Text.literal(canRecruit ? "\u00a7a(5 food to recruit)" : "\u00a7c(Can't recruit)"), cx + 8, sy, 0xFF888888, false);

        // Divider
        ctx.fill(cx + 4, sy + 12, cx + PANEL_WIDTH - 4, sy + 13, 0x44FFFFFF);

        // Header
        int listY = sy + 16;
        ctx.drawText(textRenderer, Text.literal("\u00a7n\u00a77#  Job"), cx + 8, listY, 0xFFBBBBBB, false);
        ctx.drawText(textRenderer, Text.literal("\u00a7n\u00a77Status"), cx + 160, listY, 0xFFBBBBBB, false);
        ctx.drawText(textRenderer, Text.literal("\u00a7n\u00a77HP"), cx + 260, listY, 0xFFBBBBBB, false);
        ctx.drawText(textRenderer, Text.literal("\u00a77\u21BB"), cx + PANEL_WIDTH - 36, listY, 0xFF888888, false);
        listY += LINE_HEIGHT + 2;

        // Colonist entries
        List<ColonistInfo> colonists = gatherColonistInfo();
        int maxVisible = (cy + PANEL_HEIGHT - 50 - listY) / LINE_HEIGHT;
        int end = Math.min(colonists.size(), scrollOffset + maxVisible);
        for (int i = scrollOffset; i < end; i++) {
            ColonistInfo ci = colonists.get(i);
            String idx = String.format("\u00a78%2d  ", i + 1);
            String jobColor = ci.isGuard ? "\u00a7c" : (ci.job.equals("Unemployed") ? "\u00a78" : "\u00a7a");
            ctx.drawText(textRenderer, Text.literal(idx + jobColor + ci.job), cx + 8, listY, 0xFFFFFFFF, false);

            String status = ci.statusText;
            ctx.drawText(textRenderer, Text.literal(status), cx + 160, listY, 0xFFFFFFFF, false);

            // Health bar
            if (ci.maxHealth > 0) {
                int barX = cx + 260;
                int barW = 40;
                ctx.fill(barX, listY + 1, barX + barW, listY + 8, 0xFF333333);
                int fillW = (int)(barW * (ci.health / ci.maxHealth));
                int hpColor = ci.health > ci.maxHealth * 0.5 ? 0xFF00CC00 : (ci.health > ci.maxHealth * 0.25 ? 0xFFCCCC00 : 0xFFCC0000);
                ctx.fill(barX, listY + 1, barX + fillW, listY + 8, hpColor);
            }

            listY += LINE_HEIGHT;
        }

        if (colonists.isEmpty()) {
            ctx.drawText(textRenderer, Text.literal("\u00a78No colonists yet. Place job blocks and recruit!"), cx + 8, listY, 0xFF888888, false);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }

    private List<ColonistInfo> gatherColonistInfo() {
        List<ColonistInfo> list = new ArrayList<>();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getServer() == null || colony == null) return list;

        ServerWorld world = mc.getServer().getOverworld();
        for (UUID uuid : colony.getColonistUuids()) {
            Entity e = world.getEntity(uuid);
            if (e instanceof ColonistEntity ce) {
                ColonistInfo ci = new ColonistInfo();
                ci.entityUuid = ce.getUuid();
                ci.job = ce.getColonistJob().displayName;
                ci.isGuard = false;
                ci.health = ce.getHealth();
                ci.maxHealth = ce.getMaxHealth();
                ci.statusText = ce.getCurrentStatus();
                list.add(ci);
            } else if (e instanceof GuardEntity ge) {
                ColonistInfo ci = new ColonistInfo();
                ci.entityUuid = ge.getUuid();
                ci.job = ge.getGuardJob().displayName;
                ci.isGuard = true;
                ci.health = ge.getHealth();
                ci.maxHealth = ge.getMaxHealth();
                ci.statusText = "\u00a7c\u2694 Guarding";
                list.add(ci);
            } else {
                ColonistInfo ci = new ColonistInfo();
                ci.entityUuid = uuid;
                ci.job = "Unknown (unloaded)";
                ci.isGuard = false;
                ci.health = 0;
                ci.maxHealth = 20;
                ci.statusText = "\u00a78? Unloaded";
                list.add(ci);
            }
        }
        return list;
    }

    private static class ColonistInfo {
        UUID entityUuid;
        String job;
        boolean isGuard;
        float health;
        float maxHealth;
        String statusText;
    }
}