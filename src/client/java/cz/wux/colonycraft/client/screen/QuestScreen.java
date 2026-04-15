package cz.wux.colonycraft.client.screen;

import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.data.Quest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.Collection;

public class QuestScreen extends Screen {

    private static final int PANEL_WIDTH  = 320;
    private static final int PANEL_HEIGHT = 260;
    private static final int LINE_HEIGHT  = 28;

    private ColonyData colony;
    private int scrollOffset = 0;

    public QuestScreen() {
        super(Text.literal("Colony Quests"));
    }

    @Override
    protected void init() {
        super.init();
        refreshColony();

        int cx = (width - PANEL_WIDTH) / 2;
        int cy = (height - PANEL_HEIGHT) / 2;

        // Back button
        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a77\u2190 Back"), btn -> {
            MinecraftClient.getInstance().setScreen(new ColonyManagementScreen());
        }).dimensions(cx + 5, cy + PANEL_HEIGHT - 25, 60, 20).build());

        // Scroll buttons
        addDrawableChild(ButtonWidget.builder(Text.literal("\u25B2"), btn -> {
            scrollOffset = Math.max(0, scrollOffset - 2);
        }).dimensions(cx + PANEL_WIDTH - 18, cy + 30, 14, 14).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("\u25BC"), btn -> {
            scrollOffset += 2;
        }).dimensions(cx + PANEL_WIDTH - 18, cy + PANEL_HEIGHT - 42, 14, 14).build());
    }

    private void refreshColony() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getServer() == null) return;
        Collection<ColonyData> colonies = ColonyManager.get(mc.getServer()).getAllColonies();
        colony = colonies.isEmpty() ? null : colonies.iterator().next();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        if (colony == null) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("\u00a7cNo colony found. Place a Colony Banner first!"),
                    width / 2, height / 2, 0xFFFF5555);
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        ServerWorld world = mc.getServer() != null ? mc.getServer().getWorld(World.OVERWORLD) : null;
        if (world == null) return;

        int cx = (width - PANEL_WIDTH) / 2;
        int cy = (height - PANEL_HEIGHT) / 2;

        // Background
        ctx.fill(cx, cy, cx + PANEL_WIDTH, cy + PANEL_HEIGHT, 0xDD1A1A2E);
        ctx.fill(cx, cy, cx + PANEL_WIDTH, cy + 24, 0xDD2A2A4E);

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("\u00a76\u00a7l\u2756 Colony Quests"),
                cx + PANEL_WIDTH / 2, cy + 7, 0xFFFFD700);

        // Quest count
        int completed = colony.getCompletedQuests().size();
        int total = Quest.values().length;
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("\u00a77" + completed + "/" + total + " completed"),
                cx + PANEL_WIDTH - 110, cy + 9, 0xFFAAAAAA);

        // Quest list
        int listY = cy + 30;
        int maxVisible = (PANEL_HEIGHT - 60) / LINE_HEIGHT;
        Quest[] quests = Quest.values();

        for (int i = scrollOffset; i < quests.length && i < scrollOffset + maxVisible; i++) {
            Quest quest = quests[i];
            int qY = listY + (i - scrollOffset) * LINE_HEIGHT;

            boolean isCompleted = colony.isQuestCompleted(quest.name());
            boolean isUnlocked = quest.isUnlocked(colony);

            if (isCompleted) {
                // Completed quest — green
                ctx.fill(cx + 4, qY, cx + PANEL_WIDTH - 4, qY + LINE_HEIGHT - 2, 0x44005500);
                ctx.drawTextWithShadow(textRenderer,
                        Text.literal("\u00a7a\u2714 " + quest.displayName),
                        cx + 8, qY + 2, 0xFF55FF55);
                ctx.drawTextWithShadow(textRenderer,
                        Text.literal("\u00a72" + quest.description),
                        cx + 8, qY + 13, 0xFF228B22);
            } else if (!isUnlocked) {
                // Locked quest — gray
                ctx.fill(cx + 4, qY, cx + PANEL_WIDTH - 4, qY + LINE_HEIGHT - 2, 0x33333333);
                ctx.drawTextWithShadow(textRenderer,
                        Text.literal("\u00a78\u274C " + quest.displayName),
                        cx + 8, qY + 2, 0xFF888888);
                ctx.drawTextWithShadow(textRenderer,
                        Text.literal("\u00a78Requires: " + quest.prerequisite),
                        cx + 8, qY + 13, 0xFF666666);
            } else {
                // Active quest — yellow with progress
                ctx.fill(cx + 4, qY, cx + PANEL_WIDTH - 4, qY + LINE_HEIGHT - 2, 0x44554400);
                ctx.drawTextWithShadow(textRenderer,
                        Text.literal("\u00a7e\u2726 " + quest.displayName),
                        cx + 8, qY + 2, 0xFFFFFF55);

                int progress = quest.getProgress(colony, world);
                int clamped = Math.min(progress, quest.targetValue);
                String progressText = quest.description + " (" + clamped + "/" + quest.targetValue + ")";
                ctx.drawTextWithShadow(textRenderer,
                        Text.literal("\u00a77" + progressText),
                        cx + 8, qY + 13, 0xFFCCCCCC);

                // Progress bar
                int barX = cx + PANEL_WIDTH - 80;
                int barW = 70;
                int barY2 = qY + 4;
                ctx.fill(barX, barY2, barX + barW, barY2 + 6, 0xFF333333);
                int fillW = (int) ((float) clamped / quest.targetValue * barW);
                ctx.fill(barX, barY2, barX + fillW, barY2 + 6, 0xFFFFAA00);

                // Reward indicator
                if (quest.scienceReward > 0) {
                    ctx.drawTextWithShadow(textRenderer,
                            Text.literal("\u00a7b+" + quest.scienceReward + "\u2605"),
                            barX, barY2 + 8, 0xFF55FFFF);
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, Quest.values().length - (PANEL_HEIGHT - 60) / LINE_HEIGHT);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) verticalAmount));
        return true;
    }
}
