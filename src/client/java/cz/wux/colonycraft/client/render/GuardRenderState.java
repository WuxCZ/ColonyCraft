package cz.wux.colonycraft.client.render;

import net.minecraft.client.render.entity.state.IllagerEntityRenderState;

public class GuardRenderState extends IllagerEntityRenderState {
    public float healthPercent = 1.0f;
    public float maxHealth = 30.0f;
    public float currentHealth = 30.0f;
    public String statusText = "Patrolling";
    public String jobName = "Guard";
    public String guardName = "";
    public boolean isBowGuard = false;
}
