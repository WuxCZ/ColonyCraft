package cz.wux.colonycraft.client.screen;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.screen.ResearchScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import java.util.Optional;

/**
 * Research Tree screen.
 * Lists all unlockable jobs, their science-point costs, and whether the
 * player can currently afford them.  Clicking a button spends science points
 * and unlocks that job (singleplayer / integrated-server only).
 */
public class ResearchScreen extends HandledScreen<ResearchScreenHandler> {

    private static final ColonistJob[] JOBS = ResearchScreenHandler.UNLOCKABLE_JOBS;
    private static final int ROW_H = 14;

    public ResearchScreen(ResearchScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth  = 210;
        this.backgroundHeight = 30 + JOBS.length * ROW_H + 14;
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        playerInventoryTitleY = Integer.MAX_VALUE; // hide "Inventory" label

        // Unlock buttons in the left column, one per job
        for (int i = 0; i < JOBS.length; i++) {
            final ColonistJob job = JOBS[i];
            final int idx = i;
            int btnY = y + 24 + i * ROW_H;
            ButtonWidget btn = ButtonWidget.builder(Text.literal(unlockLabel(job)), b -> tryUnlock(job))
                    .dimensions(x + 4, btnY, backgroundWidth - 8, ROW_H - 1)
                    .tooltip(Tooltip.of(Text.literal(tooltipText(job))))
                    .build();
            btn.active = !isUnlocked(job);
            addDrawableChild(btn);
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xEE1A1A2E);
        // Gold border
        context.fill(x,     y,     x + backgroundWidth, y + 1,                    0xFFFFD700);
        context.fill(x,     y + backgroundHeight - 1, x + backgroundWidth, y + backgroundHeight, 0xFFFFD700);
        context.fill(x,     y,     x + 1,              y + backgroundHeight,      0xFFFFD700);
        context.fill(x + backgroundWidth - 1, y, x + backgroundWidth, y + backgroundHeight, 0xFFFFD700);
        // Header separator
        context.fill(x, y + 20, x + backgroundWidth, y + 21, 0x88FFD700);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawText(textRenderer, "\u00a76\u00a7lResearch Tree", x + 8, y + 7, 0xFFD700, false);

        // Science points: synced from server via PropertyDelegate
        String sciTxt = "\u00a7bSci: \u00a7f" + handler.getSciencePoints();
        context.drawText(textRenderer, sciTxt,
                x + backgroundWidth - textRenderer.getWidth(sciTxt) - 8, y + 7, 0xFFFFFF, false);

        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private String unlockLabel(ColonistJob job) {
        int cost = ResearchScreenHandler.unlockCost(job);
        if (isUnlocked(job)) return "\u00a7a\u2714 " + job.displayName + " (unlocked)";
        int science = handler.getSciencePoints();
        boolean canAfford = science >= cost;
        String col = canAfford ? "\u00a7e" : "\u00a7c";
        return col + job.displayName + " \u00a77(" + cost + " sci)";
    }

    private String tooltipText(ColonistJob job) {
        if (isUnlocked(job)) return job.displayName + " is already unlocked!";
        int cost = ResearchScreenHandler.unlockCost(job);
        int science = handler.getSciencePoints();
        if (science < cost) return "Need " + (cost - science) + " more science to unlock " + job.displayName + ".";
        return "Click to unlock " + job.displayName + " for " + cost + " science points.";
    }

    private boolean isUnlocked(ColonistJob job) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getServer() == null) return false;
        return ColonyManager.get(mc.getServer())
                .getAllColonies().stream().findFirst()
                .map(c -> c.isJobUnlocked(job))
                .orElse(false);
    }

    private void tryUnlock(ColonistJob job) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getServer() == null) return;
        mc.getServer().execute(() -> {
            Optional<ColonyData> opt = ColonyManager.get(mc.getServer())
                    .getAllColonies().stream().findFirst();
            if (opt.isEmpty()) return;
            ColonyData colony = opt.get();
            int cost = ResearchScreenHandler.unlockCost(job);
            if (cost == 0 || colony.isJobUnlocked(job)) return;
            if (!colony.spendScience(cost)) return;
            colony.unlockJob(job);
            ColonyManager.get(mc.getServer()).markDirty();
            mc.execute(() -> {
                if (mc.player != null) {
                    mc.player.sendMessage(
                        Text.literal("\u00a7aUnlocked \u00a76" + job.displayName
                            + "\u00a7a for \u00a7b" + cost + "\u00a7a science!"), true);
                }
                // Refresh button labels
                clearAndInit();
            });
        });
    }
}
