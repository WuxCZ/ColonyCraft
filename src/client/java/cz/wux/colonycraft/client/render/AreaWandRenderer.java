package cz.wux.colonycraft.client.render;

import cz.wux.colonycraft.blockentity.JobBlockEntity;
import cz.wux.colonycraft.data.ColonistJob;
import cz.wux.colonycraft.item.AreaWandItem;
import cz.wux.colonycraft.registry.ModItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class AreaWandRenderer {

    private static int tickCounter = 0;

    public static void tick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) return;
        boolean holdingWand = mc.player.getMainHandStack().isOf(ModItems.AREA_WAND)
                           || mc.player.getOffHandStack().isOf(ModItems.AREA_WAND);
        tickCounter++;
        if (tickCounter < 2) return;  // every 2 ticks for smoother borders
        tickCounter = 0;

        UUID playerId = mc.player.getUuid();

        // Always show assigned job area borders (even without wand) — uses integrated server
        if (mc.getServer() != null) {
            var serverWorld = mc.getServer().getOverworld();
            BlockPos playerPos = mc.player.getBlockPos();
            for (BlockPos bp : BlockPos.iterate(
                    playerPos.add(-48, -8, -48),
                    playerPos.add(48, 8, 48))) {
                var be = serverWorld.getBlockEntity(bp);
                if (be instanceof JobBlockEntity jb && jb.hasArea()) {
                    spawnBorderOnly(mc, jb.getAreaMin(), jb.getAreaMax(), false);
                }
            }
        }

        if (!holdingWand) return;

        BlockPos[] sel = AreaWandItem.getSelection(playerId);
        if (sel != null) {
            if (sel[0] != null && sel[1] == null) {
                // Corner 1 set — show marker + real-time preview to crosshair
                spawnMarker(mc, sel[0]);
                if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                    BlockPos crosshairPos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();
                    BlockPos min = new BlockPos(
                        Math.min(sel[0].getX(), crosshairPos.getX()),
                        Math.min(sel[0].getY(), crosshairPos.getY()),
                        Math.min(sel[0].getZ(), crosshairPos.getZ()));
                    BlockPos max = new BlockPos(
                        Math.max(sel[0].getX(), crosshairPos.getX()),
                        Math.max(sel[0].getY(), crosshairPos.getY()),
                        Math.max(sel[0].getZ(), crosshairPos.getZ()));
                    spawnFilledBox(mc, min, max, true);
                }
            } else if (sel[0] != null && sel[1] != null) {
                BlockPos min = new BlockPos(
                    Math.min(sel[0].getX(), sel[1].getX()),
                    Math.min(sel[0].getY(), sel[1].getY()),
                    Math.min(sel[0].getZ(), sel[1].getZ()));
                BlockPos max = new BlockPos(
                    Math.max(sel[0].getX(), sel[1].getX()),
                    Math.max(sel[0].getY(), sel[1].getY()),
                    Math.max(sel[0].getZ(), sel[1].getZ()));
                spawnFilledBox(mc, min, max, true);
            }
        }

        // Wand selection preview handled above, job areas handled before wand check
    }

    private static void spawnMarker(MinecraftClient mc, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.2;
        double z = pos.getZ() + 0.5;
        mc.world.addParticleClient(ParticleTypes.END_ROD, x, y, z, 0.0, 0.05, 0.0);
        mc.world.addParticleClient(ParticleTypes.END_ROD, x, y + 0.3, z, 0.0, 0.05, 0.0);
    }

    /** Render a border outline of a cuboid using particles on all 12 edges. */
    private static void spawnBorderOnly(MinecraftClient mc, BlockPos min, BlockPos max, boolean isSelection) {
        double x1 = min.getX(), y1 = min.getY(), z1 = min.getZ();
        double x2 = max.getX() + 1.0, y2 = max.getY() + 1.0, z2 = max.getZ() + 1.0;

        double px = mc.player.getX(), pz = mc.player.getZ();
        double cx = (x1 + x2) / 2, cz = (z1 + z2) / 2;
        if (Math.abs(px - cx) > 80 || Math.abs(pz - cz) > 80) return;

        var particle = isSelection
                ? new DustParticleEffect(0x55FF55, 1.0f)
                : new DustParticleEffect(0x55AAFF, 1.0f);

        double step = 0.5;

        // 4 bottom edges
        for (double x = x1; x <= x2; x += step) {
            mc.world.addParticleClient(particle, x, y1, z1, 0, 0, 0);
            mc.world.addParticleClient(particle, x, y1, z2, 0, 0, 0);
        }
        for (double z = z1; z <= z2; z += step) {
            mc.world.addParticleClient(particle, x1, y1, z, 0, 0, 0);
            mc.world.addParticleClient(particle, x2, y1, z, 0, 0, 0);
        }
        // 4 top edges
        for (double x = x1; x <= x2; x += step) {
            mc.world.addParticleClient(particle, x, y2, z1, 0, 0, 0);
            mc.world.addParticleClient(particle, x, y2, z2, 0, 0, 0);
        }
        for (double z = z1; z <= z2; z += step) {
            mc.world.addParticleClient(particle, x1, y2, z, 0, 0, 0);
            mc.world.addParticleClient(particle, x2, y2, z, 0, 0, 0);
        }
        // 4 vertical edges
        for (double y = y1; y <= y2; y += step) {
            mc.world.addParticleClient(particle, x1, y, z1, 0, 0, 0);
            mc.world.addParticleClient(particle, x2, y, z1, 0, 0, 0);
            mc.world.addParticleClient(particle, x1, y, z2, 0, 0, 0);
            mc.world.addParticleClient(particle, x2, y, z2, 0, 0, 0);
        }
    }

    /** Render a filled cuboid using particles on all 6 faces. */
    private static void spawnFilledBox(MinecraftClient mc, BlockPos min, BlockPos max, boolean isSelection) {
        double x1 = min.getX(), y1 = min.getY(), z1 = min.getZ();
        double x2 = max.getX() + 1.0, y2 = max.getY() + 1.0, z2 = max.getZ() + 1.0;

        double px = mc.player.getX(), pz = mc.player.getZ();
        double cx = (x1 + x2) / 2, cz = (z1 + z2) / 2;
        if (Math.abs(px - cx) > 48 || Math.abs(pz - cz) > 48) return;

        var selParticle = new DustParticleEffect(0x55FF55, 0.7f);
        var assignParticle = new DustParticleEffect(0x55AAFF, 1.0f);

        double step = 1.0;

        // Bottom face (y = y1)
        for (double x = x1; x <= x2; x += step) {
            for (double z = z1; z <= z2; z += step) {
                addFaceParticle(mc, x, y1, z, isSelection, selParticle, assignParticle);
            }
        }
        // Top face (y = y2)
        for (double x = x1; x <= x2; x += step) {
            for (double z = z1; z <= z2; z += step) {
                addFaceParticle(mc, x, y2, z, isSelection, selParticle, assignParticle);
            }
        }
        // Front face (z = z1)
        for (double x = x1; x <= x2; x += step) {
            for (double y = y1 + step; y < y2; y += step) {
                addFaceParticle(mc, x, y, z1, isSelection, selParticle, assignParticle);
            }
        }
        // Back face (z = z2)
        for (double x = x1; x <= x2; x += step) {
            for (double y = y1 + step; y < y2; y += step) {
                addFaceParticle(mc, x, y, z2, isSelection, selParticle, assignParticle);
            }
        }
        // Left face (x = x1)
        for (double z = z1 + step; z < z2; z += step) {
            for (double y = y1 + step; y < y2; y += step) {
                addFaceParticle(mc, x1, y, z, isSelection, selParticle, assignParticle);
            }
        }
        // Right face (x = x2)
        for (double z = z1 + step; z < z2; z += step) {
            for (double y = y1 + step; y < y2; y += step) {
                addFaceParticle(mc, x2, y, z, isSelection, selParticle, assignParticle);
            }
        }
    }

    private static void addFaceParticle(MinecraftClient mc, double x, double y, double z,
            boolean isSelection, DustParticleEffect selParticle, DustParticleEffect assignParticle) {
        if (isSelection) {
            mc.world.addParticleClient(selParticle, x, y, z, 0, 0, 0);
        } else {
            mc.world.addParticleClient(assignParticle, x, y, z, 0, 0, 0);
        }
    }
}
