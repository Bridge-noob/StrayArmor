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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** ModMenu config screen for StrayArmor, backed by the same config/strayarmor.json. */
public class StrayArmorConfigScreenBuilder {

    public static Screen create(Screen parent) {
        StrayArmorConfig cfg = StrayArmorConfigManager.config();

        ConfigCategory general = ConfigCategory.createBuilder()
                .name(Component.literal("General"))
                .option(enabledOption(cfg))
                .option(installOption(cfg))
                .build();

        OptionGroup.Builder colors = OptionGroup.createBuilder()
                .name(Component.literal("Armor Colors"))
                .description(OptionDescription.of(Component.literal(
                        "Armor tint per side. Leave Neutral empty for normal, uncolored armor.")))
                .collapsed(false);

        colors.option(colorOption("Own Color", cfg.ownColor, v -> cfg.ownColor = v));
        colors.option(colorOption("Ally Color", cfg.allyColor, v -> cfg.allyColor = v));
        colors.option(colorOption("Enemy Color", cfg.enemyColor, v -> cfg.enemyColor = v));
        colors.option(colorOption("Neutral Color", cfg.neutralColor, v -> cfg.neutralColor = v));

        Option<Float> strength = Option.<Float>createBuilder()
                .name(Component.literal("Tint Strength"))
                .description(OptionDescription.of(Component.literal(
                        "0 = no tint, 1 = full color. Lower values blend toward white.")))
                .binding(cfg.tintStrength, () -> cfg.tintStrength, v -> cfg.tintStrength = v)
                .controller(o -> FloatSliderControllerBuilder.create(o)
                        .range(Float.valueOf(0F), Float.valueOf(1F))
                        .step(Float.valueOf(0.05F))
                        .formatValue(ValueFormatters.percent(0)))
                .build();

        Option<String> mode = Option.<String>createBuilder()
                .name(Component.literal("Tint Mode"))
                .description(OptionDescription.of(Component.literal(
                        "How the color is applied: vivid, solid, or multiply.")))
                .binding(cfg.tintMode, () -> cfg.tintMode, v -> cfg.tintMode = v)
                .controller(o -> DropdownStringControllerBuilder.create(o)
                        .values("vivid", "solid", "multiply")
                        .allowAnyValue(true))
                .build();

        colors.option(strength);
        colors.option(mode);

        ConfigCategory colorsCategory = ConfigCategory.createBuilder()
                .name(Component.literal("Armor Colors"))
                .group(colors.build())
                .build();

        YetAnotherConfigLib lib = YetAnotherConfigLib.createBuilder()
                .title(Component.literal("StrayArmor"))
                .categories(List.of(general, colorsCategory))
                .save(() -> {
                    StrayArmorConfigManager.save();
                    StrayArmorConfigManager.recompute();
                })
                .build();

        return lib.generateScreen(parent);
    }

    private static Option<Boolean> enabledOption(StrayArmorConfig cfg) {
        return Option.<Boolean>createBuilder()
                .name(Component.literal("Enabled"))
                .description(OptionDescription.of(Component.literal("Tint armor with StrayArmor.")))
                .binding(cfg.enabled, () -> cfg.enabled, v -> cfg.enabled = v)
                .controller(BooleanControllerBuilder::create)
                .build();
    }

    private static Option<Boolean> installOption(StrayArmorConfig cfg) {
        return Option.<Boolean>createBuilder()
                .name(Component.literal("Install StrayTags Config"))
                .description(OptionDescription.of(Component.literal(
                        "Write the bundled Valor StrayTags config on first launch.")))
                .binding(cfg.installStrayTagsConfig, () -> cfg.installStrayTagsConfig, v -> cfg.installStrayTagsConfig = v)
                .controller(BooleanControllerBuilder::create)
                .build();
    }

    private static Option<String> colorOption(String name, String value, java.util.function.Consumer<String> setter) {
        return Option.<String>createBuilder()
                .name(Component.literal(name))
                .description(OptionDescription.of(Component.literal(
                        "Armor color as #RRGGBB (or #AARRGGBB). Leave empty for no tint.")))
                .binding(value, () -> value, setter)
                .controller(StringControllerBuilder::create)
                .build();
    }
}
