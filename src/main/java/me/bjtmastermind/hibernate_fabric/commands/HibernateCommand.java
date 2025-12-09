package me.bjtmastermind.hibernate_fabric.commands;

import com.mojang.brigadier.context.CommandContext;

import me.bjtmastermind.hibernate_fabric.HibernateFabric;
import me.bjtmastermind.hibernate_fabric.config.Config;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public class HibernateCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("hibernate")
                .requires(src -> src.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(Config.permissionLevel))))
                .executes(HibernateCommand::toggleHibernation)
                .then(Commands.literal("status")
                    .executes(HibernateCommand::showStatus)
                )
                .then(Commands.literal("memory")
                    .executes(HibernateCommand::showMemoryInfo)
                )
                .then(Commands.literal("gc")
                    .executes(HibernateCommand::forceGarbageCollection)
                )
            );
        });
    }

    private static int toggleHibernation(CommandContext<CommandSourceStack> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        boolean newState = !HibernateFabric.isHibernating();

        // Do not allow hibernation with players online
        if (newState && server.getPlayerCount() >= 1) {
            ctx.getSource().sendFailure(
                Component.literal("Cannot hibernate while players are online! (" +
                    server.getPlayerCount() + " connected player" +
                    (server.getPlayerCount() == 1 ? ")" : "s)")
                ).withColor(TextColor.fromLegacyFormat(ChatFormatting.RED).getValue())
            );
            return 0;
        }

        // Warn if trying to disable hibernation with no players online
        if (!newState && server.getPlayerCount() == 0) {
            ctx.getSource().sendSuccess(
                () -> Component.literal("Warning: Disabling hibernation while no players are online. " +
                    "Hibernation will be reactivated automatically."),
                true
            );
        }

        HibernateFabric.setHibernationState(server, newState);

        ctx.getSource().sendSuccess(
            () -> Component.literal("Hibernation " + (newState ? "activated" : "deactivated")),
            true
        );
        return 1;
    }

    private static int showStatus(CommandContext<CommandSourceStack> ctx) {
        boolean hibernating = HibernateFabric.isHibernating();
        int playerCount = ctx.getSource().getServer().getPlayerCount();

        ctx.getSource().sendSuccess(() -> Component.literal(
            "Hibernation Status:\n" +
            "- State: " + (hibernating ? "HIBERNATING" : "AWAKE") + "\n" +
            "- Players online: " + playerCount + "\n" +
            "- Memory optimization: " + (Config.enableMemoryOptimization ? "ACTIVE" : "INACTIVE")
        ), false);

        return 1;
    }

    private static int showMemoryInfo(CommandContext<CommandSourceStack> ctx) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory() / (1024 * 1024);

        double usagePercent = (double) usedMemory / maxMemory * 100;

        ctx.getSource().sendSuccess(() -> Component.literal(
            "Memory Information:\n" +
            "- Memory used: " + usedMemory + "MB\n" +
            "- Total allocated memory: " + totalMemory + "MB\n" +
            "- Maximum memory: " + maxMemory + "MB\n" +
            "- Usage: " + String.format("%.1f%%", usagePercent) + "\n" +
            "- Free memory: " + freeMemory + "MB"
        ), false);

        return 1;
    }

    private static int forceGarbageCollection(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("Performing memory cleanup..."), false);

        long beforeGC = getUsedMemoryMB();
        System.gc();

        // Waits a bit for the GC to complete
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long afterGC = getUsedMemoryMB();
        long memoryFreed = beforeGC - afterGC;

        ctx.getSource().sendSuccess(() -> Component.literal(
            "Memory cleanup completed!\n" +
            "- Memory before: " + beforeGC + "MB\n" +
            "- Memory after: " + afterGC + "MB\n" +
            "- Memory freed: " + memoryFreed + "MB"
        ), false);

        return 1;
    }

    private static long getUsedMemoryMB() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }
}