package cz.wux.colonycraft.item;

import cz.wux.colonycraft.blockentity.JobBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Colony Survey Wand — used to mark a rectangular work area for farming/woodcutting/mining job blocks.
 *
 * Usage (mirrors Colony Survival area selection):
 *   1. Right-click any block → set Corner 1
 *   2. Right-click another block → set Corner 2 (area preview shown)
 *   3. Right-click the Job Block → area assigned to that workstation
 *
 * The colonist assigned to that job will then only search/work inside the defined area.
 */
public class AreaWandItem extends Item {

    /** Per-player selection state: [0] = corner1, [1] = corner2 */
    private static final Map<UUID, BlockPos[]> SELECTIONS = new HashMap<>();

    /** Public accessor for the renderer to read current selection. */
    public static BlockPos[] getSelection(UUID playerId) {
        return SELECTIONS.get(playerId);
    }

    public AreaWandItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld().isClient()) return ActionResult.SUCCESS;

        PlayerEntity player = context.getPlayer();
        if (player == null) return ActionResult.PASS;

        BlockPos clicked = context.getBlockPos();
        var be = context.getWorld().getBlockEntity(clicked);

        // ── Right-clicking a job block ───────────────────────────────────
        if (be instanceof JobBlockEntity jb) {
            BlockPos[] sel = SELECTIONS.get(player.getUuid());
            if (sel != null && sel[0] != null && sel[1] != null) {
                // Both corners set — assign area to this job block
                BlockPos min = new BlockPos(
                    Math.min(sel[0].getX(), sel[1].getX()),
                    Math.min(sel[0].getY(), sel[1].getY()),
                    Math.min(sel[0].getZ(), sel[1].getZ())
                );
                BlockPos max = new BlockPos(
                    Math.max(sel[0].getX(), sel[1].getX()),
                    Math.max(sel[0].getY(), sel[1].getY()),
                    Math.max(sel[0].getZ(), sel[1].getZ())
                );
                jb.setArea(min, max);
                SELECTIONS.remove(player.getUuid());
                int w = max.getX() - min.getX() + 1;
                int d = max.getZ() - min.getZ() + 1;
                player.sendMessage(Text.literal(
                    "§a[" + jb.getJob().displayName + "] §fWork area set: §e" + w + "×" + d +
                    " §7blocks (" + min.getX() + " " + min.getY() + " " + min.getZ() +
                    " → " + max.getX() + " " + max.getY() + " " + max.getZ() + ")"), false);
            } else {
                // Show current area info
                if (jb.hasArea()) {
                    BlockPos mn = jb.getAreaMin(), mx = jb.getAreaMax();
                    int w = mx.getX() - mn.getX() + 1;
                    int d = mx.getZ() - mn.getZ() + 1;
                    player.sendMessage(Text.literal(
                        "§e[" + jb.getJob().displayName + "] §7Current area: §f" + w + "×" + d +
                        " blocks. §7Select corners then right-click here to update."), false);
                } else {
                    player.sendMessage(Text.literal(
                        "§e[" + jb.getJob().displayName + "] §cNo work area defined. " +
                        "§7Select 2 corners with the wand, then right-click this block."), false);
                }
            }
            return ActionResult.SUCCESS;
        }

        // ── Right-clicking any other block — set corner ──────────────────
        BlockPos[] sel = SELECTIONS.computeIfAbsent(player.getUuid(), k -> new BlockPos[2]);

        if (sel[0] == null || (sel[0] != null && sel[1] != null)) {
            // Set corner 1 (or reset if both were already set)
            sel[0] = clicked.toImmutable();
            sel[1] = null;
            player.sendMessage(Text.literal(
                "§a[Wand] Corner 1: §f" + clicked.getX() + " " + clicked.getY() + " " + clicked.getZ() +
                " §7— now select Corner 2"), false);
        } else {
            // Set corner 2
            sel[1] = clicked.toImmutable();
            int w = Math.abs(sel[1].getX() - sel[0].getX()) + 1;
            int h = Math.abs(sel[1].getY() - sel[0].getY()) + 1;
            int d = Math.abs(sel[1].getZ() - sel[0].getZ()) + 1;
            player.sendMessage(Text.literal(
                "§a[Wand] Corner 2: §f" + clicked.getX() + " " + clicked.getY() + " " + clicked.getZ() +
                " §7— Area: §e" + w + "×" + h + "×" + d +
                "§7. Right-click the §ejob block§7 to assign this area."), false);
        }
        return ActionResult.SUCCESS;
    }
}