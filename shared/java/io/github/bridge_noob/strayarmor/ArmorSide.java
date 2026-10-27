package io.github.bridge_noob.strayarmor;

/**
 * The four armor tint groups StrayArmor understands.
 *
 * <p>StrayTags categories are user-defined, so the side is inferred from the
 * category name and format (see {@link StrayTagsConfig}). Anything that cannot
 * be mapped to one of these groups is left untinted.</p>
 */
public enum ArmorSide {
    NONE,
    OWN,
    ALLY,
    ENEMY,
    NEUTRAL
}
