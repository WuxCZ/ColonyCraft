package cz.wux.colonycraft.entity.goal;

import cz.wux.colonycraft.blockentity.StockpileBlockEntity;
import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.entity.ColonistEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

/**
 * Core work goal: colonist path-finds to its job block, waits for the work
 * cooldown, performs one production cycle, then deposits output into the
 * stockpile.
 *
 * <p>Each {@link ColonistJob} has a hardcoded recipe that mirrors Colony
 * Survival's recipe JSON files.</p>
 */
public class WorkAtJobGoal extends Goal {

    private final ColonistEntity colonist;
    private static final int WORK_TICKS = 60; // one production cycle = 3 seconds

    // Simple record used for recipes
    private record Recipe(Item[] inputs, int[] inputAmounts, Item[] outputs, int[] outputAmounts) {}

    /** Maps job → production recipe (mirrors Colony Survival data). */
    private static final Map<ColonistJob, Recipe> RECIPES = new HashMap<>();

    static {
        // Woodcutter: 1 log → 4 planks
        RECIPES.put(ColonistJob.WOODCUTTER, new Recipe(
                new Item[]{Items.OAK_LOG}, new int[]{1},
                new Item[]{Items.OAK_PLANKS}, new int[]{4}));

        // Forester: plants saplings naturally (no stockpile IO — handled separately)
        RECIPES.put(ColonistJob.FORESTER, new Recipe(
                new Item[]{Items.OAK_SAPLING}, new int[]{1},
                new Item[]{Items.OAK_LOG}, new int[]{3}));

        // Miner: stone → cobblestone + chance of coal/iron
        RECIPES.put(ColonistJob.MINER, new Recipe(
                new Item[]{}, new int[]{},
                new Item[]{Items.COBBLESTONE, Items.COAL}, new int[]{4, 1}));

        // Farmer: seeds → wheat
        RECIPES.put(ColonistJob.FARMER, new Recipe(
                new Item[]{Items.WHEAT_SEEDS}, new int[]{1},
                new Item[]{Items.WHEAT}, new int[]{3}));

        // Berry Farmer: (no input) → sweet berries
        RECIPES.put(ColonistJob.BERRY_FARMER, new Recipe(
                new Item[]{}, new int[]{},
                new Item[]{Items.SWEET_BERRIES}, new int[]{3}));

        // Fisherman: (no input, must be near water) → raw cod
        RECIPES.put(ColonistJob.FISHERMAN, new Recipe(
                new Item[]{}, new int[]{},
                new Item[]{Items.COD}, new int[]{2}));

        // Water Gatherer: (bucket) → water bucket
        RECIPES.put(ColonistJob.WATER_GATHERER, new Recipe(
                new Item[]{Items.BUCKET}, new int[]{1},
                new Item[]{Items.WATER_BUCKET}, new int[]{1}));

        // Cook: raw cod + 1 stick (firewood) → cooked cod
        RECIPES.put(ColonistJob.COOK, new Recipe(
                new Item[]{Items.COD, Items.STICK}, new int[]{1, 1},
                new Item[]{Items.COOKED_COD}, new int[]{1}));

        // Smelter: raw iron → iron ingot (needs coal)
        RECIPES.put(ColonistJob.SMELTER, new Recipe(
                new Item[]{Items.RAW_IRON, Items.COAL}, new int[]{2, 1},
                new Item[]{Items.IRON_INGOT}, new int[]{2}));

        // Blacksmith: iron ingot → iron sword / iron pickaxe recipe via stockpile
        RECIPES.put(ColonistJob.BLACKSMITH, new Recipe(
                new Item[]{Items.IRON_INGOT}, new int[]{3},
                new Item[]{Items.IRON_INGOT}, new int[]{3})); // passthrough - handled as smelting

        // Tanner: leather → leather armor piece
        RECIPES.put(ColonistJob.TANNER, new Recipe(
                new Item[]{Items.LEATHER}, new int[]{4},
                new Item[]{Items.LEATHER_HELMET}, new int[]{1}));

        // Tailor: string → lead (rope equivalent)
        RECIPES.put(ColonistJob.TAILOR, new Recipe(
                new Item[]{Items.STRING}, new int[]{4},
                new Item[]{Items.LEAD}, new int[]{1}));

        // Fletcher: planks + feather → arrow
        RECIPES.put(ColonistJob.FLETCHER, new Recipe(
                new Item[]{Items.OAK_PLANKS, Items.FEATHER}, new int[]{2, 1},
                new Item[]{Items.ARROW}, new int[]{4}));

        // Stonemason: cobblestone → stone bricks
        RECIPES.put(ColonistJob.STONEMASON, new Recipe(
                new Item[]{Items.COBBLESTONE}, new int[]{4},
                new Item[]{Items.STONE_BRICKS}, new int[]{4}));

        // Composter: organics → bone meal
        RECIPES.put(ColonistJob.COMPOSTER, new Recipe(
                new Item[]{Items.WHEAT}, new int[]{8},
                new Item[]{Items.BONE_MEAL}, new int[]{2}));

        // Grinder: wheat → flour (bread, no pottery yet)
        RECIPES.put(ColonistJob.GRINDER, new Recipe(
                new Item[]{Items.WHEAT}, new int[]{2},
                new Item[]{Items.BREAD}, new int[]{1}));

        // Alchemist: ingredients → potion base (sugar = default)
        RECIPES.put(ColonistJob.ALCHEMIST, new Recipe(
                new Item[]{Items.SUGAR, Items.GLASS_BOTTLE}, new int[]{1, 1},
                new Item[]{Items.AWKWARD_POTION}, new int[]{1}));

        // Glassblower: sand + coal → glass panes
        RECIPES.put(ColonistJob.GLASSBLOWER, new Recipe(
                new Item[]{Items.SAND, Items.COAL}, new int[]{4, 1},
                new Item[]{Items.GLASS_PANE}, new int[]{8}));

        // Beekeeper: (no input) → honey bottle
        RECIPES.put(ColonistJob.BEEKEEPER, new Recipe(
                new Item[]{Items.GLASS_BOTTLE}, new int[]{1},
                new Item[]{Items.HONEY_BOTTLE}, new int[]{1}));

        // Chicken Farmer: seeds → egg + feather
        RECIPES.put(ColonistJob.CHICKEN_FARMER, new Recipe(
                new Item[]{Items.WHEAT_SEEDS}, new int[]{2},
                new Item[]{Items.EGG, Items.FEATHER}, new int[]{2, 1}));

        // Researcher: generates science (outputs to ResearchTableBlockEntity)
        RECIPES.put(ColonistJob.RESEARCHER, new Recipe(
                new Item[]{Items.PAPER}, new int[]{1},
                new Item[]{}, new int[]{})); // science handled separately

        // Potter: clay → brick + pot
        RECIPES.put(ColonistJob.POTTER, new Recipe(
                new Item[]{Items.CLAY_BALL}, new int[]{4},
                new Item[]{Items.BRICK, Items.FLOWER_POT}, new int[]{4, 1}));
    }

