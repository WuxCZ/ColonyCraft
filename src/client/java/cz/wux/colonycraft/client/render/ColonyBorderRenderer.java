package cz.wux.colonycraft.client.render;

import cz.wux.colonycraft.data.ColonyData;
import cz.wux.colonycraft.data.ColonyManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

/**
 * Renders colony border as purple particles in a continuous line around the colony perimeter.
 * Called from ClientTickEvents every few ticks.
 */
public class ColonyBorderRenderer {

    private static final int BORDER_RADIUS = 32;
    private static int tickCounter = 0;
    /** Purple color for dust particles (RGB 0.6, 0.2, 0.9) */
    private static final DustParticleEffect PURPLE_DUST =
            new DustParticleEffect(0x9933FF, 1.0f);

    public static void tick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null || mc.getServer() == null) return;
        tickCounter++;
        if (tickCounter < 10) return; // every 10 ticks = 0.5s
        tickCounter = 0;

        Collection<ColonyData> colonies = ColonyManager.get(mc.getServer()).getAllColonies();
        if (colonies.isEmpty()) return;
        ColonyData colony = colonies.iterator().next();
        BlockPos banner = colony.getBannerPos();
        if (banner == null) return;

        double px = mc.player.getX(), pz = mc.player.getZ();
        double bx = banner.getX(), bz = banner.getZ();
        // Only render if player is within 80 blocks
        if (Math.abs(px - bx) > 80 || Math.abs(pz - bz) > 80) return;

        double by = banner.getY() + 1;
        int r = BORDER_RADIUS;

        // Spawn particles along edges the player can see (closest 2 edges)
        // North edge (z = bz - r)
        if (Math.abs(pz - (bz - r)) < 48) {
            spawnEdge(mc, bx - r, by, bz - r, bx + r, by, bz - r);
        }
        // South edge (z = bz + r)
        if (Math.abs(pz - (bz + r)) < 48) {
            spawnEdge(mc, bx - r, by, bz + r, bx + r, by, bz + r);
        }
        // West edge (x = bx - r)
        if (Math.abs(px - (bx - r)) < 48) {
            spawnEdge(mc, bx - r, by, bz - r, bx - r, by, bz + r);
        }
        // East edge (x = bx + r)
        if (Math.abs(px - (bx + r)) < 48) {
            spawnEdge(mc, bx + r, by, bz - r, bx + r, by, bz + r);
        }
    }

    private static void spawnEdge(MinecraftClient mc, double x1, double y1, double z1,
                                  double x2, double y2, double z2) {
        double dx = x2 - x1, dz = z2 - z1;
        double len = Math.sqrt(dx * dx + dz * dz);
        int particles = (int)(len / 0.3); // dense particles for fully continuous line
        for (int i = 0; i <= particles; i++) {
            double t = (double) i / Math.max(1, particles);
            double x = x1 + dx * t;
            double z = z1 + dz * t;
            mc.world.addParticleClient(PURPLE_DUST, x, y1 + 0.5, z, 0.0, 0.0, 0.0);
        }
    }
}