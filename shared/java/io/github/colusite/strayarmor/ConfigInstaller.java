package io.github.colusite.strayarmor;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Writes the bundled StrayTags config and asks StrayTags to reload it. */
public final class ConfigInstaller {

    private static final Logger LOGGER = LoggerFactory.getLogger("StrayArmor");
    private static final String RESOURCE = "/assets/strayarmor/valor-straytags.json";
    private static final String STRAYTAGS_MANAGER =
            "io.github.colusite.straytags.client.config.StrayTagsConfigManager";

    private ConfigInstaller() {
    }

    public static boolean install() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Files.createDirectories(configDir);
            Path target = configDir.resolve("straytags.json");

            if (Files.exists(target)) {
                Path backup = configDir.resolve("straytags.json.strayarmor.bak");
                Files.copy(target, backup, StandardCopyOption.REPLACE_EXISTING);
            }

            try (InputStream stream = StrayArmorClient.class.getResourceAsStream(RESOURCE)) {
                if (stream == null) {
                    LOGGER.error("[StrayArmor] Bundled config {} is missing", RESOURCE);
                    return false;
                }
                Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("[StrayArmor] Failed to install straytags.json", e);
            return false;
        }
    }

    /**
     * Calls StrayTags' public config reload when StrayTags is installed. The
     * reflection keeps StrayArmor usable without a compile-time dependency.
     */
    public static void reloadStrayTags() {
        try {
            Class<?> manager = Class.forName(STRAYTAGS_MANAGER);
            manager.getMethod("load").invoke(null);
        } catch (ClassNotFoundException e) {
            LOGGER.info("[StrayArmor] StrayTags is not installed; using straytags.json directly");
        } catch (ReflectiveOperationException | LinkageError e) {
            LOGGER.warn("[StrayArmor] Could not reload StrayTags config: {}", e.getMessage());
        }
    }
}
