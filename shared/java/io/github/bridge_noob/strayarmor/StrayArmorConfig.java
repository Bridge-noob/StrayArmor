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
     * Normalizes a color field: adds the missing {@code #} and upper-cases it.
     * Empty or blank means "no tint". Returns {@code null} when the value is
     * not a valid {@code #RRGGBB} / {@code #AARRGGBB} color.
     */
    public static String normalizeColor(String value) {
        if (value == null) return "";
        String v = value.trim();
        if (v.isEmpty()) return "";
        if (!v.startsWith("#")) v = "#" + v;
        if (!v.matches("^#([0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")) return null;
        return v.toUpperCase(Locale.ROOT);
    }
}
