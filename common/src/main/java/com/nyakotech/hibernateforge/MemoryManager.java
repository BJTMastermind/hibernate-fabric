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
 * Sistema de gerenciamento de memória para hibernação
 */

public class MemoryManager {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean memoryOptimizationActive = false;
    private static long lastGCTime = 0;

    // Configurações ajustáveis

    private static final long GC_INTERVAL_MS = 30000;
    private static final int CHUNKS_TO_UNLOAD_PER_TICK = 10;
    private static final double MEMORY_THRESHOLD = 0.8;

    /*
     * Inicia o sistema de otimização
     */
    public static void startMemoryOptimization(MinecraftServer server) {
        if (memoryOptimizationActive) return;

        memoryOptimizationActive = true;
        Constants.LOG.info("Starting Memory Optimization for hibernation");

        // Agenda limpeza periódica de memória

        scheduler.scheduleAtFixedRate(() -> {
            if (Hibernation.isHibernating()) {
                performMemoryCleanup(server);
            }
        }, 10, 30, TimeUnit.SECONDS);

        // Força garbage collection inicial
        performGarbageCollection();
    }
    /*
     * Para o sistema de otimização de memória
     */

    public static void stopMemoryOptimization() {
        if (!memoryOptimizationActive) return;

        memoryOptimizationActive = false;
        Constants.LOG.info("Stopping Memory Optimization for hibernation");

        // Não cancela o scheduler para evitar problemas, apenas para as operações
    }

    /*
     * Executa limpeza completa de memória
     */

    private static void performMemoryCleanup(MinecraftServer server) {
        try {
            // 1. Descarrega chunks desnecessários
            unloadUnnecessaryChunks(server);

            // 2. Limpa entidades inativas
            cleanupInactiveEntities(server);

            // 3. Força garbage collection se necessário
            if (shouldForceGC()) {
                performGarbageCollection();
            }

            // 4. Compacta estruturas de dados
            //compactDataStructures(server);

            // Log do uso de memória
            logMemoryUsage();
        } catch (Exception e) {
            Constants.LOG.error("Erro durante limpeza de memória: ", e);
        }
    }

    /**
     * Descarrega chuncks que não são necessários durante hibernação
     */

    private static void unloadUnnecessaryChunks(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            // Mantém apenas chunks do spawn carregados

            var chunkSource = level.getChunkSource();
            var spawnPos = level.getSharedSpawnPos();
            int spawnX = spawnPos.getX() >> 4;
            int spawnZ = spawnPos.getZ() >> 4;

            // Força o salvamento de chunnks antes de descarregar
            CompletableFuture.runAsync(() -> {
                try {
                    chunkSource.save(true);
                } catch (RuntimeException e) {
                    Constants.LOG.warn("Erro ao salvar chunks: ", e);
                }
            });
        }
    }
    /**
     * Remove entidades que podem ser seguramente removidas durante hibernação
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
                Constants.LOG.debug("Removidas {} entidades inativas do nível {}",
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
     * Verifica se uma entidade pode ser removida durante hibernação
     */

    private static boolean canEntityBeRemovedDuringHibernation(Entity entity) {
        // Remove apenas entidades temporárias/efeitos visuais
        // NÃO remove itens no chão, mods importantes, etc.
        String entityType = entity.getType().toString();

        return entityType.contains("experience_orb") ||
                entityType.contains("firework") ||
                entityType.contains("arrow") ||
                (entity.tickCount > 6000 && entityType.contains("item")); // Itens muito antigos
    }

    /*
      Compacta estruturas de dados do servidor
     NÃO funcionou

    private static void compactDataStructures(MinecraftServer server) {
        // Compacta registries e cache
        try {
            // Limpa caches de receitas
            server.getRecipeManager().byType.forEach((type, recipes) -> {
                if (recipes instanceof java.util.HashMap) {
                    ((java.util.HashMap<?, ?>) recipes).trimToSize();
                }
            });
        } catch (Exception e) {
            Constants.LOG.error("Erro ao compactar data structures: ", e);
        }
    }*/

    /**
     * Verifica se deve forçar garbage collection
     */

    private static boolean shouldForceGC() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        double memoryUsagePercent = (double) usedMemory / (double) maxMemory;

        // Força GC se usar mais que 80% da memória ou se passou tempo suficiente
        return memoryUsagePercent >  MEMORY_THRESHOLD ||
                (System.currentTimeMillis() - lastGCTime > GC_INTERVAL_MS);
    }

    /**
     * Executa garbage collection
     */

    private static void performGarbageCollection() {
        long beforeGC = getUsedMemoryMB();
        long startTime = System.currentTimeMillis();

        // Sugere GC múltiplas vezes para ser mais efetivo
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

        Constants.LOG.info("GC executado: {}MB liberados em {}ms (Antes: {}MB, Depois: {}MB)",
                memoryFreed, gcTime, beforeGC, afterGC);
    }

    /**
     * Obtém o uso atual de memória em MB
     */
    private static long getUsedMemoryMB() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }

    /**
     * Obtém a memória máxima disponível em MB
     */
    private static long getMaxMemoryMB() {
        return Runtime.getRuntime().maxMemory() / (1024 * 1024);
    }

    /**
     * Registra informações sobre uso de memória
     */
    private static void logMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory() / (1024 * 1024);

        double usagePercent = (double) usedMemory / (double) maxMemory * 100;
        double formattedUsagePercent = Math.round(usagePercent * 10.0) / 10.0;

        Constants.LOG.info("Memória: {}MB usada / {}MB máxima ({}%) - Livre: {}MB",
                usedMemory, maxMemory, formattedUsagePercent, freeMemory);
    }

    /**
     * Salva dados importantes antes da otimização de memória
     */

    public static void saveImportantData(MinecraftServer server) {
        Constants.LOG.info("Salvando dados importantes antes da hibernação...");

        try {
            // Salva o mundo
            server.saveEverything(false, false, true);

            // Salva os dados dos jogadores
            server.getPlayerList().saveAll();

            Constants.LOG.info("Dados salvos com sucesso");
        } catch (Exception e) {
            Constants.LOG.error("Erro ao salvar dados importantes: ", e);
        }
    }

    /**
     * Shutdown gracioso do sistema
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