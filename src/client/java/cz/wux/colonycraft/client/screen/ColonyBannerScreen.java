package cz.wux.colonycraft.client.screen;

import cz.wux.colonycraft.screen.ColonyBannerScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

/**
 * Colony Banner "management" screen.
 * Shows colony statistics: colonist count, food, science points and days survived.
 * Data is sent from server via a custom packet (simplified — here we display
 * placeholders that are filled in via the screen handler's property system).
 */
public class ColonyBannerScreen extends HandledScreen<ColonyBannerScreenHandler> {

    public ColonyBannerScreen(ColonyBannerScreenHandler handler,
                              PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth  = 176;
        this.backgroundHeight = 120;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // Draw a simple dark background
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xCC1A1A1A);
        // Border
        context.fill(x, y, x + backgroundWidth, y + 1, 0xFFFFD700);
        context.fill(x, y + backgroundHeight - 1, x + backgroundWidth, y + backgroundHeight, 0xFFFFD700);
        context.fill(x, y, x + 1, y + backgroundHeight, 0xFFFFD700);
        context.fill(x + backgroundWidth - 1, y, x + backgroundWidth, y + backgroundHeight, 0xFFFFD700);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Title
        context.drawText(textRenderer, "§6§lColony Management", x + 8, y + 8, 0xFFFFFF, false);
        context.drawText(textRenderer, "§7Your colony is active.", x + 8, y + 24, 0xAAAAAA, false);
        context.drawText(textRenderer, "§fColonists: §a(check server log)", x + 8, y + 40, 0xFFFFFF, false);
        context.drawText(textRenderer, "§fFood: §e(in stockpile)", x + 8, y + 52, 0xFFFFFF, false);
        context.drawText(textRenderer, "§fScience: §b(research table)", x + 8, y + 64, 0xFFFFFF, false);
        context.drawText(textRenderer, "§7Place job blocks near the banner.", x + 8, y + 82, 0x888888, false);
        context.drawText(textRenderer, "§7Colonists auto-assign to open jobs.", x + 8, y + 92, 0x888888, false);

        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }
}
