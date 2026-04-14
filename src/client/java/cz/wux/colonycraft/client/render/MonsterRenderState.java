package cz.wux.colonycraft.client.render;

import net.minecraft.client.render.entity.state.BipedEntityRenderState;

public class MonsterRenderState extends BipedEntityRenderState {
    public float healthPercent = 1.0f;
    public float maxHealth = 20.0f;
    public float currentHealth = 20.0f;
    public String statusText = "Attacking";
}
