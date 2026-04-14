package cz.wux.colonycraft.client.screen;

import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import cz.wux.colonycraft.screen.ColonyBannerScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import java.util.Optional;

/**
 * Colony Banner management screen.
 * Shows live colony statistics by querying the integrated server directly
 * (singleplayer / LAN host). Data is refreshed every render frame.
 */
public class ColonyBannerScreen extends HandledScreen<ColonyBannerScreenHandler> {

    public ColonyBannerScreen(ColonyBannerScreenHandler handler,
                              PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth  = 200;
        this.backgroundHeight = 140;
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        playerInventoryTitleY = Integer.MAX_VALUE; // hide "Inventory" label
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xEE1A1A2E);
        // Gold border
        context.fill(x,     y,                    x + backgroundWidth, y + 1,                    0xFFFFD700);
        context.fill(x,     y + backgroundHeight - 1, x + backgroundWidth, y + backgroundHeight, 0xFFFFD700);
        context.fill(x,     y,                    x + 1,               y + backgroundHeight,     0xFFFFD700);
        context.fill(x + backgroundWidth - 1, y,  x + backgroundWidth, y + backgroundHeight,     0xFFFFD700);
        // Header divider
        context.fill(x, y + 20, x + backgroundWidth, y + 21, 0x88FFD700);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Title
        context.drawText(textRenderer, "\u00a76\u00a7lColony Management", x + 8, y + 7, 0xFFD700, false);

        // Try singleplayer / LAN-host server access for live data
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getServer() != null) {
            Optional<ColonyData> colonyOpt =
                    ColonyManager.get(mc.getServer()).getAllColonies().stream().findFirst();
            if (colonyOpt.isPresent()) {
                ColonyData c = colonyOpt.get();
                int lx = x + 8, ly = y + 28;
                context.drawText(textRenderer, "\u00a77Owner:      \u00a7f" + c.getOwnerName(),          lx, ly,      0xFFFFFF, false);
                context.drawText(textRenderer, "\u00a7aColonists:  \u00a7f" + c.getColonistCount() + " \u00a77/ " + c.getPopulationCap(), lx, ly + 13, 0xFFFFFF, false);
                context.drawText(textRenderer, "\u00a7eFood:       \u00a7f" + c.getFoodUnits() + " units",  lx, ly + 26, 0xFFFFFF, false);
                context.drawText(textRenderer, "\u00a7bScience:    \u00a7f" + c.getSciencePoints() + " pts", lx, ly + 39, 0xFFFFFF, false);
                context.drawText(textRenderer, "\u00a7cDay:        \u00a7f" + c.getDaysSurvived(),          lx, ly + 52, 0xFFFFFF, false);
                context.drawText(textRenderer, "\u00a77Tip: \u00a7fPlace job blocks within 16 blocks of the banner.", x + 8, y + backgroundHeight - 16, 0x888888, false);
                return;
            }
        }
        // Fallback (multiplayer or no colony yet)
        context.drawText(textRenderer, "\u00a77No colony data available.", x + 8, y + 32, 0xAAAAAA, false);
        context.drawText(textRenderer, "\u00a77Place the banner to found a colony.", x + 8, y + 46, 0x888888, false);

        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
