package cz.wux.colonycraft.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class GuidebookScreen extends Screen {

    private static final int BOOK_WIDTH = 280;
    private static final int BOOK_HEIGHT = 200;
    private int currentPage = 0;

    private static final List<Page> PAGES = List.of(
        new Page("\u00a76\u00a7l\u2654 ColonyCraft Guide",
            new String[]{"\u00a77Build, manage and defend your colony","\u00a77against nightly monster raids!","","\u00a7ePress \u00a7b;\u00a7e to open Colony Management.","","\u00a78Your journey starts by crafting a","\u00a76Colony Banner\u00a78 and placing it on","\u00a78flat ground to found your colony."}),
        new Page("\u00a7a\u00a7l\u2756 Step 1: Found Your Colony",
            new String[]{"\u00a7fCraft and place the \u00a72Colony Banner\u00a7f.",""," \u00a7a\u25b8\u00a7f 2 colonists spawn automatically"," \u00a7a\u25b8\u00a7f You receive starter items:","   \u00a7e- Guide Book, Job Assignment Book","   \u00a7e- Colony Survey Wand","   \u00a7e- Bread, Seeds, Saplings","","\u00a78The banner is the heart of your colony!"}),
        new Page("\u00a76\u00a7l\u2756 Step 2: Stockpile",
            new String[]{"\u00a7fCraft and place the \u00a76Stockpile\u00a7f block.",""," \u00a7a\u25b8\u00a7f Colonists deposit resources here"," \u00a7a\u25b8\u00a7f Food keeps your colonists alive"," \u00a7a\u25b8\u00a7f Each colonist eats 1 food / ~5 min",""," \u00a7c\u26a0 No food = colonists starve!"," \u00a7c  Death after ~20 min without food"}),
        new Page("\u00a7b\u00a7l\u2756 Step 3: Job Blocks",
            new String[]{"\u00a7fPlace \u00a7bjob blocks\u00a7f to create workstations:",""," \u00a7a\u2692\u00a7f Woodcutter's Bench \u00a78- chops trees"," \u00a7a\u2692\u00a7f Farmer's Hut \u00a78- grows wheat"," \u00a7a\u2692\u00a7f Miner's Hut \u00a78- mines stone and ore"," \u00a7a\u2692\u00a7f Forester's Hut \u00a78- plants saplings"," \u00a7a\u2692\u00a7f Berry Farm \u00a78- grows berries","","\u00a78Workers auto-assign when they spawn!"}),
        new Page("\u00a7d\u00a7l\u2756 Step 4: Area Wand",
            new String[]{"\u00a7fThe \u00a7dColony Survey Wand\u00a7f defines work","\u00a7fareas, just like Colony Survival!",""," \u00a7e1.\u00a7f Right-click block \u2192 set Corner 1"," \u00a7e2.\u00a7f Right-click another \u2192 set Corner 2"," \u00a7e3.\u00a7f Right-click Job Block \u2192 assign area","","\u00a7aHold the wand to see area outlines!","\u00a78Workers only work inside their area."}),
        new Page("\u00a7e\u00a7l\u2756 Growing Your Colony",
            new String[]{"\u00a7fKeep food \u00a72above 20\u00a7f for growth!",""," \u00a7a\u25b8\u00a7f Pop cap: 2 + days survived (max 50)"," \u00a7a\u25b8\u00a7f Press \u00a7e;\u00a7f \u2192 Recruit (costs 5 food)"," \u00a7a\u25b8\u00a7f Auto-spawn when food is abundant","","\u00a7eTip:\u00a7f Multiple farms = faster growth!","\u00a78Diversify jobs for a strong colony."}),
        new Page("\u00a7c\u00a7l\u2756 Night Raids!",
            new String[]{"\u00a7fEvery night, \u00a7cColony Raiders\u00a7f attack!",""," \u00a7c\u25b8\u00a7f Waves scale with days survived"," \u00a7c\u25b8\u00a7f Build \u00a7cGuard Towers\u00a7f for defense"," \u00a7c\u25b8\u00a7f Guards auto-shoot with bows"," \u00a7c\u25b8\u00a7f Stronger monsters over time","","\u00a7eTip:\u00a7f Light up perimeter with torches!","\u00a78Walls + guards = survival."}),
        new Page("\u00a75\u00a7l\u2756 Processing and Crafting",
            new String[]{"\u00a7fAdvanced blocks process materials:",""," \u00a7e\u2692\u00a7f Stove \u00a78- cook raw food"," \u00a7e\u2692\u00a7f Bloomery \u00a78- smelt iron ore"," \u00a7e\u2692\u00a7f Grindstone \u00a78- grind wheat"," \u00a7e\u2692\u00a7f Fletcher's Bench \u00a78- make arrows"," \u00a7e\u2692\u00a7f Tailor Shop \u00a78- craft armor","","\u00a78Assign colonists to process!"}),
        new Page("\u00a79\u00a7l\u2756 Research and Science",
            new String[]{"\u00a7fPlace a \u00a79Research Desk\u00a7f and assign","\u00a7fa colonist to earn science points.",""," \u00a7b\u25b8\u00a7f Open Research Table to spend points"," \u00a7b\u25b8\u00a7f Unlock new job types and upgrades","","\u00a7eKnowledge is power!","\u00a78Research unlocks better equipment."}),
        new Page("\u00a76\u00a7l\u2756 Controls Summary",
            new String[]{"","\u00a7e ;\u00a7f \u2192 Colony Management Screen"," \u00a7eR-click\u00a7f Job Book \u2192 cycle job type"," \u00a7eR-click\u00a7f colonist w/ book \u2192 assign"," \u00a7eR-click\u00a7f w/ Wand \u2192 set work areas"," \u00a7eR-click\u00a7f this guide \u2192 open this screen","","\u00a7a\u00a7lGood luck, Commander! \u00a76\u2605","\u00a78Build. Grow. Defend. Survive."})
    );

    public GuidebookScreen() {
        super(Text.literal("ColonyCraft Guide"));
    }

    @Override
    protected void init() {
        super.init();
        int cx = (width - BOOK_WIDTH) / 2;
        int cy = (height - BOOK_HEIGHT) / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("\u25c0 Prev"), btn -> {
            if (currentPage > 0) currentPage--;
        }).dimensions(cx + 8, cy + BOOK_HEIGHT - 26, 60, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Next \u25b6"), btn -> {
            if (currentPage < PAGES.size() - 1) currentPage++;
        }).dimensions(cx + BOOK_WIDTH - 68, cy + BOOK_HEIGHT - 26, 60, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7cX"), btn -> {
            close();
        }).dimensions(cx + BOOK_WIDTH - 18, cy + 4, 14, 14).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0xC0101010);
        int cx = (width - BOOK_WIDTH) / 2;
        int cy = (height - BOOK_HEIGHT) / 2;
        ctx.fill(cx, cy, cx + BOOK_WIDTH, cy + BOOK_HEIGHT, 0xFF2A1F14);
        ctx.fill(cx + 4, cy + 4, cx + BOOK_WIDTH - 4, cy + BOOK_HEIGHT - 4, 0xFF3D2E1F);
        ctx.fill(cx, cy, cx + BOOK_WIDTH, cy + 2, 0xFFB8860B);
        ctx.fill(cx, cy + BOOK_HEIGHT - 2, cx + BOOK_WIDTH, cy + BOOK_HEIGHT, 0xFFB8860B);
        ctx.fill(cx, cy, cx + 2, cy + BOOK_HEIGHT, 0xFFB8860B);
        ctx.fill(cx + BOOK_WIDTH - 2, cy, cx + BOOK_WIDTH, cy + BOOK_HEIGHT, 0xFFB8860B);
        ctx.fill(cx + 6, cy + 6, cx + BOOK_WIDTH - 6, cy + 7, 0x44FFD700);
        ctx.fill(cx + 6, cy + BOOK_HEIGHT - 33, cx + BOOK_WIDTH - 6, cy + BOOK_HEIGHT - 32, 0x44FFD700);
        if (currentPage < PAGES.size()) {
            Page page = PAGES.get(currentPage);
            int ty = cy + 12;
            ctx.drawText(textRenderer, page.title, cx + 12, ty, 0xFFD700, true);
            ty += 16;
            for (String line : page.lines) {
                ctx.drawText(textRenderer, line, cx + 12, ty, 0xDDDDDD, false);
                ty += 11;
            }
        }
        String pageInfo = "Page " + (currentPage + 1) + " / " + PAGES.size();
        int piWidth = textRenderer.getWidth(pageInfo);
        ctx.drawText(textRenderer, "\u00a78" + pageInfo, cx + (BOOK_WIDTH - piWidth) / 2, cy + BOOK_HEIGHT - 24, 0x888888, false);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private record Page(String title, String[] lines) {}
}