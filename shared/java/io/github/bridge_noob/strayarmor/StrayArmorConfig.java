package io.github.bridge_noob.strayarmor;

/** User-editable colors and install switches for StrayArmor. */
public class StrayArmorConfig {

    public boolean enabled = true;

    public String ownColor = "#00FF66";
    public String allyColor = "#3BA7FF";
    public String enemyColor = "#FF3B3B";
    public String neutralColor = "";

    /** 0 = no tint, 1 = full color. */
    public float tintStrength = 1.0F;

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
    public String tintMode = "vivid";

    /** Internal config version, used to upgrade old defaults once. */
    public int configVersion = 0;

    /** Writes the bundled Valor StrayTags config on first launch. */
    public boolean installStrayTagsConfig = true;

    /** Internal marker; set after the bundled config has been installed once. */
    public boolean strayTagsConfigInstalled = false;

    /** Bumped when the bundled defaults change, so existing configs can upgrade. */
    public static final int CURRENT_VERSION = 5;
}
