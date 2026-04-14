package cz.wux.colonycraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple JSON config for ColonyCraft. File: config/colonycraft.json
 * Loaded once at startup. Edit the file and restart to apply changes.
 */
public class ColonyCraftConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("colonycraft.json");

    private static ColonyCraftConfig INSTANCE;

    // ── Fields (all public for Gson) ──────────────────────────────────────────

    /** Colony tick interval in ticks (default 60 = 3 s). */
    public int colonyTickInterval = 60;

    /** Food consumption interval in colony-ticks (default 100 = ~5 min). */
    public int foodConsumeInterval = 100;

    /** Maximum wave size hard cap (default 40). */
    public int maxWaveSize = 40;

    /** Base wave HP (default 20). Scales up with days. */
    public double baseMonsterHp = 20.0;

    /** Population cap growth per day survived (default 1). */
    public int popCapGrowthPerDay = 1;

    /** Colony border radius in blocks (default 64). */
    public int colonyRadius = 64;

    /** Whether to show border particles (default true). */
    public boolean showBorderParticles = true;

    /** Whether to show colonist name tags (default true). */
    public boolean showColonistNametags = true;

    // ── Singleton access ──────────────────────────────────────────────────────

    public static ColonyCraftConfig get() {
        if (INSTANCE == null) INSTANCE = load();
        return INSTANCE;
    }

    // ── Load / Save ───────────────────────────────────────────────────────────

    private static ColonyCraftConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                ColonyCraftConfig cfg = GSON.fromJson(r, ColonyCraftConfig.class);
                if (cfg != null) {
                    cfg.save(); // re-save to add any new fields
                    return cfg;
                }
            } catch (IOException e) {
                ColonyCraftMod.LOGGER.warn("Failed to load colonycraft.json, using defaults: {}", e.getMessage());
            }
        }
        ColonyCraftConfig defaults = new ColonyCraftConfig();
        defaults.save();
        return defaults;
    }

    public void save() {
        try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(this, w);
        } catch (IOException e) {
            ColonyCraftMod.LOGGER.warn("Failed to save colonycraft.json: {}", e.getMessage());
        }
    }
}
