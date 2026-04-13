package cz.wux.colonycraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import cz.wux.colonycraft.blockentity.JobBlockEntity;

/**
 * Job Assignment Book — right-click any Job Block while holding this to cycle
 * through available jobs. Useful for manually re-assigning colonists.
 */
public class JobAssignmentBook extends Item {

    public JobAssignmentBook(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) return ActionResult.SUCCESS;

        HitResult hit = user.raycast(5.0, 0, false);
        if (hit instanceof BlockHitResult blockHit) {
            var be = world.getBlockEntity(blockHit.getBlockPos());
            if (be instanceof JobBlockEntity job) {
                user.sendMessage(Text.of("§e[ColonyCraft] §fJob block: §a" +
                        job.getJob().displayName +
                        (job.hasAssignedColonist() ? " §7(assigned)" : " §7(unassigned)")), false);
                return ActionResult.CONSUME;
            }
        }
        return ActionResult.PASS;
    }
}
