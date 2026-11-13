package io.github.bridge_noob.strayarmor.client.modmenu;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DropdownStringControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.gui.ValueFormatters;
import io.github.bridge_noob.strayarmor.StrayArmorConfig;
import io.github.bridge_noob.strayarmor.StrayArmorConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** ModMenu config screen for StrayArmor, backed by the same config/strayarmor.json. */
public class StrayArmorConfigScreenBuilder {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("StrayArmor");

    public static Screen create(Screen parent) {
        StrayArmorConfig cfg = StrayArmorConfigManager.config();
        // Snapshot the colors so an invalid entry can be reverted instead of saved.
        String beforeOwn = cfg.ownColor;
        String beforeAlly = cfg.allyColor;
        String beforeEnemy = cfg.enemyColor;
        String beforeNeutral = cfg.neutralColor;

        OptionGroup.Builder colors = OptionGroup.createBuilder()
                .name(Component.literal("Side Colors"))
                .description(OptionDescription.of(Component.literal(
                        "Armor tint per side. Leave Neutral empty for normal, uncolored armor.")))
                .collapsed(false);
        colors.option(colorOption("Own", StrayArmorConfig.DEFAULT_OWN_COLOR,
                () -> cfg.ownColor, v -> cfg.ownColor = v));
        colors.option(colorOption("Ally", StrayArmorConfig.DEFAULT_ALLY_COLOR,
                () -> cfg.allyColor, v -> cfg.allyColor = v));
        colors.option(colorOption("Enemy", StrayArmorConfig.DEFAULT_ENEMY_COLOR,
                () -> cfg.enemyColor, v -> cfg.enemyColor = v));
        colors.option(colorOption("Neutral", StrayArmorConfig.DEFAULT_NEUTRAL_COLOR,
                () -> cfg.neutralColor, v -> cfg.neutralColor = v));

        OptionGroup.Builder sides = OptionGroup.createBuilder()
                .name(Component.literal("Enabled Sides"))
                .description(OptionDescription.of(Component.literal(
                        "Switch individual tints off without losing their color.")))
                .collapsed(false);
        sides.option(boolOption("Own", "Tint your own armor.",
                () -> cfg.tintOwn, v -> cfg.tintOwn = v));
        sides.option(boolOption("Ally", "Tint allied players.",
                () -> cfg.tintAlly, v -> cfg.tintAlly = v));
        sides.option(boolOption("Enemy", "Tint enemy players.",
                () -> cfg.tintEnemy, v -> cfg.tintEnemy = v));
        sides.option(boolOption("Neutral", "Tint players with no matching clan.",
                () -> cfg.tintNeutral, v -> cfg.tintNeutral = v));

        OptionGroup.Builder appearance = OptionGroup.createBuilder()
                .name(Component.literal("Tint Appearance"))
                .description(OptionDescription.of(Component.literal(
                        "How strong the tint is and how it blends with the armor texture.")))
                .collapsed(false);
        appearance.option(strengthOption(cfg));

        ConfigCategory general = ConfigCategory.createBuilder()
                .name(Component.literal("General"))
                .option(boolOption("Enabled", "Master switch for all armor tinting.",
                        () -> cfg.enabled, v -> cfg.enabled = v))
                .option(boolOption("Install StrayTags Config",
                        "Write the bundled Valor StrayTags config on first launch.",
                        () -> cfg.installStrayTagsConfig, v -> cfg.installStrayTagsConfig = v))
                .build();

        ConfigCategory armor = ConfigCategory.createBuilder()
                .name(Component.literal("Armor Colors"))
                .group(colors.build())
                .group(sides.build())
                .group(appearance.build())
                .build();

        YetAnotherConfigLib lib = YetAnotherConfigLib.createBuilder()
                .title(Component.literal("StrayArmor"))
                .categories(List.of(general, armor))
                .save(() -> save(cfg, beforeOwn, beforeAlly, beforeEnemy, beforeNeutral))
                .build();

        return lib.generateScreen(parent);
    }

