package io.github.bridge_noob.strayarmor;

import java.util.Locale;

/** User-editable colors and install switches for StrayArmor. */
public class StrayArmorConfig {

    public static final String DEFAULT_OWN_COLOR = "#00FF66";
    public static final String DEFAULT_ALLY_COLOR = "#3BA7FF";
    public static final String DEFAULT_ENEMY_COLOR = "#FF0000";
    public static final String DEFAULT_NEUTRAL_COLOR = "";
    public static final float DEFAULT_TINT_STRENGTH = 1.0F;
    public static final String DEFAULT_TINT_MODE = "vivid";

    public boolean enabled = true;

    public String ownColor = DEFAULT_OWN_COLOR;
    public String allyColor = DEFAULT_ALLY_COLOR;
    public String enemyColor = DEFAULT_ENEMY_COLOR;
    public String neutralColor = DEFAULT_NEUTRAL_COLOR;

    /** 0 = no tint, 1 = full color. */
    public float tintStrength = DEFAULT_TINT_STRENGTH;

    /**
     * How the color is applied:
     * <ul>
     *   <li>{@code vivid} - pure, fully saturated color; the armor becomes that
     *       hue with the texture only affecting brightness (default).</li>
     *   <li>{@code solid} - flat color with no texture detail.</li>
     *   <li>{@code multiply} - original behavior, color multiplied into the
     *       texture.</li>
     * </ul>
     */
    public String tintMode = DEFAULT_TINT_MODE;

    /** Per-side switches so a color can be kept but not applied. */
    public boolean tintOwn = true;
    public boolean tintAlly = true;
    public boolean tintEnemy = true;
    public boolean tintNeutral = true;

    /** Internal config version, used to upgrade old defaults once. */
    public int configVersion = 0;

    /** Writes the bundled Valor StrayTags config on first launch. */
    public boolean installStrayTagsConfig = true;

    /** Internal marker; set after the bundled config has been installed once. */
    public boolean strayTagsConfigInstalled = false;

    /** Bumped when the bundled defaults change, so existing configs can upgrade. */
    public static final int CURRENT_VERSION = 6;

    /**
     * Normalizes a color field: accepts {@code #RRGGBB} / {@code #AARRGGBB} hex or a
     * comma-separated RGB triple such as {@code 255,0,0} (values may be floats in
     * 0..1 or numbers in 0..255). Returns {@code null} when the value is invalid.
     * Returns an empty string for "no tint".
     */
    public static String normalizeColor(String value) {
        if (value == null) return "";
        String v = value.trim();
        if (v.isEmpty()) return "";
        if (v.contains(",")) {
            String[] parts = v.split(",", -1);
            if (parts.length < 3) return null;
            float r = parseColorChannel(parts[0]);
            float g = parseColorChannel(parts[1]);
            float b = parseColorChannel(parts[2]);
            if (r < 0f || g < 0f || b < 0f) return null;
            int rr = clampColorChannel(r);
            int gg = clampColorChannel(g);
            int bb = clampColorChannel(b);
            return String.format("#%02X%02X%02X", rr, gg, bb);
        }
        if (!v.startsWith("#")) v = "#" + v;
        if (!v.matches("^#([0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")) return null;
        return v.toUpperCase(Locale.ROOT);
    }

    private static float parseColorChannel(String part) {
        try {
            return Float.parseFloat(part.trim());
        } catch (NumberFormatException e) {
            return -1f;
        }
    }

    private static int clampColorChannel(float f) {
        if (Float.isNaN(f) || f < 0f) return 0;
        float scaled = f <= 1f ? f * 255f : f;
        if (scaled > 255f) scaled = 255f;
        return (int) Math.round(scaled);
    }
}
