package cz.wux.colonycraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

/**
 * Chisel — right-click blocks to cycle through decorative variants.
 * Works on vanilla stone/brick/wood blocks, cycling through related styles.
 */
public class ChiselItem extends Item {

    /** Maps each block in a variant chain to the next block in that chain. */
    private static final Map<Block, Block> CHISEL_MAP = new LinkedHashMap<>();

    static {
        // Stone variants
        chain(Blocks.STONE, Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS,
              Blocks.CRACKED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS);

        // Cobblestone variants
        chain(Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE);

        // Deepslate variants
        chain(Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE, Blocks.POLISHED_DEEPSLATE,
              Blocks.DEEPSLATE_BRICKS, Blocks.CRACKED_DEEPSLATE_BRICKS,
              Blocks.DEEPSLATE_TILES, Blocks.CRACKED_DEEPSLATE_TILES, Blocks.CHISELED_DEEPSLATE);

        // Sandstone variants
        chain(Blocks.SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.SMOOTH_SANDSTONE);
        chain(Blocks.RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE);

        // Quartz variants
        chain(Blocks.QUARTZ_BLOCK, Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_BRICKS, Blocks.SMOOTH_QUARTZ);

        // Prismarine variants
        chain(Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.DARK_PRISMARINE);

        // Nether brick variants
        chain(Blocks.NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS, Blocks.CHISELED_NETHER_BRICKS, Blocks.RED_NETHER_BRICKS);

        // Blackstone variants
        chain(Blocks.BLACKSTONE, Blocks.POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS,
              Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.CHISELED_POLISHED_BLACKSTONE,
              Blocks.GILDED_BLACKSTONE);

        // End stone
        chain(Blocks.END_STONE, Blocks.END_STONE_BRICKS);

        // Copper
        chain(Blocks.COPPER_BLOCK, Blocks.CUT_COPPER, Blocks.CHISELED_COPPER);
        chain(Blocks.EXPOSED_COPPER, Blocks.EXPOSED_CUT_COPPER, Blocks.EXPOSED_CHISELED_COPPER);
        chain(Blocks.WEATHERED_COPPER, Blocks.WEATHERED_CUT_COPPER, Blocks.WEATHERED_CHISELED_COPPER);
        chain(Blocks.OXIDIZED_COPPER, Blocks.OXIDIZED_CUT_COPPER, Blocks.OXIDIZED_CHISELED_COPPER);

        // Purpur
        chain(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR);

        // Andesite/Granite/Diorite
        chain(Blocks.ANDESITE, Blocks.POLISHED_ANDESITE);
        chain(Blocks.GRANITE, Blocks.POLISHED_GRANITE);
        chain(Blocks.DIORITE, Blocks.POLISHED_DIORITE);

        // Mud bricks
        chain(Blocks.PACKED_MUD, Blocks.MUD_BRICKS);

        // Tuff
        chain(Blocks.TUFF, Blocks.POLISHED_TUFF, Blocks.TUFF_BRICKS, Blocks.CHISELED_TUFF_BRICKS, Blocks.CHISELED_TUFF);
    }

    /** Register a circular chain: A -> B -> C -> ... -> A */
    private static void chain(Block... blocks) {
        for (int i = 0; i < blocks.length; i++) {
            CHISEL_MAP.put(blocks[i], blocks[(i + 1) % blocks.length]);
        }
    }

    public ChiselItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block nextBlock = CHISEL_MAP.get(state.getBlock());

        if (nextBlock == null) return ActionResult.PASS;

        if (!world.isClient()) {
            world.setBlockState(pos, nextBlock.getDefaultState());
            world.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT,
                    SoundCategory.BLOCKS, 1.0f, 1.0f);

            PlayerEntity player = context.getPlayer();
            if (player != null) {
                player.sendMessage(Text.literal("\u00a77\u2692 " + nextBlock.getName().getString()), true);
            }
        }

        return ActionResult.SUCCESS;
    }
}
