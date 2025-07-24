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
                        // Use CommonConfig, not a non-existent Config class
                        .requires(src -> src.hasPermission(CommonConfig.permissionLevel))
                        .executes(HibernationCommand::execute)
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        boolean newState = !HibernateforgeFabric.isHibernating();
        HibernateforgeFabric.setHibernationState(server, newState);

        ctx.getSource().sendSuccess(
                () -> Component.literal("Hibernation set to " + newState),
                true
        );
        return 1;
    }
}
