package cz.wux.colonycraft.client.render;

import net.minecraft.client.render.entity.state.BipedEntityRenderState;

public class GuardRenderState extends BipedEntityRenderState {
    public float healthPercent = 1.0f;
    public float maxHealth = 30.0f;
    public float currentHealth = 30.0f;
    public String statusText = "Patrolling";
    public String jobName = "Guard";
    public boolean isBowGuard = false;
}
