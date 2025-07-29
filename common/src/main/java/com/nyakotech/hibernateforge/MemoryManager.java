package com.nyakotech.hibernateforge;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

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

    private static final long GC_INTERVAL_MS = 30000;
    private static final double MEMORY_THRESHOLD = 0.8;

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
        }, 10, 30, TimeUnit.SECONDS);

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

            // 4. Compact data structures (It didn't work)
            //compactDataStructures(server);

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
        for (ServerLevel level : server.getAllLevels()) {
            // Keep only spawn chunks loaded

            var chunkSource = level.getChunkSource();
            var spawnPos = level.getSharedSpawnPos();
            int spawnX = spawnPos.getX() >> 4;
            int spawnZ = spawnPos.getZ() >> 4;

            // Force chunk saving before unloading
            CompletableFuture.runAsync(() -> {
                try {
                    chunkSource.save(true);
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
        for (ServerLevel level : server.getAllLevels()) {

            List<Entity> entities = level.getEntities(
                    EntityTypeTest.forClass(Entity.class),
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
                        entitiesToRemove.size(), level.dimension().location());
            }
        }
    }

    private static AABB getWorldBorderBoundingBox(ServerLevel level) {
        var border = level.getWorldBorder();
        double centerX = border.getCenterX();
        double centerZ = border.getCenterZ();
        double size = border.getSize();
        double halfSize = size / 2.0;

        return new AABB(
                centerX - halfSize, Double.NEGATIVE_INFINITY, centerZ - halfSize,
                centerX + halfSize, Double.POSITIVE_INFINITY, centerZ + halfSize
        );
    }

    /**
     * Check if an entity can be removed during hibernation
     */

    private static boolean canEntityBeRemovedDuringHibernation(Entity entity) {
        // Only remove temporary entities/visual effects
        // DO NOT remove items on the ground, important mods, etc.
        String entityType = entity.getType().toString();

        return entityType.contains("experience_orb") ||
                entityType.contains("firework") ||
                entityType.contains("arrow") ||
                (entity.tickCount > 6000 && entityType.contains("item"));
    }

    /*
      Compacts server data structures (Did NOT work)

    private static void compactDataStructures(MinecraftServer server) {
        // Compacts registries and cache
        try {
            // Clears recipe caches
            server.getRecipeManager().byType.forEach((type, recipes) -> {
                if (recipes instanceof java.util.HashMap) {
                    ((java.util.HashMap<?, ?>) recipes).trimToSize();
                }
            });
        } catch (Exception e) {
            Constants.LOG.error("Error while compacting data structures: ", e);
        }
    }*/

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

        // Forces GC if memory usage exceeds 80% or sufficient time has passed
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
            server.saveEverything(false, false, true);

            // Saves player data
            server.getPlayerList().saveAll();

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