    /**
     * Normalizes every color, reverting only the invalid ones, then persists.
     * Called when the user presses Save.
     */
    private static void save(StrayArmorConfig cfg, String beforeOwn, String beforeAlly,
                             String beforeEnemy, String beforeNeutral) {
        boolean ok = applyColor(cfg, "Own", beforeOwn)
                & applyColor(cfg, "Ally", beforeAlly)
                & applyColor(cfg, "Enemy", beforeEnemy)
                & applyColor(cfg, "Neutral", beforeNeutral);
        if (!ok) {
            // Persist the reverted values so the bad text is not saved.
            StrayArmorConfigManager.save();
            StrayArmorConfigManager.recompute();
            return;
        }
        StrayArmorConfigManager.save();
        StrayArmorConfigManager.recompute();
        feedback("\\u00a7a[StrayArmor] Settings saved");
    }

    /** Normalizes one color field; reverts it to {@code before} and warns when invalid. */
    private static boolean applyColor(StrayArmorConfig cfg, String side, String before) {
        String value = colorOf(cfg, side);
        String normalized = StrayArmorConfig.normalizeColor(value);
        if (normalized == null) {
            setColor(cfg, side, before);
            LOGGER.warn("[StrayArmor] Rejected invalid {} color: '{}'", side, value);
            feedback("\\u00a7c[StrayArmor] Invalid " + side + " color '" + value
                    + "' - use #RRGGBB or #AARRGGBB; the old value was kept");
            return false;
        }
        setColor(cfg, side, normalized);
        return true;
    }

    private static String colorOf(StrayArmorConfig cfg, String side) {
        return switch (side) {
            case "Own" -> cfg.ownColor;
            case "Ally" -> cfg.allyColor;
            case "Enemy" -> cfg.enemyColor;
            default -> cfg.neutralColor;
        };
    }

    private static void setColor(StrayArmorConfig cfg, String side, String value) {
        switch (side) {
            case "Own" -> cfg.ownColor = value;
            case "Ally" -> cfg.allyColor = value;
            case "Enemy" -> cfg.enemyColor = value;
            default -> cfg.neutralColor = value;
        }
    }

    private static Option<Boolean> boolOption(String name, String description,
                                              Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Component.literal(name))
                .description(OptionDescription.of(Component.literal(description)))
                .binding(true, getter, setter)
                .controller(BooleanControllerBuilder::create)
                .build();
    }

    private static Option<String> colorOption(String side, String def,
                                              Supplier<String> getter, Consumer<String> setter) {
        return Option.<String>createBuilder()
                .name(Component.literal(side + " Color"))
                .description(OptionDescription.of(Component.literal(
                        "Armor color as #RRGGBB (or #AARRGGBB). Leave empty for no tint. Default: "
                                + (def.isEmpty() ? "none" : def))))
                .binding(def, getter, setter)
                .controller(StringControllerBuilder::create)
                .build();
    }

    private static Option<Float> strengthOption(StrayArmorConfig cfg) {
        return Option.<Float>createBuilder()
                .name(Component.literal("Tint Strength"))
                .description(OptionDescription.of(Component.literal(
                        "0 = no tint, 1 = full color. Lower values blend toward white. Default: 100%.")))
                .binding(StrayArmorConfig.DEFAULT_TINT_STRENGTH,
                        () -> cfg.tintStrength, v -> cfg.tintStrength = v)
                .controller(o -> FloatSliderControllerBuilder.create(o)
                        .range(Float.valueOf(0F), Float.valueOf(1F))
                        .step(Float.valueOf(0.05F))
                        .formatValue(ValueFormatters.percent(0)))
                .build();
    }

    private static void feedback(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Component text = Component.literal(message);
        try {
            // Minecraft 26.x renamed the client feedback entry point; reflect so
            // the config screen works on both the older and the newer render paths.
            mc.player.getClass().getMethod("sendSystemMessage", Component.class).invoke(mc.player, text);
        } catch (ReflectiveOperationException ignored) {
            // Older versions used a different feedback method; nothing to do there.
        }
    }
}
