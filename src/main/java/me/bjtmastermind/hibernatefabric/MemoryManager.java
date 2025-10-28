package me.bjtmastermind.hibernatefabric;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.entity.Entity;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/*
 * Memory Management System for Hibernation
 */

public class MemoryManager {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean memoryOptimizationActive = false;
    private static long lastGCTime = 0;

    // Adjustable settings

    private static final long GC_INTERVAL_MS = Config.gcIntervalSeconds * 1000L;
    private static final double MEMORY_THRESHOLD = Config.memoryThresholdPercent / 100.0;

    /*
     * Start Memory Optimization
     */
    public static void startMemoryOptimization(MinecraftServer server) {
        if (memoryOptimizationActive) return;

        memoryOptimizationActive = true;
        Constants.LOG.info("Starting Memory Optimization for hibernation");

        // Schedule periodic memory cleanup

        scheduler.scheduleAtFixedRate(() -> {
            if (Hibernation.isHibernating()) {
                performMemoryCleanup(server);
            }
        }, 10, Config.memoryCleanupIntervalSeconds, TimeUnit.SECONDS);

        // Force initial garbage collection
        performGarbageCollection();
    }
    /*
     * For the memory optimization system
     */

    public static void stopMemoryOptimization() {
        if (!memoryOptimizationActive) return;

        memoryOptimizationActive = false;
        Constants.LOG.info("Stopping Memory Optimization for hibernation");

        // Do not cancel the scheduler to avoid issues — only pause operations
    }

    /*
     * Execute full memory cleanup
     */

    private static void performMemoryCleanup(MinecraftServer server) {
        try {
            // 1. Unload unnecessary chunks
            unloadUnnecessaryChunks(server);

            // 2. Clean inactive entities
            cleanupInactiveEntities(server);

            // 3. Force garbage collection if needed
            if (shouldForceGC()) {
                performGarbageCollection();
            }

            // Memory usage log
            logMemoryUsage();
        } catch (Exception e) {
            Constants.LOG.error("Error during memory cleanup: ", e);
        }
    }

    /**
     * Unload unnecessary chunks during hibernation
     */

    private static void unloadUnnecessaryChunks(MinecraftServer server) {
        for (ServerWorld level : server.getWorlds()) {
            ServerChunkManager chunkManager = level.getChunkManager();

            // Force chunk saving before unloading
            CompletableFuture.runAsync(() -> {
                try {
                    chunkManager.save(true);
                } catch (RuntimeException e) {
                    Constants.LOG.warn("Error saving chunks: ", e);
                }
            });
        }
    }
    /**
     * Remove entities that can be safely deleted during hibernation
     */
    private static void cleanupInactiveEntities(MinecraftServer server) {
        for (ServerWorld level : server.getWorlds()) {

            List<Entity> entities = level.getEntitiesByType(
                    TypeFilter.instanceOf(Entity.class),
                    getWorldBorderBoundingBox(level),
                    entity -> true
            );

            List<Entity> entitiesToRemove = entities.stream()
                    .filter(MemoryManager::canEntityBeRemovedDuringHibernation)
                    .toList();

            for (Entity entity : entitiesToRemove) {
                entity.discard();
            }

            if (!entitiesToRemove.isEmpty()) {
                Constants.LOG.debug("{} inactive entities removed from the level {}",
                        entitiesToRemove.size(), level.getDimensionEntry().getIdAsString());
            }
        }
    }

    private static Box getWorldBorderBoundingBox(ServerWorld level) {
        var border = level.getWorldBorder();
        double centerX = border.getCenterX();
        double centerZ = border.getCenterZ();
        double size = border.getSize();
        double halfSize = size / 2.0;

        return new Box(
                centerX - halfSize, Double.NEGATIVE_INFINITY, centerZ - halfSize,
                centerX + halfSize, Double.POSITIVE_INFINITY, centerZ + halfSize
        );
    }

    /**
     * Check if an entity can be removed during hibernation
     */

    private static boolean canEntityBeRemovedDuringHibernation(Entity entity) {
        // Only remove temporary entities/visual effects
        // DO NOT remove items on the ground, important mobs, etc.
        String entityType = entity.getType().toString();

        return (entityType.contains("experience_orb") && Config.removeExperienceOrbs) ||
                (entityType.contains("firework") ||
                entityType.contains("arrow") && Config.removeProjectiles) ||
                (entity.age > (Config.droppedItemMaxAgeSeconds * 20) && entityType.contains("item") && Config.removeOldDroppedItems);
    }

    /**
     * Check if garbage collection should be forced
     */

    private static boolean shouldForceGC() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        double memoryUsagePercent = (double) usedMemory / (double) maxMemory;

        // Forces GC if memory usage exceeds threshold percentage defined in config (Default: 80%) or sufficient time has passed
        return memoryUsagePercent >  MEMORY_THRESHOLD ||
                (System.currentTimeMillis() - lastGCTime > GC_INTERVAL_MS);
    }

    /**
     * Performs garbage collection
     */

    private static void performGarbageCollection() {
        long beforeGC = getUsedMemoryMB();
        long startTime = System.currentTimeMillis();

        // Suggests GC multiple times to be more effective
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.gc();

        long afterGC = getUsedMemoryMB();
        long gcTime = System.currentTimeMillis() - startTime;
        long memoryFreed = beforeGC - afterGC;

        lastGCTime = System.currentTimeMillis();

        Constants.LOG.info("GC executed: {}MB freed in {}ms (Before: {}MB, After: {}MB)",
                memoryFreed, gcTime, beforeGC, afterGC);
    }

    /**
     * Gets current memory usage in MB
     */
    private static long getUsedMemoryMB() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }

    /**
     * Records memory usage information
     */
    private static void logMemoryUsage() {
        if (!Config.logMemoryUsage) {
            return;
        }

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory() / (1024 * 1024);

        double usagePercent = (double) usedMemory / (double) maxMemory * 100;
        double formattedUsagePercent = Math.round(usagePercent * 10.0) / 10.0;

        Constants.LOG.info("Memory: {}MB used / {}MB max ({}%) — Free: {}MB",
                usedMemory, maxMemory, formattedUsagePercent, freeMemory);
    }

    /**
     * Saves important data before memory optimization
     */

    public static void saveImportantData(MinecraftServer server) {
        Constants.LOG.info("Saving important data before hibernation...");

        try {
            // Saves the world
            server.saveAll(false, false, true);

            // Saves player data
            server.getPlayerManager().saveAllPlayerData();

            Constants.LOG.info("Data saved successfully");
        } catch (Exception e) {
            Constants.LOG.error("Error while saving important data: ", e);
        }
    }

    /**
     * Graceful system shutdown
     */
    public static void shutdown() {
        memoryOptimizationActive = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}