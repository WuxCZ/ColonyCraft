package cz.wux.colonycraft.screen;

import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;

import java.util.UUID;

public class ResearchScreenHandler extends ScreenHandler {

    public static final ColonistJob[] UNLOCKABLE_JOBS = {
        ColonistJob.TANNER,         ColonistJob.TAILOR,        ColonistJob.FLETCHER,
        ColonistJob.STONEMASON,     ColonistJob.GRINDER,       ColonistJob.POTTER,
        ColonistJob.BLACKSMITH,     ColonistJob.SMELTER,       ColonistJob.ALCHEMIST,
        ColonistJob.GLASSBLOWER,    ColonistJob.RESEARCHER,    ColonistJob.BEEKEEPER,
        ColonistJob.CHICKEN_FARMER, ColonistJob.GUARD_SWORD,   ColonistJob.GUARD_BOW
    };

    private final PropertyDelegate props;
    private UUID colonyId;

    public ResearchScreenHandler(int syncId, PlayerInventory playerInv) {
        super(ModScreenHandlers.RESEARCH, syncId);
        this.props = new ArrayPropertyDelegate(2);
        addProperties(this.props);
    }

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

    public static int unlockCost(ColonistJob job) {
        return switch (job) {
            case TANNER, TAILOR, COMPOSTER     -> 20;
            case STONEMASON, GRINDER, POTTER   -> 30;
            case BLACKSMITH, SMELTER           -> 40;
            case ALCHEMIST, GLASSBLOWER        -> 60;
            case RESEARCHER                    -> 50;
            case BEEKEEPER, CHICKEN_FARMER     -> 25;
            case GUARD_SWORD, GUARD_BOW          -> 80;
            case FLETCHER                      -> 35;
            default -> 0;
        };
    }
}