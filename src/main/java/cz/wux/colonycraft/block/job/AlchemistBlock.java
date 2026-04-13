package cz.wux.colonycraft.block.job;
import cz.wux.colonycraft.data.ColonistJob;
import net.minecraft.block.AbstractBlock;
public class AlchemistBlock extends JobBlock {
    public AlchemistBlock(AbstractBlock.Settings settings) { super(settings); }
    @Override public ColonistJob getJob() { return ColonistJob.ALCHEMIST; }
}
