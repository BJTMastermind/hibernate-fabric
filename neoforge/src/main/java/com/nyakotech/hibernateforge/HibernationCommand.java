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
        boolean newState = !Hibernation.isHibernating();
        Hibernation.setHibernating(newState);
        GameRuleHandler.setHibernationGameRules(server, newState);
        ctx.getSource().sendSuccess(() -> Component.literal("Hibernation set to " + newState), true);
        return 1;
    }
}
