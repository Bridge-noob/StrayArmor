package io.github.bridge_noob.strayarmor;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads StrayTags' own config file and answers "which side is this player on?".
 *
 * <p>This intentionally reads the JSON directly instead of linking against
 * StrayTags classes, so StrayArmor works with any StrayTags build and does not
 * need StrayTags present at compile time.</p>
 */
public final class StrayTagsConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("StrayArmor");
    private static final Gson GSON = new Gson();
    private static final String DEFAULT_PATTERN = "^(?<Usernames>\\S+)(?:\\s+\\[(?<Clans>[^\\]]+)\\])?$";
    private static final Pattern GROUP_NAME_PATTERN = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");
    private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> GROUP_CACHE = new ConcurrentHashMap<>();

    private static volatile Data data;

    private StrayTagsConfig() {
    }

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("straytags.json");
        if (!Files.exists(path)) {
            data = null;
            return;
        }
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            Data parsed = GSON.fromJson(json, Data.class);
            if (parsed == null) {
                LOGGER.warn("[StrayArmor] straytags.json is empty; armor tinting is off");
                data = null;
                return;
            }
            if (parsed.serverConfigs == null) parsed.serverConfigs = new LinkedHashMap<>();
            if (parsed.serverWhitelist == null) parsed.serverWhitelist = new ArrayList<>();
            data = parsed;
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("[StrayArmor] Could not read straytags.json: {}", e.getMessage());
            data = null;
        }
    }

    public static boolean isActive(String serverAddress) {
        Data current = data;
        return current != null
                && current.enabled
                && serverAddress != null
                && current.isWhitelisted(serverAddress)
                && current.getServerConfig(serverAddress) != null;
    }

    /**
     * Classifies a player using the same category order and matching rules as
     * StrayTags. {@code rawText} is the player's riding TextDisplay nametag, or
     * null if there is none.
     */
    public static ArmorSide classify(String serverAddress, String playerName,
                                     UUID playerUuid, String rawText) {
        Data current = data;
        if (current == null || !current.enabled) return ArmorSide.NONE;
        if (serverAddress == null || !current.isWhitelisted(serverAddress)) return ArmorSide.NONE;

        ServerData server = current.getServerConfig(serverAddress);
        if (server == null) return ArmorSide.NONE;

        String name = playerName == null ? "" : playerName;
        String text = TextUtil.clean(rawText);
        if (text == null || text.isBlank()) text = name;

        List<CategoryData> categories = categories(server);

        // Categories can override the server's matching regex. StrayTags tries
        // those first, then falls back to the server-wide pattern.
        for (CategoryData category : categories) {
            if (!appliesToServer(category, serverAddress)) continue;
            String override = category.namePatternOverride;
            if (override == null || override.isBlank()) continue;

            Pattern pattern = compile(override);
            if (pattern == null) continue;
            Matcher matcher = pattern.matcher(text);
            if (!matcher.matches()) continue;

            Map<String, String> matches = extract(override, matcher);
            List<String> groupNames = groupNames(override);
            if (categoryMatches(category, server, matches, groupNames, playerUuid, name)) {
                ArmorSide side = sideOf(category);
                return side != null ? side : ArmorSide.NONE;
            }
        }

        String patternSource = server.namePattern != null && !server.namePattern.isBlank()
                ? server.namePattern : DEFAULT_PATTERN;
        Pattern pattern = compile(patternSource);
        if (pattern == null) return ArmorSide.NONE;

        Matcher matcher = pattern.matcher(text);
        if (!matcher.matches()) return ArmorSide.NONE;

        Map<String, String> matches = extract(patternSource, matcher);
        List<String> groupNames = groupNames(patternSource);

        for (CategoryData category : categories) {
            if (!appliesToServer(category, serverAddress)) continue;
            if (categoryMatches(category, server, matches, groupNames, playerUuid, name)) {
                ArmorSide side = sideOf(category);
                return side != null ? side : ArmorSide.NONE;
            }
        }

        // No category matched. StrayTags shows the neutral format for a player
        // who still has a clan tag, so do the same for armor.
        if (groupNames.size() >= 2) {
            String clan = matches.get(groupNames.get(1));
            if (clan != null && !clan.isBlank()) return ArmorSide.NEUTRAL;
        }
        return ArmorSide.NONE;
    }

    private static List<CategoryData> categories(ServerData server) {
        if (server.categories == null || server.categories.isEmpty()) return Collections.emptyList();
        List<CategoryData> result = new ArrayList<>(server.categories.size());
        for (CategoryData category : server.categories) {
            if (category != null) result.add(category);
        }
        return result;
    }

    private static boolean categoryMatches(CategoryData category, ServerData server,
                                           Map<String, String> matches, List<String> patternGroupNames,
                                           UUID playerUuid, String playerName) {
        if (matches.isEmpty() || category.groupValues == null || category.groupValues.isEmpty()) {
            return false;
        }
        for (String groupName : effectiveMatchOrder(category, patternGroupNames)) {
            String matched = matches.get(groupName);
            if (matched == null || matched.isEmpty()) continue;

            List<String> configured = category.groupValues.get(groupName);
            if (configured == null || configured.isEmpty()) continue;

            boolean groupIsUuid = server.uuidGroups != null && server.uuidGroups.contains(groupName);
            for (String value : configured) {
                if (value == null || value.isEmpty()) continue;
                if (groupIsUuid) {
                    if (playerUuid != null && sameUuid(value, playerUuid)) return true;
                    if (value.equalsIgnoreCase(playerName)) return true;
                } else if (value.equalsIgnoreCase(matched)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> effectiveMatchOrder(CategoryData category, List<String> patternGroupNames) {
        List<String> order = new ArrayList<>();
        if (category.matchPriority != null) {
            for (String group : category.matchPriority) {
                if (group != null && patternGroupNames.contains(group) && !order.contains(group)) {
                    order.add(group);
                }
            }
        }
        for (String group : patternGroupNames) {
            if (!order.contains(group)) order.add(group);
        }
        return order;
    }

    private static boolean appliesToServer(CategoryData category, String serverAddress) {
        if (category.serverFilters == null || category.serverFilters.isEmpty()) return true;
        if (serverAddress == null) return true;
        String lower = serverAddress.toLowerCase(Locale.ROOT);
        for (String filter : category.serverFilters) {
            if (filter == null || filter.isBlank()) continue;
            String normalized = filter.toLowerCase(Locale.ROOT).trim();
            if (normalized.contains(".")) {
                if (lower.equals(normalized)) return true;
            } else if (lower.contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    private static ArmorSide sideOf(CategoryData category) {
        String text = TextUtil.stripMarkup(category.name) + " " + TextUtil.stripMarkup(category.format);
        String lower = text.toLowerCase(Locale.ROOT);

        if (lower.contains("enem") || lower.contains("hostile")) return ArmorSide.ENEMY;
        if (lower.contains("allie") || lower.contains("ally") || lower.contains("friend")) return ArmorSide.ALLY;
        if (lower.contains("own") || lower.contains("self")
                || lower.contains("valor") || lower.contains("latte")) return ArmorSide.OWN;
        if (lower.contains("neutral")) return ArmorSide.NEUTRAL;
        return null;
    }

    private static boolean sameUuid(String configured, UUID playerUuid) {
        String left = configured.toLowerCase(Locale.ROOT).replace("-", "");
        String right = playerUuid.toString().toLowerCase(Locale.ROOT).replace("-", "");
        return left.equals(right);
    }

    private static Map<String, String> extract(String patternSource, Matcher matcher) {
        Map<String, String> matches = new LinkedHashMap<>();
        for (String groupName : groupNames(patternSource)) {
            try {
                String value = matcher.group(groupName);
                if (value != null) matches.put(groupName, value);
            } catch (IllegalArgumentException ignored) {
                // The pattern changed between compilation and extraction.
            }
        }
        return matches;
    }

    private static Pattern compile(String source) {
        if (source == null) return null;
        Pattern cached = PATTERN_CACHE.get(source);
        if (cached != null) return cached;
        try {
            Pattern compiled = Pattern.compile(source);
            PATTERN_CACHE.put(source, compiled);
            return compiled;
        } catch (RuntimeException e) {
            LOGGER.warn("[StrayArmor] Invalid StrayTags regex '{}': {}", source, e.getMessage());
            return null;
        }
    }

    private static List<String> groupNames(String source) {
        if (source == null) return Collections.emptyList();
        List<String> cached = GROUP_CACHE.get(source);
        if (cached != null) return cached;

        List<String> names = new ArrayList<>();
        Matcher matcher = GROUP_NAME_PATTERN.matcher(source);
        while (matcher.find()) names.add(matcher.group(1));
        List<String> immutable = List.copyOf(names);
        GROUP_CACHE.put(source, immutable);
        return immutable;
    }

    /** Gson model matching the top level of config/straytags.json. */
    public static class Data {
        public boolean enabled = true;
        public List<String> serverWhitelist = new ArrayList<>();
        public Map<String, ServerData> serverConfigs = new LinkedHashMap<>();

        public boolean isWhitelisted(String serverAddress) {
            if (serverAddress == null || serverWhitelist == null) return false;
            String lower = serverAddress.toLowerCase(Locale.ROOT);
            for (String entry : serverWhitelist) {
                if (entry == null) continue;
                String allowed = entry.toLowerCase(Locale.ROOT);
                if (lower.equals(allowed) || lower.contains(allowed)) return true;
            }
            return false;
        }

        public ServerData getServerConfig(String serverAddress) {
            if (serverAddress == null || serverConfigs == null) return null;
            String lower = serverAddress.toLowerCase(Locale.ROOT);

            ServerData exact = serverConfigs.get(lower);
            if (exact != null) return exact;

            for (Map.Entry<String, ServerData> entry : serverConfigs.entrySet()) {
                String key = entry.getKey().toLowerCase(Locale.ROOT);
                if (key.equals("__default__")) continue;
                if (lower.contains(key) || key.contains(lower)) return entry.getValue();
            }
            return serverConfigs.get("__default__");
        }
    }

    /** Gson model for a per-server StrayTags config. */
    public static class ServerData {
        public List<CategoryData> categories = new ArrayList<>();
        public String neutralFormat = "";
        public String noClanFormat = "";
        public String namePattern = DEFAULT_PATTERN;
        public List<String> uuidGroups = new ArrayList<>(List.of("Usernames"));
    }

    /** Gson model for one StrayTags category. */
    public static class CategoryData {
        public String id;
        public String name;
        public String format;
        public Map<String, List<String>> groupValues = new LinkedHashMap<>();
        public List<String> matchPriority = new ArrayList<>();
        public List<String> serverFilters = new ArrayList<>();
        public String namePatternOverride;
    }
}
