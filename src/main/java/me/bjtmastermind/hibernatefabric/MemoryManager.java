package me.bjtmastermind.hibernatefabric;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

/**
 * Memory Management System for Hibernation
 */
public class MemoryManager {
    // Do not cancel the scheduler to avoid issues — only pause operations
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final long GC_INTERVAL_MS = Config.gcIntervalSeconds * 1000L;
    private static final double MEMORY_THRESHOLD = Config.memoryThresholdPercent / 100.0;

    private static boolean memoryOptimizationActive = false;
    private static long lastGCTime = 0;

    public static void startMemoryOptimization(MinecraftServer server) {
        if (!Config.enableMemoryOptimization) {
            return;
        }

        memoryOptimizationActive = true;
        HibernateFabric.LOGGER.info("Starting Memory Optimization for hibernation");

        // Schedule periodic memory cleanup
        scheduler.scheduleAtFixedRate(() -> {
            if (HibernateFabric.isHibernating()) {
                performMemoryCleanup(server);
            }
        }, 0, Config.memoryCleanupIntervalSeconds, TimeUnit.SECONDS);

        // Force initial garbage collection
        performGarbageCollection();
    }

    public static void stopMemoryOptimization() {
        if (!memoryOptimizationActive) {
            return;
        }

        memoryOptimizationActive = false;
        HibernateFabric.LOGGER.info("Stopping Memory Optimization for hibernation");
    }

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

            logMemoryUsage();
        } catch (Exception e) {
            HibernateFabric.LOGGER.error("Error during memory cleanup: ", e);
        }
    }

    private static void unloadUnnecessaryChunks(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            ServerChunkCache chunkManager = level.getChunkSource();

            // Force chunk saving before unloading
            CompletableFuture.runAsync(() -> {
                try {
                    chunkManager.save(true);
                } catch (RuntimeException e) {
                    HibernateFabric.LOGGER.warn("Error saving chunks: ", e);
                }
            });
        }
    }

    // Remove entities that can be safely deleted during hibernation
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
                HibernateFabric.LOGGER.info(
                    "{} inactive entities removed from the level {}",
                    entitiesToRemove.size(),
                    level.dimensionTypeRegistration().getRegisteredName()
                );
            }
        }
    }

    private static AABB getWorldBorderBoundingBox(ServerLevel level) {
        WorldBorder border = level.getWorldBorder();
        double centerX = border.getCenterX();
        double centerZ = border.getCenterZ();
        double size = border.getSize();
        double halfSize = size / 2.0;

        return new AABB(
            centerX - halfSize, Double.MIN_VALUE, centerZ - halfSize,
            centerX + halfSize, Double.MAX_VALUE, centerZ + halfSize
        );
    }

    private static boolean canEntityBeRemovedDuringHibernation(Entity entity) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

        if (Config.removeEntities.contains(ResourceLocation.parse("minecraft:item")) &&
            entityId.equals(ResourceLocation.parse("minecraft:item")))
        {
            return entity.tickCount >= (Config.droppedItemMaxAgeSeconds * 20);
        } else {
            return Config.removeEntities.contains(entityId) && !entity.hasCustomName();
        }
    }

    // Check if garbage collection should be forced
    private static boolean shouldForceGC() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        double memoryUsagePercent = (double) usedMemory / (double) maxMemory;

        // Forces GC if memory usage exceeds threshold percentage defined in config (Default: 80%) or sufficient time has passed
        return memoryUsagePercent > MEMORY_THRESHOLD || (System.currentTimeMillis() - lastGCTime > GC_INTERVAL_MS);
    }

    private static void performGarbageCollection() {
        long beforeGC = getUsedMemoryMB();
        long startTime = System.currentTimeMillis();

        // Run GC multiple times to be more effective
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

        HibernateFabric.LOGGER.info(
            "GC executed: {}MB freed in {}ms (Before: {}MB, After: {}MB)",
            memoryFreed, gcTime, beforeGC, afterGC
        );
    }

    private static long getUsedMemoryMB() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }

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

        HibernateFabric.LOGGER.info(
            "Memory: {}MB used / {}MB max ({}%) — Free: {}MB",
            usedMemory, maxMemory, formattedUsagePercent, freeMemory
        );
    }

    public static void saveImportantData(MinecraftServer server) {
        HibernateFabric.LOGGER.info("Saving important data before hibernation...");

        try {
            // Saves the world
            server.saveEverything(false, false, true);

            // Saves player data
            server.getPlayerList().saveAll();

            HibernateFabric.LOGGER.info("Data saved successfully");
        } catch (Exception e) {
            HibernateFabric.LOGGER.error("Error while saving important data: ", e);
        }
    }

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