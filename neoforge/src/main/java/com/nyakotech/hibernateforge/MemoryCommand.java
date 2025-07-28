package com.nyakotech.hibernateforge;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class MemoryCommand {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("hibernate")
                .requires(src -> src.hasPermission(CommonConfig.permissionLevel))
                .executes(MemoryCommand::toggleHibernation)
                .then(Commands.literal("status")
                        .executes(MemoryCommand::showStatus))
                .then(Commands.literal("memory")
                        .executes(MemoryCommand::showMemoryInfo))
                .then(Commands.literal("gc")
                        .executes(MemoryCommand::forceGarbageCollection))
        );
    }

    private static int toggleHibernation(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        boolean newState = !HibernateforgeNeoforge.isHibernating();
        HibernateforgeNeoforge.setHibernationState(server, newState);

        ctx.getSource().sendSuccess(
                () -> Component.literal("Hibernação " + (newState ? "ativada" : "desativada")),
                true
        );
        return 1;
    }

    private static int showStatus(CommandContext<CommandSourceStack> ctx) {
        boolean hibernating = HibernateforgeNeoforge.isHibernating();
        int playerCount = ctx.getSource().getServer().getPlayerCount();

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Status da Hibernação:\n" +
                        "- Estado: " + (hibernating ? "HIBERNANDO" : "ATIVO") + "\n" +
                        "- Jogadores online: " + playerCount + "\n" +
                        "- Otimização de memória: " + (HibernateforgeNeoforge.isMemoryOptimizationEnabled() ? "ATIVA" : "INATIVA")
        ), false);

        return 1;
    }

    private static int showMemoryInfo(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Informações de Memória:\n" + HibernateforgeNeoforge.getMemoryInfo()
        ), false);

        return 1;
    }

    private static int forceGarbageCollection(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("Executando limpeza de memória..."), false);

        HibernateforgeNeoforge.forceGarbageCollection();

        ctx.getSource().sendSuccess(() -> Component.literal(
                "Limpeza de memória executada!\n" +
                        HibernateforgeNeoforge.getMemoryInfo()
        ), false);

        return 1;
    }

}