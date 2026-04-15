package cz.wux.colonycraft.client.render;

import net.minecraft.client.render.entity.state.IllagerEntityRenderState;
import net.minecraft.util.Identifier;

public class ColonistRenderState extends IllagerEntityRenderState {
    public Identifier jobTexture = ColonistEntityRenderer.DEFAULT_TEXTURE;
    public float healthPercent = 1.0f;
    public float maxHealth = 20.0f;
    public float currentHealth = 20.0f;
    public String statusText = "";
    public String jobName = "Colonist";
    public String colonistName = "";
}