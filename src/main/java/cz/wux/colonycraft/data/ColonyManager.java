package cz.wux.colonycraft.data;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.*;

/**
 * Server-side singleton that manages every colony in the world.
 * Data is persisted through Minecraft's {@link PersistentState} mechanism so it
 * survives server restarts.
 *
 * <p>Obtain via {@link #get(MinecraftServer)} or {@link #get(ServerWorld)}.</p>
 */
public class ColonyManager extends PersistentState {

    private static final String DATA_KEY = "colonycraft_colonies";

    private final Map<UUID, ColonyData> colonies = new HashMap<>();

    // ── PersistentState factory ───────────────────────────────────────────────

    private static final Codec<ColonyManager> CODEC = NbtCompound.CODEC.xmap(
            ColonyManager::fromNbt,
            ColonyManager::toNbt
    );

    private static final PersistentStateType<ColonyManager> TYPE = new PersistentStateType<>(
            DATA_KEY,
            ColonyManager::new,
            CODEC,
            null
    );

    public static ColonyManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public static ColonyManager get(MinecraftServer server) {
        return get(server.getOverworld());
    }

    // ── Serialization ─────────────────────────────────────────────────────────

    private NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        NbtList list = new NbtList();
        for (ColonyData cd : colonies.values()) {
            list.add(cd.toNbt());
        }
        nbt.put("Colonies", list);
        return nbt;
    }

    private static ColonyManager fromNbt(NbtCompound nbt) {
        ColonyManager mgr = new ColonyManager();
        NbtList list = nbt.getListOrEmpty("Colonies");
        for (int i = 0; i < list.size(); i++) {
            ColonyData cd = ColonyData.fromNbt(list.getCompoundOrEmpty(i));
            mgr.colonies.put(cd.getColonyId(), cd);
        }
        return mgr;
    }

    // ── Colony lifecycle ──────────────────────────────────────────────────────

    /** Creates a new colony and marks this state dirty. */
    public ColonyData createColony(UUID ownerUuid, String ownerName, BlockPos bannerPos) {
        UUID colonyId = UUID.randomUUID();
        ColonyData data = new ColonyData(colonyId, ownerUuid, ownerName, bannerPos);
        colonies.put(colonyId, data);
        markDirty();
        return data;
    }

    public void removeColony(UUID colonyId) {
        colonies.remove(colonyId);
        markDirty();
    }

    // ── Lookups ───────────────────────────────────────────────────────────────

    public Optional<ColonyData> getColony(UUID colonyId) {
        return Optional.ofNullable(colonies.get(colonyId));
    }

    /** Returns the colony whose banner is at exactly this position. */
    public Optional<ColonyData> getColonyAtBanner(BlockPos pos) {
        return colonies.values().stream()
                .filter(cd -> cd.getBannerPos().equals(pos))
                .findFirst();
    }

    /**
     * Returns the nearest colony within {@code maxDistSq} blocks² of the given
     * position, or empty if none found.
     */
    public Optional<ColonyData> getNearestColony(BlockPos pos, double maxDistSq) {
        ColonyData nearest = null;
        double best = Double.MAX_VALUE;
        for (ColonyData cd : colonies.values()) {
            double d = cd.getBannerPos().getSquaredDistance(pos);
            if (d <= maxDistSq && d < best) {
                best = d;
                nearest = cd;
            }
        }
        return Optional.ofNullable(nearest);
    }

    public Collection<ColonyData> getAllColonies() {
        return Collections.unmodifiableCollection(colonies.values());
    }

    public void markDirty() {
        super.markDirty();
    }
}
