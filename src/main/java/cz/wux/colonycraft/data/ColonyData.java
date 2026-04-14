package cz.wux.colonycraft.data;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Uuids;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.math.BlockPos;

import java.util.*;

/**
 * Holds all data for a single colony. Persisted via {@link ColonyManager}.
 *
 * <p>A colony is founded by placing a Colony Banner block. The banner's world
 * position is the colony's anchor. All colonist UUIDs, the stockpile position,
 * the food counter, the science counter, and the set of unlocked jobs are
 * stored here.</p>
 */
public class ColonyData {

    // ── Identity ──────────────────────────────────────────────────────────────
    private UUID colonyId;
    private UUID ownerUuid;
    private String ownerName;
    private BlockPos bannerPos;

    // ── Population ────────────────────────────────────────────────────────────
    private final List<UUID> colonistUuids = new ArrayList<>();
    /** Hard cap on colonist count. Grows by 1 per in-game day survived (base 2). */
    private int populationCap = 2;

    // ── Resources ─────────────────────────────────────────────────────────────
    /** Food units currently in stockpile. Each "meal" = 1 unit. */
    private int foodUnits = 0;
    /** Science points accumulated by researchers. */
    private int sciencePoints = 0;

    // ── Tech tree ─────────────────────────────────────────────────────────────
    private final Set<String> unlockedJobs = new HashSet<>();

    // ── Stockpile position ────────────────────────────────────────────────────
    private BlockPos stockpilePos;

    // ── Waves ─────────────────────────────────────────────────────────────────
    private int daysSurvived = 0;
    private long nextWaveDayTime = 13000; // first wave at dusk of day 1

    // ─────────────────────────────────────────────────────────────────────────

    public ColonyData(UUID colonyId, UUID ownerUuid, String ownerName, BlockPos bannerPos) {
        this.colonyId   = colonyId;
        this.ownerUuid  = ownerUuid;
        this.ownerName  = ownerName;
        this.bannerPos  = bannerPos;

        // Everyone starts with woodcutter and farmer unlocked
        unlockedJobs.add("WOODCUTTER");
        unlockedJobs.add("FARMER");
        unlockedJobs.add("FORESTER");
        unlockedJobs.add("FISHERMAN");
        unlockedJobs.add("COOK");
        unlockedJobs.add("WATER_GATHERER");
        unlockedJobs.add("BERRY_FARMER");
        unlockedJobs.add("GUARD_BOW");
        unlockedJobs.add("MINER");
    }

    // ── Serialization ─────────────────────────────────────────────────────────

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putIntArray("ColonyId",  Uuids.toIntArray(colonyId));
        nbt.putIntArray("OwnerUuid", Uuids.toIntArray(ownerUuid));
        nbt.putString("OwnerName", ownerName);
        nbt.putInt("BannerX", bannerPos.getX());
        nbt.putInt("BannerY", bannerPos.getY());
        nbt.putInt("BannerZ", bannerPos.getZ());
        nbt.putInt("PopCap",    populationCap);
        nbt.putInt("FoodUnits", foodUnits);
        nbt.putInt("Science",   sciencePoints);
        nbt.putInt("Days",      daysSurvived);
        nbt.putLong("NextWave", nextWaveDayTime);

        if (stockpilePos != null) {
            nbt.putInt("StockX", stockpilePos.getX());
            nbt.putInt("StockY", stockpilePos.getY());
            nbt.putInt("StockZ", stockpilePos.getZ());
        }

        NbtList colList = new NbtList();
        for (UUID u : colonistUuids) {
            NbtCompound c = new NbtCompound();
            c.putIntArray("UUID", Uuids.toIntArray(u));
            colList.add(c);
        }
        nbt.put("Colonists", colList);

        NbtList jobList = new NbtList();
        for (String j : unlockedJobs) jobList.add(NbtString.of(j));
        nbt.put("UnlockedJobs", jobList);

