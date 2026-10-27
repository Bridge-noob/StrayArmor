package io.github.colusite.strayarmor;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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

            dispatcher.register(root);
        });
    }
}
