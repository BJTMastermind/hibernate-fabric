package com.nyakotech.hibernateforge;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;

public class MemoryCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("hibernate")
                    .requires(src -> src.hasPermissionLevel(Config.permissionLevel))
                    .executes(MemoryCommand::toggleHibernation)
                    .then(CommandManager.literal("status")
                            .executes(MemoryCommand::showStatus))
                    .then(CommandManager.literal("memory")
                            .executes(MemoryCommand::showMemoryInfo))
                    .then(CommandManager.literal("gc")
                            .executes(MemoryCommand::forceGarbageCollection))
            );
        });
    }

    private static int toggleHibernation(CommandContext<ServerCommandSource> ctx) {
        MinecraftServer server = ctx.getSource().getServer();
        boolean newState = !HibernateFabric.isHibernating();
        HibernateFabric.setHibernationState(server, newState);

        ctx.getSource().sendFeedback(
                () -> Text.literal("Hibernation " + (newState ? "activated" : "deactivated")),
                true
        );
        return 1;
    }

    private static int showStatus(CommandContext<ServerCommandSource> ctx) {
        boolean hibernating = HibernateFabric.isHibernating();
        int playerCount = ctx.getSource().getServer().getCurrentPlayerCount();

        ctx.getSource().sendFeedback(() -> Text.literal(
                "Hibernation Status:\n" +
                        "- State: " + (hibernating ? "HIBERNATING" : "ACTIVE") + "\n" +
                        "- Players online: " + playerCount + "\n" +
                        "- Memory optimization: " + (Config.enableMemoryOptimization ? "ACTIVE" : "INACTIVE")
        ), false);

        return 1;
    }

    private static int showMemoryInfo(CommandContext<ServerCommandSource> ctx) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory() / (1024 * 1024);

        double usagePercent = (double) usedMemory / maxMemory * 100;

        ctx.getSource().sendFeedback(() -> Text.literal(
                "Memory Information:\n" +
                        "- Memory used: " + usedMemory + "MB\n" +
                        "- Total allocated memory: " + totalMemory + "MB\n" +
                        "- Maximum memory: " + maxMemory + "MB\n" +
                        "- Usage: " + String.format("%.1f%%", usagePercent) + "\n" +
                        "- Free memory: " + freeMemory + "MB"
        ), false);

        return 1;
    }

    private static int forceGarbageCollection(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(() -> Text.literal("Performing memory cleanup..."), false);

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

        ctx.getSource().sendFeedback(() -> Text.literal(
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