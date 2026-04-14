package cz.wux.colonycraft.client.render;

import cz.wux.colonycraft.blockentity.JobBlockEntity;
import cz.wux.colonycraft.item.AreaWandItem;
import cz.wux.colonycraft.registry.ModItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class AreaWandRenderer {

    private static int tickCounter = 0;

    public static void tick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null || mc.getServer() == null) return;
        boolean holdingWand = mc.player.getMainHandStack().isOf(ModItems.AREA_WAND)
                           || mc.player.getOffHandStack().isOf(ModItems.AREA_WAND);
        if (!holdingWand) return;
        tickCounter++;
        if (tickCounter < 5) return;
        tickCounter = 0;

        UUID playerId = mc.player.getUuid();
        ServerWorld serverWorld = mc.getServer().getOverworld();

        BlockPos[] sel = AreaWandItem.getSelection(playerId);
        if (sel != null) {
            if (sel[0] != null && sel[1] == null) {
                spawnMarker(mc, sel[0]);
            } else if (sel[0] != null && sel[1] != null) {
                BlockPos min = new BlockPos(
                    Math.min(sel[0].getX(), sel[1].getX()),
                    Math.min(sel[0].getY(), sel[1].getY()),
                    Math.min(sel[0].getZ(), sel[1].getZ()));
                BlockPos max = new BlockPos(
                    Math.max(sel[0].getX(), sel[1].getX()),
                    Math.max(sel[0].getY(), sel[1].getY()),
                    Math.max(sel[0].getZ(), sel[1].getZ()));
                spawnBoxOutline(mc, min, max, true);
            }
        }

        BlockPos playerPos = mc.player.getBlockPos();
        for (BlockPos bp : BlockPos.iterate(
                playerPos.add(-32, -8, -32),
                playerPos.add(32, 8, 32))) {
            var be = serverWorld.getBlockEntity(bp);
            if (be instanceof JobBlockEntity jb && jb.hasArea()) {
                spawnBoxOutline(mc, jb.getAreaMin(), jb.getAreaMax(), false);
            }
        }
    }

    private static void spawnMarker(MinecraftClient mc, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.2;
        double z = pos.getZ() + 0.5;
        mc.world.addParticleClient(ParticleTypes.END_ROD, x, y, z, 0.0, 0.05, 0.0);
        mc.world.addParticleClient(ParticleTypes.END_ROD, x, y + 0.3, z, 0.0, 0.05, 0.0);
    }

    private static void spawnBoxOutline(MinecraftClient mc, BlockPos min, BlockPos max, boolean isSelection) {
        double x1 = min.getX(), y1 = min.getY(), z1 = min.getZ();
        double x2 = max.getX() + 1.0, y2 = max.getY() + 1.0, z2 = max.getZ() + 1.0;
        double px = mc.player.getX(), pz = mc.player.getZ();
        double cx = (x1 + x2) / 2, cz = (z1 + z2) / 2;
        if (Math.abs(px - cx) > 48 || Math.abs(pz - cz) > 48) return;

        spawnLine(mc, x1,y1,z1, x2,y1,z1, isSelection);
        spawnLine(mc, x1,y1,z2, x2,y1,z2, isSelection);
        spawnLine(mc, x1,y1,z1, x1,y1,z2, isSelection);
        spawnLine(mc, x2,y1,z1, x2,y1,z2, isSelection);
        spawnLine(mc, x1,y2,z1, x2,y2,z1, isSelection);
        spawnLine(mc, x1,y2,z2, x2,y2,z2, isSelection);
        spawnLine(mc, x1,y2,z1, x1,y2,z2, isSelection);
        spawnLine(mc, x2,y2,z1, x2,y2,z2, isSelection);
        spawnLine(mc, x1,y1,z1, x1,y2,z1, isSelection);
        spawnLine(mc, x2,y1,z1, x2,y2,z1, isSelection);
        spawnLine(mc, x1,y1,z2, x1,y2,z2, isSelection);
        spawnLine(mc, x2,y1,z2, x2,y2,z2, isSelection);
    }

    private static void spawnLine(MinecraftClient mc,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            boolean isSelection) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double len = Math.sqrt(dx*dx + dy*dy + dz*dz);
        int count = Math.max(1, (int)(len / 2.0));
        for (int i = 0; i <= count; i++) {
            double t = (double) i / Math.max(1, count);
            double x = x1 + dx * t;
            double y = y1 + dy * t;
            double z = z1 + dz * t;
            if (isSelection) {
                mc.world.addParticleClient(ParticleTypes.END_ROD, x, y, z, 0.0, 0.0, 0.0);
            } else {
                mc.world.addParticleClient(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }
}
