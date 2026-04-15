package cz.wux.colonycraft.block.job;

import cz.wux.colonycraft.data.ColonistJob;
import net.minecraft.block.AbstractBlock;

public class CowHerderBlock extends JobBlock {
    public CowHerderBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public ColonistJob getJob() {
        return ColonistJob.COW_HERDER;
    }
}
