package io.github.bridge_noob.strayarmor;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class StrayArmorCommand {

    private StrayArmorCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> root =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("strayarmor");

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

            LiteralArgumentBuilder<FabricClientCommandSource> setOwn =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("own");
            RequiredArgumentBuilder<FabricClientCommandSource, String> setOwnColor =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("color", StringArgumentType.string());
            setOwnColor.executes(ctx -> setColor(ctx.getSource(), ArmorSide.OWN, StringArgumentType.getString(ctx, "color")));
            setOwn.then(setOwnColor);
            set.then(setOwn);

            LiteralArgumentBuilder<FabricClientCommandSource> setAlly =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("ally");
            RequiredArgumentBuilder<FabricClientCommandSource, String> setAllyColor =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("color", StringArgumentType.string());
            setAllyColor.executes(ctx -> setColor(ctx.getSource(), ArmorSide.ALLY, StringArgumentType.getString(ctx, "color")));
            setAlly.then(setAllyColor);
            set.then(setAlly);

            LiteralArgumentBuilder<FabricClientCommandSource> setEnemy =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("enemy");
            RequiredArgumentBuilder<FabricClientCommandSource, String> setEnemyColor =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("color", StringArgumentType.string());
            setEnemyColor.executes(ctx -> setColor(ctx.getSource(), ArmorSide.ENEMY, StringArgumentType.getString(ctx, "color")));
            setEnemy.then(setEnemyColor);
            set.then(setEnemy);

            LiteralArgumentBuilder<FabricClientCommandSource> setNeutral =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("neutral");
            RequiredArgumentBuilder<FabricClientCommandSource, String> setNeutralColor =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("color", StringArgumentType.string());
            setNeutralColor.executes(ctx -> setColor(ctx.getSource(), ArmorSide.NEUTRAL, StringArgumentType.getString(ctx, "color")));
            setNeutral.then(setNeutralColor);
            set.then(setNeutral);

            root.then(set);

            LiteralArgumentBuilder<FabricClientCommandSource> clan =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("clan");

            LiteralArgumentBuilder<FabricClientCommandSource> clanAdd =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("add");

            LiteralArgumentBuilder<FabricClientCommandSource> clanAddEnemy =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("enemy");
            RequiredArgumentBuilder<FabricClientCommandSource, String> clanAddEnemyClans =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("clans", StringArgumentType.string());
            clanAddEnemyClans.executes(ctx -> addClan(ctx.getSource(), ArmorSide.ENEMY, StringArgumentType.getString(ctx, "clans")));
            clanAddEnemy.then(clanAddEnemyClans);
            clanAdd.then(clanAddEnemy);
            clan.then(clanAdd);

            LiteralArgumentBuilder<FabricClientCommandSource> clanAddAlly =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("ally");
            RequiredArgumentBuilder<FabricClientCommandSource, String> clanAddAllyClans =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("clans", StringArgumentType.string());
            clanAddAllyClans.executes(ctx -> addClan(ctx.getSource(), ArmorSide.ALLY, StringArgumentType.getString(ctx, "clans")));
            clanAddAlly.then(clanAddAllyClans);
            clanAdd.then(clanAddAlly);
            clan.then(clanAdd);

            LiteralArgumentBuilder<FabricClientCommandSource> clanAddOwn =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("own");
            RequiredArgumentBuilder<FabricClientCommandSource, String> clanAddOwnClans =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("clans", StringArgumentType.string());
            clanAddOwnClans.executes(ctx -> addClan(ctx.getSource(), ArmorSide.OWN, StringArgumentType.getString(ctx, "clans")));
            clanAddOwn.then(clanAddOwnClans);
            clanAdd.then(clanAddOwn);
            clan.then(clanAdd);

            LiteralArgumentBuilder<FabricClientCommandSource> clanAddNeutral =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("neutral");
            RequiredArgumentBuilder<FabricClientCommandSource, String> clanAddNeutralClans =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("clans", StringArgumentType.string());
            clanAddNeutralClans.executes(ctx -> addClan(ctx.getSource(), ArmorSide.NEUTRAL, StringArgumentType.getString(ctx, "clans")));
            clanAddNeutral.then(clanAddNeutralClans);
            clanAdd.then(clanAddNeutral);
            clan.then(clanAdd);

            LiteralArgumentBuilder<FabricClientCommandSource> clanEdit =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("edit");

            LiteralArgumentBuilder<FabricClientCommandSource> clanEditEnemy =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("enemy");
            RequiredArgumentBuilder<FabricClientCommandSource, String> clanEditOld =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("old", StringArgumentType.string());
            RequiredArgumentBuilder<FabricClientCommandSource, String> clanEditNew =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("new", StringArgumentType.string());
            clanEditNew.executes(ctx -> editClan(ctx.getSource(), ArmorSide.ENEMY, StringArgumentType.getString(ctx, "old"), StringArgumentType.getString(ctx, "new")));
            clanEditOld.then(clanEditNew);
            clanEditEnemy.then(clanEditOld);
            clanEdit.then(clanEditEnemy);
            clan.then(clanEdit);

            LiteralArgumentBuilder<FabricClientCommandSource> clanEditAlly =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("ally");
            RequiredArgumentBuilder<FabricClientCommandSource, String> clanEditAllyOld =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("old", StringArgumentType.string());
            RequiredArgumentBuilder<FabricClientCommandSource, String> clanEditAllyNew =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("new", StringArgumentType.string());
            clanEditAllyNew.executes(ctx -> editClan(ctx.getSource(), ArmorSide.ALLY, StringArgumentType.getString(ctx, "old"), StringArgumentType.getString(ctx, "new")));
            clanEditAllyOld.then(clanEditAllyNew);
            clanEditAlly.then(clanEditAllyOld);
            clanEdit.then(clanEditAlly);
            clan.then(clanEdit);

            LiteralArgumentBuilder<FabricClientCommandSource> clanEditOwn =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("own");
            RequiredArgumentBuilder<FabricClientCommandSource, String> clanEditOwnOld =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("old", StringArgumentType.string());
            RequiredArgumentBuilder<FabricClientCommandSource, String> clanEditOwnNew =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("new", StringArgumentType.string());
            clanEditOwnNew.executes(ctx -> editClan(ctx.getSource(), ArmorSide.OWN, StringArgumentType.getString(ctx, "old"), StringArgumentType.getString(ctx, "new")));
            clanEditOwnOld.then(clanEditOwnNew);
            clanEditOwn.then(clanEditOwnOld);
            clanEdit.then(clanEditOwn);
            clan.then(clanEdit);

            LiteralArgumentBuilder<FabricClientCommandSource> clanEditNeutral =
                    LiteralArgumentBuilder.<FabricClientCommandSource>literal("neutral");
            RequiredArgumentBuilder<FabricClientCommandSource, String> clanEditNeutralOld =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("old", StringArgumentType.string());
            RequiredArgumentBuilder<FabricClientCommandSource, String> clanEditNeutralNew =
                    RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("new", StringArgumentType.string());
            clanEditNeutralNew.executes(ctx -> editClan(ctx.getSource(), ArmorSide.NEUTRAL, StringArgumentType.getString(ctx, "old"), StringArgumentType.getString(ctx, "new")));
            clanEditNeutralOld.then(clanEditNeutralNew);
            clanEditNeutral.then(clanEditNeutralOld);
            clanEdit.then(clanEditNeutral);
            clan.then(clanEdit);

            dispatcher.register(root);
        });
    }

    private static int addClan(FabricClientCommandSource source, ArmorSide side, String clans) {
        String serverAddress = StrayArmorClient.currentServerAddress();
        List<String> names = new ArrayList<>();
        if (clans != null) {
            for (String part : clans.split(",")) {
                String cleaned = part == null ? "" : part.trim();
                if (!cleaned.isEmpty()) names.add(cleaned);
            }
        }
        boolean ok = StrayTagsConfig.addClan(serverAddress, side, names);
        source.sendFeedback(Component.literal(ok
                ? "\u00a7a[StrayArmor] Added clan(s) to the " + side + " list"
                : "\u00a7c[StrayArmor] No clan added (no matching category?)"));
        StrayArmorConfigManager.reload();
        ConfigInstaller.reloadStrayTags();
        return ok ? 1 : 0;
    }

    private static int editClan(FabricClientCommandSource source, ArmorSide side, String oldClan, String newClan) {
        String serverAddress = StrayArmorClient.currentServerAddress();
        boolean ok = StrayTagsConfig.editClan(serverAddress, side, oldClan, newClan);
        source.sendFeedback(Component.literal(ok
                ? "\u00a7a[StrayArmor] Renamed clan in the " + side + " list"
                : "\u00a7c[StrayArmor] No clan edited (not found?)"));
        StrayArmorConfigManager.reload();
        ConfigInstaller.reloadStrayTags();
        return ok ? 1 : 0;
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
