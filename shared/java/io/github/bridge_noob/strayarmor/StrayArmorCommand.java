package io.github.bridge_noob.strayarmor;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public final class StrayArmorCommand {

    private StrayArmorCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> root =
                    LiteralArgumentBuilder.literal("strayarmor");

            root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("reload")
                    .executes(context -> {
                        StrayArmorConfigManager.reload();
                        ConfigInstaller.reloadStrayTags();
                        context.getSource().sendFeedback(
                                Component.literal("\u00a7a[StrayArmor] Configs reloaded"));
                        return 1;
                    }));

            root.then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("setup")
                    .executes(context -> {
                        boolean installed = ConfigInstaller.install();
                        StrayArmorConfigManager.reload();
                        ConfigInstaller.reloadStrayTags();
                        context.getSource().sendFeedback(Component.literal(installed
                                ? "\u00a7a[StrayArmor] Installed the Valor StrayTags config"
                                : "\u00a7c[StrayArmor] Could not install the StrayTags config"));
                        return installed ? 1 : 0;
                    }));

            LiteralArgumentBuilder<FabricClientCommandSource> set =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("set");

            LiteralArgumentBuilder<FabricClientCommandSource> setEnemy =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("enemy");
            RequiredArgumentBuilder<FabricClientCommandSource, String> setEnemyColor =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("color", StringArgumentType.string());
            setEnemy.then(setEnemyColor);
            setEnemy.executes(ctx -> setColor(ctx.getSource(), ArmorSide.ENEMY, StringArgumentType.getString(ctx, "color")));
            set.then(setEnemy);

            LiteralArgumentBuilder<FabricClientCommandSource> setOwn =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("own");
            RequiredArgumentBuilder<FabricClientCommandSource, String> setOwnColor =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("color", StringArgumentType.string());
            setOwn.then(setOwnColor);
            setOwn.executes(ctx -> setColor(ctx.getSource(), ArmorSide.OWN, StringArgumentType.getString(ctx, "color")));
            set.then(setOwn);

            LiteralArgumentBuilder<FabricClientCommandSource> setAlly =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("ally");
            RequiredArgumentBuilder<FabricClientCommandSource, String> setAllyColor =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("color", StringArgumentType.string());
            setAlly.then(setAllyColor);
            setAlly.executes(ctx -> setColor(ctx.getSource(), ArmorSide.ALLY, StringArgumentType.getString(ctx, "color")));
            set.then(setAlly);

            LiteralArgumentBuilder<FabricClientCommandSource> setNeutral =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("neutral");
            RequiredArgumentBuilder<FabricClientCommandSource, String> setNeutralColor =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("color", StringArgumentType.string());
            setNeutral.then(setNeutralColor);
            setNeutral.executes(ctx -> setColor(ctx.getSource(), ArmorSide.NEUTRAL, StringArgumentType.getString(ctx, "color")));
            set.then(setNeutral);

            root.then(set);

            dispatcher.register(root);
        });
    }

    private static int setColor(FabricClientCommandSource source, ArmorSide side, String color) {
        String value = color == null ? "" : color.trim();
        StrayArmorConfig cfg = StrayArmorConfigManager.config();
        switch (side) {
            case OWN -> cfg.ownColor = value;
            case ALLY -> cfg.allyColor = value;
            case ENEMY -> cfg.enemyColor = value;
            case NEUTRAL -> cfg.neutralColor = value;
        }
        StrayArmorConfigManager.save();
        StrayArmorConfigManager.recompute();
        source.sendFeedback(Component.literal("\u00a7a[StrayArmor] Set " + side + " color to " + value));
        return 1;
    }
}
