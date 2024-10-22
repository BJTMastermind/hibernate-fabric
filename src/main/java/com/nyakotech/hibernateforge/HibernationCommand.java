package com.nyakotech.hibernateforge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HibernateForge.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HibernationCommand {
    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("hibernate")
                .requires(cs -> cs.hasPermission(Config.permissionLevel))
                .executes(HibernationCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        if (HibernateForge.isHibernating()) {
            HibernateForge.setHibernationState(false);
        } else {
            HibernateForge.setHibernationState(true);
        }
        context.getSource().sendSuccess(() -> Component.literal("Hibernation set to " + HibernateForge.isHibernating()), true); // Wrap in Supplier
        return Command.SINGLE_SUCCESS;
    }
}
