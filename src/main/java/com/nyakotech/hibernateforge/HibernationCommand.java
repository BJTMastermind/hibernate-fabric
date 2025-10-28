package com.nyakotech.hibernateforge;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.MinecraftServer;

public class HibernationCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("hibernate")
                        .requires(src -> src.hasPermissionLevel(Config.permissionLevel))
                        .executes(HibernationCommand::execute)
                )
        );
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        boolean currentState = HibernateFabric.isHibernating();
        boolean newState = !currentState;

        // Do not allow hibernation with players online
        if (newState && server.getCurrentPlayerCount() >= 1) {
            ctx.getSource().sendError(
                    Text.literal("§cCannot hibernate while players are online! (" +
                            server.getPlayerManager().getPlayerList().size() + " connected players)")
            );
            return 0;
        }

        // Warn if trying to disable hibernation with no players online
        if (!newState && server.getCurrentPlayerCount() == 0) {
            ctx.getSource().sendFeedback(
                    () -> Text.literal("§eWarning: Disabling hibernation while no players are online. " +
                            "Hibernation will be reactivated automatically."),
                    true
            );
        }

        HibernateFabric.setHibernationState(server, newState);

        String status = newState ? "§aactivated" : "§cdeactivated";
        ctx.getSource().sendFeedback(
                () -> Text.literal("§fHibernation " + status + "§f!"),
                true
        );

        return 1;
    }
}