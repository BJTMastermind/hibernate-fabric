package com.nyakotech.hibernateforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.minecraft.server.MinecraftServer;

/**
 * Main entry point for NeoForge with full hibernation and memory optimization system
 */
@Mod(Constants.MOD_ID)
public class HibernateforgeNeoforge {
    private static boolean hibernating = false;

    public HibernateforgeNeoforge(IEventBus modBus) {
        Constants.LOG.info("Loading NeoForge module from Hibernateforge");

        // Load settings
        ConfigManager.loadConfig();

        // Initialize common class
        CommonClass.init();

        // Define initial hibernation state
        hibernating = CommonConfig.startEnabled;
        Hibernation.setHibernating(hibernating);

        // Register events on the game bus
        IEventBus gameEventBus = NeoForge.EVENT_BUS;
        gameEventBus.addListener(this::onServerStarted);
        gameEventBus.addListener(this::onServerStopping);
        gameEventBus.addListener(GameRuleHandler::onServerStarted);
        gameEventBus.addListener(HibernationCommand::register);
        gameEventBus.addListener(GameRuleHandler::onPlayerLogin);
        gameEventBus.addListener(GameRuleHandler::onPlayerLogout);
        gameEventBus.addListener(TickEventHandler::onServerTick);
        gameEventBus.addListener(ChunkUnloadHandler::onLevelTick);

        Constants.LOG.info("Hibernateforge NeoForge successfully initialized");
    }

    /**
     * Called when the server finishes initializing
     */
    private void onServerStarted(ServerStartedEvent event) {
        Constants.LOG.info("Server started — Hibernateforge active");

        // If enabled, start hibernation if no players are present
        if (CommonConfig.startEnabled && event.getServer().getPlayerCount() == 0) {
            setHibernationState(event.getServer(), true);
        }
    }

    /**
     * Called when the server is stopping
     */
    private void onServerStopping(ServerStoppingEvent event) {
        Constants.LOG.info("Server stopping — disabling hibernation");

        // For memory optimization before shutdown
        if (CommonConfig.enableMemoryOptimization) {
            MemoryManager.stopMemoryOptimization();
            MemoryManager.shutdown();
        }

        hibernating = false;
        Hibernation.setHibernating(false);
    }

    /**
     * Exposed for command logic and other systems
     */
    public static boolean isHibernating() {
        return hibernating;
    }

    /**
     * Set hibernation state with all optimizations
     */
    public static void setHibernationState(MinecraftServer server, boolean state) {
        boolean wasHibernating = hibernating;
        hibernating = state;

        // Update global state
        Hibernation.setHibernating(state);

        // Update game rules
        GameRuleHandler.setHibernationGameRules(server, state);

        // Manage memory optimization
        if (CommonConfig.enableMemoryOptimization) {
            if (state && !wasHibernating) {
                // Entering hibernation
                Constants.LOG.info("Entering hibernation mode — starting optimizations");

                if (CommonConfig.saveBeforeHibernation) {
                    MemoryManager.saveImportantData(server);
                }

                MemoryManager.startMemoryOptimization(server);

            } else if (!state && wasHibernating) {
                // Exiting hibernation
                Constants.LOG.info("Exiting hibernation mode — stopping optimizations");
                MemoryManager.stopMemoryOptimization();
            }
        }

        // State Change Log
        Constants.LOG.info("Hibernation state changed: {} -> {}",
                wasHibernating ? "HIBERNATING" : "ACTIVE",
                state ? "HIBERNATING" : "ACTIVE");
    }

    /**
     * Force garbage collection (for use by commands)
     */
    public static void forceGarbageCollection() {
        if (CommonConfig.enableMemoryOptimization && CommonConfig.forceGarbageCollection) {
            long beforeGC = getUsedMemoryMB();
            System.gc();

            // Short pause to allow GC to complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            long afterGC = getUsedMemoryMB();
            long memoryFreed = beforeGC - afterGC;

            Constants.LOG.info("Manual GC executed: {}MB freed (Before: {}MB, After: {}MB))",
                    memoryFreed, beforeGC, afterGC);
        }
    }

    /**
     * Retrieve memory usage information
     */
    public static String getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long availableMemory = maxMemory - usedMemory;

        double usagePercent = (double) usedMemory / maxMemory * 100;
        long totalHeapMB = runtime.totalMemory() / (1024 * 1024);

        return String.format(
                "Memory: %dMB used / %dMB max (%.1f%%) — Available: %dMB [Current heap: %dMB]",
                usedMemory, maxMemory, usagePercent, availableMemory, totalHeapMB
        );
    }

    /**
     * Retrieve used memory in MB
     */
    private static long getUsedMemoryMB() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }

    /**
     * Check if the optimization system is active
     */
    public static boolean isMemoryOptimizationEnabled() {
        return CommonConfig.enableMemoryOptimization;
    }

    /**
     * Reload configuration from file
     */
    public static void reloadConfig() {
        Constants.LOG.info("Reloading configurations...");
        ConfigManager.loadConfig();
        Constants.LOG.info("Configurations reloaded successfully");
    }

    /**
     * Save current configurations to file
     */
    public static void saveConfig() {
        ConfigManager.saveConfig();
    }
}