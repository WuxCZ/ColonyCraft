package cz.wux.colonycraft.screen;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;

import java.util.UUID;

/**
 * Screen handler for the Research Tree GUI.
 * Syncs sciencePoints (idx 0) and daysSurvived (idx 1) to the client.
 */
public class ResearchScreenHandler extends ScreenHandler {

    /** All jobs that must be researched (not free from the start). */
    public static final ColonistJob[] UNLOCKABLE_JOBS = {
        ColonistJob.TANNER,         ColonistJob.TAILOR,        ColonistJob.FLETCHER,
        ColonistJob.STONEMASON,     ColonistJob.GRINDER,       ColonistJob.POTTER,
        ColonistJob.BLACKSMITH,     ColonistJob.SMELTER,       ColonistJob.ALCHEMIST,
        ColonistJob.GLASSBLOWER,    ColonistJob.RESEARCHER,    ColonistJob.BEEKEEPER,
        ColonistJob.CHICKEN_FARMER, ColonistJob.GUARD_CROSSBOW, ColonistJob.GUARD_MUSKET
    };

    private final PropertyDelegate props;
    private UUID colonyId;

    /** Client-side constructor (no delegate; server will sync values). */
    public ResearchScreenHandler(int syncId, PlayerInventory playerInv) {
        super(ModScreenHandlers.RESEARCH, syncId);
        this.props = new ArrayPropertyDelegate(2);
        addProperties(this.props);
    }

    /** Server-side constructor. */
    public ResearchScreenHandler(int syncId, PlayerInventory playerInv,
                                 UUID colonyId, PropertyDelegate delegate) {
        super(ModScreenHandlers.RESEARCH, syncId);
        this.colonyId = colonyId;
        this.props    = delegate;
        addProperties(delegate);
    }

    public int getSciencePoints() { return props.get(0); }
    public int getDaysSurvived()  { return props.get(1); }

    @Override
    public boolean canUse(PlayerEntity player) { return true; }

    @Override
    public net.minecraft.item.ItemStack quickMove(PlayerEntity player, int slot) {
        return net.minecraft.item.ItemStack.EMPTY;
    }

    /** Cost in science points to unlock the given job. Returns 0 if already free. */
    public static int unlockCost(ColonistJob job) {
        return switch (job) {
            case TANNER, TAILOR, COMPOSTER     -> 20;
            case STONEMASON, GRINDER, POTTER   -> 30;
            case BLACKSMITH, SMELTER           -> 40;
            case ALCHEMIST, GLASSBLOWER        -> 60;
            case RESEARCHER                    -> 50;
            case BEEKEEPER, CHICKEN_FARMER     -> 25;
            case GUARD_CROSSBOW                -> 80;
            case GUARD_MUSKET                  -> 150;
            case FLETCHER                      -> 35;
            default -> 0;
        };
    }
}
