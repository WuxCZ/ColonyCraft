package cz.wux.colonycraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * ColonyCraft Guidebook - right-click to open the guidebook GUI screen.
 * The screen opener is set from the client mod initializer to avoid
 * referencing client classes from common code.
 */
public class GuidebookItem extends Item {

    /** Set from ColonyCraftClient to open the GuidebookScreen on the client. */
    public static Runnable clientScreenOpener = null;

    public GuidebookItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient() && clientScreenOpener != null) {
            clientScreenOpener.run();
        }
        return ActionResult.SUCCESS;
    }
}