        return nbt;
    }

    public static ColonyData fromNbt(NbtCompound nbt) {
        UUID colonyId  = nbt.getIntArray("ColonyId").map(Uuids::toUuid).orElseThrow();
        UUID ownerUuid = nbt.getIntArray("OwnerUuid").map(Uuids::toUuid).orElseThrow();
        String name    = nbt.getString("OwnerName", "");
        BlockPos banner = new BlockPos(
                nbt.getInt("BannerX", 0), nbt.getInt("BannerY", 0), nbt.getInt("BannerZ", 0));

        ColonyData d = new ColonyData(colonyId, ownerUuid, name, banner);
        d.populationCap  = nbt.getInt("PopCap", 0);
        d.foodUnits      = nbt.getInt("FoodUnits", 0);
        d.sciencePoints  = nbt.getInt("Science", 0);
        d.daysSurvived   = nbt.getInt("Days", 0);
        d.nextWaveDayTime = nbt.getLong("NextWave", 0L);

        if (nbt.contains("StockX")) {
            d.stockpilePos = new BlockPos(
                    nbt.getInt("StockX", 0), nbt.getInt("StockY", 0), nbt.getInt("StockZ", 0));
        }

        NbtList colList = nbt.getListOrEmpty("Colonists");
        for (int i = 0; i < colList.size(); i++) {
            colList.getCompound(i).ifPresent(c ->
                c.getIntArray("UUID").map(Uuids::toUuid).ifPresent(d.colonistUuids::add));
        }

        d.unlockedJobs.clear();
        NbtList jobList = nbt.getListOrEmpty("UnlockedJobs");
        for (int i = 0; i < jobList.size(); i++) {
            d.unlockedJobs.add(jobList.getString(i, ""));
        }

        return d;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public boolean canFeedColonist() {
        return foodUnits > 0;
    }

    public boolean consumeFood() {
        if (foodUnits > 0) { foodUnits--; return true; }
        return false;
    }

    public void addFood(int amount) {
        foodUnits = Math.min(foodUnits + amount, 9999);
    }

    private void recalcPopCap() {
        // Pop cap grows with days survived: base 2 + 1 per day, max 50
        populationCap = Math.min(50, 2 + daysSurvived);
    }

    public boolean isJobUnlocked(ColonistJob job) {
        return unlockedJobs.contains(job.name());
    }

    public void unlockJob(ColonistJob job) {
        unlockedJobs.add(job.name());
    }

    public boolean spendScience(int cost) {
        if (sciencePoints >= cost) { sciencePoints -= cost; return true; }
        return false;
    }

    public void addScience(int amount) {
        sciencePoints += amount;
    }

    // ── Getters / setters ─────────────────────────────────────────────────────

    public UUID getColonyId()      { return colonyId; }
    public UUID getOwnerUuid()     { return ownerUuid; }
    public String getOwnerName()   { return ownerName; }
    public BlockPos getBannerPos() { return bannerPos; }

    public List<UUID> getColonistUuids() { return colonistUuids; }
    public int getPopulationCap()        { return populationCap; }
    public int getColonistCount()        { return colonistUuids.size(); }

    public int getFoodUnits()       { return foodUnits; }
    public int getSciencePoints()   { return sciencePoints; }
    public int getDaysSurvived()    { return daysSurvived; }
    public void incrementDays()     { daysSurvived++; recalcPopCap(); }

    public long getNextWaveDayTime()               { return nextWaveDayTime; }
    public void setNextWaveDayTime(long t)         { this.nextWaveDayTime = t; }

    public BlockPos getStockpilePos()              { return stockpilePos; }
    public void setStockpilePos(BlockPos pos)      { this.stockpilePos = pos; }

    public Set<String> getUnlockedJobs()           { return Collections.unmodifiableSet(unlockedJobs); }

    public void addColonist(UUID uuid)    { if (!colonistUuids.contains(uuid)) colonistUuids.add(uuid); }
    public void removeColonist(UUID uuid) { colonistUuids.remove(uuid); }

    public boolean canSpawnMoreColonists() {
        return colonistUuids.size() < populationCap;
    }
}
