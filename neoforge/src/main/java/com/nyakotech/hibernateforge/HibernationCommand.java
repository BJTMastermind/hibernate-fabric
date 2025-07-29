package com.nyakotech.hibernateforge;

import com.mojang.brigadier.context.CommandContext;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class HibernationCommand {
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("hibernate")
                        .requires(src -> src.hasPermission(CommonConfig.permissionLevel))
                        .executes(HibernationCommand::execute)
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        boolean currentState = Hibernation.isHibernating();
        boolean newState = !currentState;

        // Do not allow hibernation with players online
        if (newState && !server.getPlayerList().getPlayers().isEmpty()) {
            ctx.getSource().sendFailure(
                    Component.literal("§cHibernation not possible with players online! (" +
                            server.getPlayerList().getPlayers().size() + " connected players)")
            );
            return 0;
        }

        // Warn when attempting to disable hibernation without players
        if (!newState && server.getPlayerList().getPlayers().isEmpty()) {
            ctx.getSource().sendSuccess(
                    () -> Component.literal("§eAttention: Disabling hibernation with no players online. " +
                            "Hibernation will be automatically reactivated."),
                    true
            );
        }

        Hibernation.setHibernating(newState);
        GameRuleHandler.setHibernationGameRules(server, newState);

        String status = newState ? "§aactivated" : "§cdeactivated";
        ctx.getSource().sendSuccess(
                () -> Component.literal("§fHibernation " + status + "§f!"),
                true
        );

        return 1;
    }
}