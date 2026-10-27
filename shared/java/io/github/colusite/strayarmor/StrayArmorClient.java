package io.github.colusite.strayarmor;

import io.github.colusite.strayarmor.access.TextDisplayAccess;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StrayArmorClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("StrayArmor");

    /** Last resolved side per player, used when the close-range nametag loses its clan. */
    private static final Map<UUID, CachedSide> LAST_SIDES = new ConcurrentHashMap<>();
    private static final long CACHE_GRACE_MS = 5 * 60 * 1000L;

    @Override
    public void onInitializeClient() {
        StrayArmorConfigManager.load();
        StrayArmorConfigManager.maybeInstallStrayTagsConfig();
        StrayTagsConfig.load();
        StrayArmorCommand.register();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LAST_SIDES.clear();
            StrayArmorConfigManager.reload();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> LAST_SIDES.clear());
        LOGGER.info("[StrayArmor] Initialized (armor tinting {})",
                StrayArmorConfigManager.config().enabled ? "enabled" : "disabled");

        // TEMP-MIXIN-CHECK (removed after verification)
        loadTarget("net.minecraft.client.model.geom.ModelPart");
        loadTarget("net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer");
        loadTarget("net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer");
        loadTarget("net.minecraft.world.entity.Display$TextDisplay");
        LOGGER.info("[StrayArmor] TEMP mixin targets loaded");
    }

    private static void loadTarget(String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException expected) {
            // Not present on this Minecraft version.
        } catch (Throwable t) {
            LOGGER.error("[StrayArmor] TEMP mixin target FAILED: {}", className, t);
        }
    }

    public static int tintFor(Player player) {
        if (player == null) return 0;
        return tintFor(
                player.getName().getString(),
                player.getUUID(),
                findNameTags(player),
                tabListName(player.getUUID()),
                player == Minecraft.getInstance().player);
    }

    /**
     * Used by render-state based armor layers. The entity id lets us recover
     * the full player (and therefore their riding nametag) in newer versions.
     */
    public static int tintForState(int entityId, String stateName) {
        Player player = findPlayer(entityId);
        if (player != null) return tintFor(player);
        if (stateName == null || stateName.isBlank()) return 0;
        return tintFor(stateName, null, List.of(), null, false);
    }

    private static int tintFor(String playerName, UUID playerUuid, List<String> texts,
                               String tabListName, boolean self) {
        StrayArmorConfig cfg = StrayArmorConfigManager.config();
        if (cfg == null || !cfg.enabled) return 0;

        String serverAddress = currentServerAddress();
        if (!StrayTagsConfig.isActive(serverAddress)) return 0;

        Player localPlayer = Minecraft.getInstance().player;
        if (!self && localPlayer != null) {
            self = playerUuid != null
                    ? localPlayer.getUUID().equals(playerUuid)
                    : localPlayer.getName().getString().equals(playerName);
        }
        if (self) return StrayArmorConfigManager.tint(ArmorSide.OWN);

        ArmorMatch match = findMatch(serverAddress, playerName, playerUuid, texts, tabListName);
        if (match != null) {
            if (playerUuid != null) {
                LAST_SIDES.put(playerUuid, new CachedSide(
                        match.side,
                        TextUtil.clean(match.text),
                        serverAddress,
                        System.currentTimeMillis()));
            }
            return StrayArmorConfigManager.tint(match.side);
        }

        if (playerUuid != null) {
            CachedSide cached = LAST_SIDES.get(playerUuid);
            if (cached != null && cached.matches(serverAddress, texts, playerName)) {
                return StrayArmorConfigManager.tint(cached.side);
            }
        }
        return 0;
    }

    private static ArmorMatch findMatch(String serverAddress, String playerName, UUID playerUuid,
                                        List<String> texts, String tabListName) {
        for (String text : texts) {
            if (text == null || text.isBlank()) continue;
            ArmorSide side = StrayTagsConfig.classify(serverAddress, playerName, playerUuid, text);
            if (side != ArmorSide.NONE) return new ArmorMatch(side, text);
        }
        if (tabListName != null && !tabListName.isBlank()) {
            ArmorSide side = StrayTagsConfig.classify(serverAddress, playerName, playerUuid, tabListName);
            if (side != ArmorSide.NONE) return new ArmorMatch(side, tabListName);
        }
        return null;
    }

    public static String currentServerAddress() {
        ServerData serverData = Minecraft.getInstance().getCurrentServer();
        return serverData != null ? serverData.ip : null;
    }

    public static Player findPlayer(int entityId) {
        if (Minecraft.getInstance().level == null) return null;
        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        return entity instanceof Player player ? player : null;
    }

    /**
     * All text-display lines riding this player, with lines that contain the
     * player name first. Close-range servers sometimes send extra displays or
     * drop the clan from the tag, so callers try every candidate.
     */
    public static List<String> findNameTags(Player player) {
        List<String> named = new ArrayList<>();
        List<String> other = new ArrayList<>();
        if (player == null) return named;

        String playerName = player.getName().getString();
        Entity current = player;
        for (int depth = 0; current != null && depth < 6; depth++) {
            for (Entity passenger : current.getPassengers()) {
                if (!(passenger instanceof Display.TextDisplay textDisplay)) continue;
                Component text = ((TextDisplayAccess) textDisplay).strayarmor$getText();
                if (text == null) continue;

                String raw = text.getString();
                if (raw == null || raw.isBlank()) continue;

                for (String line : raw.split("\\R")) {
                    if (line == null || line.isBlank()) continue;
                    String cleaned = TextUtil.clean(line);
                    if (cleaned != null && !cleaned.isEmpty() && cleaned.contains(playerName)) {
                        addUnique(named, line);
                    } else {
                        addUnique(other, line);
                    }
                }
            }
            current = current.getVehicle();
        }
        named.addAll(other);
        return named;
    }

    private static void addUnique(List<String> list, String value) {
        if (!list.contains(value)) list.add(value);
    }

    /** The tab-list entry often keeps the clan even when the close-range tag does not. */
    private static String tabListName(UUID playerUuid) {
        if (playerUuid == null) return null;
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return null;

        PlayerInfo info = connection.getPlayerInfo(playerUuid);
        if (info == null) return null;

        Component displayName = info.getTabListDisplayName();
        return displayName != null ? displayName.getString() : null;
    }

    private static final class ArmorMatch {
        final ArmorSide side;
        final String text;

        ArmorMatch(ArmorSide side, String text) {
            this.side = side;
            this.text = text;
        }
    }

    private static final class CachedSide {
        final ArmorSide side;
        final String text;
        final String server;
        final long at;

        CachedSide(ArmorSide side, String text, String server, long at) {
            this.side = side;
            this.text = text;
            this.server = server;
            this.at = at;
        }

        boolean matches(String currentServer, List<String> texts, String playerName) {
            if (currentServer == null || !currentServer.equals(server)) return false;

            boolean sawPlayerName = false;
            for (String text : texts) {
                String cleaned = TextUtil.clean(text);
                if (cleaned == null || cleaned.isEmpty()) continue;
                if (cleaned.contains(playerName)) sawPlayerName = true;

                // A shorter close-range tag such as "Name" is still the same
                // player as the cached "Name [Clan]" tag.
                if (this.text != null
                        && (this.text.contains(cleaned) || cleaned.contains(this.text))) {
                    return true;
                }
            }

            // No usable tag this frame. Keep the last side briefly rather than
            // dropping the tint while the nametag is missing.
            return !sawPlayerName && System.currentTimeMillis() - at <= CACHE_GRACE_MS;
        }
    }
}
