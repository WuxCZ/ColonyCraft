package cz.wux.colonycraft.block.job;
import cz.wux.colonycraft.data.ColonistJob;
import net.minecraft.block.AbstractBlock;
public class BuilderBlock extends JobBlock {
    public BuilderBlock(AbstractBlock.Settings settings) { super(settings); }
    @Override public ColonistJob getJob() { return ColonistJob.BUILDER; }
}
