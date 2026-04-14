package cz.wux.colonycraft.block.job;
import cz.wux.colonycraft.data.ColonistJob;
import net.minecraft.block.AbstractBlock;
public class GuardTowerBlock extends JobBlock {
    public GuardTowerBlock(AbstractBlock.Settings settings) { super(settings); }
    @Override public ColonistJob getJob() { return ColonistJob.GUARD_SWORD; }
}