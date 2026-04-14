package cz.wux.colonycraft.client.render;

import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.util.Identifier;

public class ColonistRenderState extends BipedEntityRenderState {
    public Identifier jobTexture = ColonistEntityRenderer.DEFAULT_TEXTURE;
    public float healthPercent = 1.0f;
    public float maxHealth = 20.0f;
    public float currentHealth = 20.0f;
    public String statusText = "";
    public String jobName = "Colonist";
}