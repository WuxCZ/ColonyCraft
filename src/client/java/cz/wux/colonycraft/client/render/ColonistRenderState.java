package cz.wux.colonycraft.client.render;

import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.util.Identifier;

/**
 * Per-frame render state for colonists. Extends biped state with a job-specific
 * texture so each job type looks visually distinct.
 */
public class ColonistRenderState extends BipedEntityRenderState {
    /** Texture identifier resolved on the server thread, carried to render thread. */
    public Identifier jobTexture = ColonistEntityRenderer.DEFAULT_TEXTURE;
}
