package io.github.bridge_noob.strayarmor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Loads config/strayarmor.json and caches the resolved armor tints. */
public final class StrayArmorConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("StrayArmor");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static volatile StrayArmorConfig config = defaults();
    private static volatile int ownTint;
    private static volatile int allyTint;
    private static volatile int enemyTint;
    private static volatile int neutralTint;

    private StrayArmorConfigManager() {
    }

    public static StrayArmorConfig config() {
        return config;
    }

    public static void load() {
        Path path = configPath();
        if (!Files.exists(path)) {
            config = defaults();
            save();
            recompute();
            return;
        }
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            StrayArmorConfig parsed = GSON.fromJson(json, StrayArmorConfig.class);
            if (parsed == null) {
                LOGGER.warn("[StrayArmor] strayarmor.json is empty; using defaults");
                config = defaults();
                save();
                recompute();
                return;
            }
            config = parsed;
            if (upgrade(parsed)) save();
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("[StrayArmor] Could not read strayarmor.json: {}", e.getMessage());
            config = defaults();
        }
        recompute();
    }

    /**
     * Old configs used 0.8 strength, which washes the color toward white. Force
     * the new full-strength default once, then leave the user's value alone.
     */
    private static boolean upgrade(StrayArmorConfig cfg) {
        if (cfg.configVersion >= StrayArmorConfig.CURRENT_VERSION) return false;
        cfg.tintStrength = 1.0F;
        if (cfg.configVersion < 5) cfg.tintMode = "vivid";
        cfg.configVersion = StrayArmorConfig.CURRENT_VERSION;
        return true;
    }

    /** True when armor should be drawn as a flat color instead of textured. */
    public static boolean isSolidMode() {
        StrayArmorConfig cfg = config;
        return cfg != null && "solid".equalsIgnoreCase(cfg.tintMode);
    }

    private static StrayArmorConfig defaults() {
        StrayArmorConfig cfg = new StrayArmorConfig();
        cfg.configVersion = StrayArmorConfig.CURRENT_VERSION;
        return cfg;
    }

    public static void reload() {
        load();
        StrayTagsConfig.load();
    }

    public static void save() {
        try {
            Path path = configPath();
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(config), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("[StrayArmor] Failed to save strayarmor.json", e);
        }
    }

    public static int tint(ArmorSide side) {
        if (side == null) return 0;
        return switch (side) {
            case OWN -> ownTint;
            case ALLY -> allyTint;
            case ENEMY -> enemyTint;
            case NEUTRAL -> neutralTint;
            default -> 0;
        };
    }

    public static void maybeInstallStrayTagsConfig() {
        StrayArmorConfig cfg = config;
        if (cfg == null || !cfg.installStrayTagsConfig) return;

        Path target = FabricLoader.getInstance().getConfigDir().resolve("straytags.json");
        if (cfg.strayTagsConfigInstalled && Files.exists(target)) return;

        if (ConfigInstaller.install()) {
            cfg.strayTagsConfigInstalled = true;
            save();
            ConfigInstaller.reloadStrayTags();
            LOGGER.info("[StrayArmor] Installed the bundled StrayTags config (Valor is OWN)");
        }
    }

    public static void recompute() {
        StrayArmorConfig cfg = config;
        float strength = Math.max(0.0F, Math.min(1.0F, cfg.tintStrength));
        boolean vivid = cfg.tintMode == null || !"multiply".equalsIgnoreCase(cfg.tintMode);

        ownTint = resolve(cfg.ownColor, 0xFF00FF66, strength, vivid);
        allyTint = resolve(cfg.allyColor, 0xFF3BA7FF, strength, vivid);
        enemyTint = resolve(cfg.enemyColor, 0xFFFF3B3B, strength, vivid);
        neutralTint = resolve(cfg.neutralColor, 0, strength, vivid);
    }

    private static int resolve(String configured, int fallback, float strength, boolean vivid) {
        if (configured == null || configured.isBlank() || configured.equalsIgnoreCase("none") || configured.equals("#00000000")) {
            return 0;
        }
        int color = ColorUtil.parse(configured, fallback);
        if (vivid) color = ColorUtil.vivid(color);
        return ColorUtil.wash(color, strength);
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("strayarmor.json");
    }
}
