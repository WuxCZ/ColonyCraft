package cz.wux.colonycraft.client.screen;

import cz.wux.colonycraft.screen.StockpileScreenHandler;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Renders the Stockpile GUI — paginated 540-slot inventory (10 pages × 54 slots).
 */
public class StockpileScreen extends HandledScreen<StockpileScreenHandler> {

    private static final Identifier TEXTURE =
            Identifier.of("minecraft", "textures/gui/container/generic_54.png");

    public StockpileScreen(StockpileScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 222;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        // Page navigation buttons
        addDrawableChild(ButtonWidget.builder(Text.literal("\u25C0"), btn -> {
            handler.setPage(handler.getCurrentPage() - 1);
        }).dimensions(x + 6, y + 4, 14, 12).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("\u25B6"), btn -> {
            handler.setPage(handler.getCurrentPage() + 1);
        }).dimensions(x + backgroundWidth - 20, y + 4, 14, 12).build());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        // Page indicator
        String pageText = "Page " + (handler.getCurrentPage() + 1) + "/" + handler.getTotalPages();
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("\u00a7e" + pageText),
                x + backgroundWidth / 2, y + 5, 0xFFFFFF);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
