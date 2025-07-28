package com.nyakotech.hibernateforge;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class HibernationCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("hibernate")
                        .requires(src -> src.hasPermission(CommonConfig.permissionLevel))
                        .executes(HibernationCommand::execute)
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        boolean currentState = HibernateforgeFabric.isHibernating();
        boolean newState = !currentState;

        // PROTEÇÃO: Não permitir hibernar com players online
        if (newState && !server.getPlayerList().getPlayers().isEmpty()) {
            ctx.getSource().sendFailure(
                    Component.literal("§cCannot hibernate while players are online! (" +
                            server.getPlayerList().getPlayers().size() + " connected players)")
            );
            return 0;
        }

        // PROTEÇÃO: Avisar se tentando desligar hibernação sem players
        if (!newState && server.getPlayerList().getPlayers().isEmpty()) {
            ctx.getSource().sendSuccess(
                    () -> Component.literal("§eWarning: Disabling hibernation while no players are online. " +
                            "Hibernation will be reactivated automatically."),
                    true
            );
        }

        HibernateforgeFabric.setHibernationState(server, newState);

        String status = newState ? "§aactivated" : "§cdeactivated";
        ctx.getSource().sendSuccess(
                () -> Component.literal("§fHibernation " + status + "§f!"),
                true
        );

        return 1;
    }
}