    public WorkAtJobGoal(ColonistEntity colonist) {
        this.colonist = colonist;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return colonist.getColonistJob() != ColonistJob.UNEMPLOYED
                && !colonist.getColonistJob().isGuard()
                && !colonist.isWorkCoolingDown()
                && !colonist.isHungry()
                && colonist.getJobBlockPos() != null;
    }

    @Override
    public void start() {
        BlockPos target = colonist.getJobBlockPos();
        if (target != null) {
            colonist.getNavigation().startMovingTo(
                    target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 0.9);
        }
    }

    @Override
    public void tick() {
        BlockPos jobPos = colonist.getJobBlockPos();
        if (jobPos == null) return;

        // Check if close enough to work
        double distSq = colonist.getPos()
                .squaredDistanceTo(jobPos.getX() + 0.5, jobPos.getY() + 0.5, jobPos.getZ() + 0.5);

        if (distSq <= 6.0) {
            // At workstation — perform one production cycle
            performWork();
            colonist.startWorkCooldown(WORK_TICKS);
        }
    }

    @Override
    public boolean shouldContinue() {
        return !colonist.isWorkCoolingDown() && !colonist.isHungry()
                && colonist.getJobBlockPos() != null;
    }

    private void performWork() {
        ColonistJob job = colonist.getColonistJob();
        Recipe recipe   = RECIPES.get(job);
        if (recipe == null) return;

        Optional<StockpileBlockEntity> stockpileOpt = colonist.getStockpile();
        if (stockpileOpt.isEmpty()) return;
        StockpileBlockEntity stockpile = stockpileOpt.get();

        // Check inputs are available
        for (int i = 0; i < recipe.inputs().length; i++) {
            if (!stockpile.hasItem(recipe.inputs()[i], recipe.inputAmounts()[i])) return;
        }

        // Consume inputs
        for (int i = 0; i < recipe.inputs().length; i++) {
            stockpile.withdrawItem(recipe.inputs()[i], recipe.inputAmounts()[i]);
        }

        // Deposit outputs
        for (int i = 0; i < recipe.outputs().length; i++) {
            ItemStack output = new ItemStack(recipe.outputs()[i], recipe.outputAmounts()[i]);
            stockpile.insertItem(output);
        }

        // Special: researcher adds science points
        if (job == ColonistJob.RESEARCHER) {
            BlockPos jobPos = colonist.getJobBlockPos();
            if (jobPos != null) {
                var be = colonist.getWorld().getBlockEntity(jobPos);
                if (be instanceof cz.wux.colonycraft.blockentity.ResearchTableBlockEntity rt) {
                    rt.addScience(5);
                }
            }
        }
    }
}
