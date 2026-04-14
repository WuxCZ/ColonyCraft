package cz.wux.colonycraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;

/**
 * ColonyCraft Guidebook – right-click to display the tutorial guide in chat.
 */
public class GuidebookItem extends Item {

    private static final List<String[]> PAGES = List.of(
        new String[]{
            "§6§l=== ColonyCraft Guide ===",
            "§7Build and defend a colony against nightly raids!"
        },
        new String[]{
            "§a§lStep 1: Found Your Colony",
            "§fPlace the §2Colony Banner§f on flat ground.",
            "§f2 colonists spawn immediately.",
            "§fYou also receive bread, seeds & saplings."
        },
        new String[]{
            "§6§lStep 2: Place a Stockpile",
            "§fPlace the §6Stockpile§f near your banner.",
            "§fColonists deposit all resources here.",
            "§cFood in the stockpile keeps colonists alive!"
        },
        new String[]{
            "§b§lStep 3: Assign Jobs",
            "§fPlace §bJob Blocks§f near trees, farmland or stone:",
            "§a  • Woodcutter's Bench§f – chops trees",
            "§a  • Farmer's Hut§f – harvests wheat",
            "§a  • Miner's Hut§f – mines stone/ore",
            "§a  • Forester's Hut§f – plants saplings"
        },
        new String[]{
            "§d§lStep 4: Use the Job Book",
            "§fHold the §dJob Assignment Book§f,",
            "§fright-click it to select a job,",
            "§fthen right-click a §dcolonist§f to assign them."
        },
        new String[]{
            "§e§lGrowing Your Colony",
            "§fKeep food §2> 20 units§f and new colonists spawn!",
            "§7Population cap: 4 + (food ÷ 10), max 50.",
            "§fMore job blocks = more food & colonists."
        },
        new String[]{
            "§c§lNight Raids!",
            "§fEvery night, §cColony Raiders§f attack!",
            "§fWaves get harder each in-game day.",
            "§fBuild a §cGuard Tower§f to defend your colony."
        },
        new String[]{
            "§5§lResearch",
            "§fPlace a §5Research Table§f and assign a Researcher.",
            "§fResearch unlocks new buildings & abilities.",
            "§7Good luck, Commander!"
        }
    );

    private int currentPage = 0;

    public GuidebookItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient() && user instanceof ServerPlayerEntity sp) {
            // Print the current page then advance
            String[] page = PAGES.get(currentPage);
            sp.sendMessage(Text.literal(""), false);
            for (String line : page) {
                sp.sendMessage(Text.literal(line), false);
            }
            int total = PAGES.size();
            sp.sendMessage(Text.literal("§8[Page " + (currentPage + 1) + "/" + total + " — right-click for next]"), false);
            currentPage = (currentPage + 1) % total;
        }
        return ActionResult.SUCCESS;
    }
}

