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
 * Ponto de entrada principal para NeoForge com sistema completo de hibernação e otimização de memória
 */
@Mod(Constants.MOD_ID)
public class HibernateforgeNeoforge {
    private static boolean hibernating = false;

    public HibernateforgeNeoforge(IEventBus modBus) {
        Constants.LOG.info("Carregando módulo NeoForge do Hibernateforge");

        // Carrega configurações
        ConfigManager.loadConfig();

        // Inicializa classe comum
        CommonClass.init();

        // Define estado inicial da hibernação
        hibernating = CommonConfig.startEnabled;
        Hibernation.setHibernating(hibernating);

        // Registra eventos no barramento do jogo
        IEventBus gameEventBus = NeoForge.EVENT_BUS;
        gameEventBus.addListener(this::onServerStarted);
        gameEventBus.addListener(this::onServerStopping);
        gameEventBus.addListener(HibernationCommand::register);
        gameEventBus.addListener(GameRuleHandler::onPlayerLogin);
        gameEventBus.addListener(GameRuleHandler::onPlayerLogout);
        gameEventBus.addListener(TickEventHandler::onServerTick);
        gameEventBus.addListener(ChunkUnloadHandler::onLevelTick);

        Constants.LOG.info("Hibernateforge NeoForge inicializado com sucesso");
    }

    /**
     * Chamado quando o servidor termina de inicializar
     */
    private void onServerStarted(ServerStartedEvent event) {
        Constants.LOG.info("Servidor iniciado - Hibernateforge ativo");

        // Se habilitado, inicia hibernação se não há jogadores
        if (CommonConfig.startEnabled && event.getServer().getPlayerCount() == 0) {
            setHibernationState(event.getServer(), true);
        }
    }

    /**
     * Chamado quando o servidor está parando
     */
    private void onServerStopping(ServerStoppingEvent event) {
        Constants.LOG.info("Servidor parando - desabilitando hibernação");

        // Para otimização de memória antes do shutdown
        if (CommonConfig.enableMemoryOptimization) {
            MemoryManager.stopMemoryOptimization();
            MemoryManager.shutdown();
        }

        hibernating = false;
        Hibernation.setHibernating(false);
    }

    /**
     * Exposto para lógica de comandos e outros sistemas
     */
    public static boolean isHibernating() {
        return hibernating;
    }

    /**
     * Define o estado de hibernação com todas as otimizações
     */
    public static void setHibernationState(MinecraftServer server, boolean state) {
        boolean wasHibernating = hibernating;
        hibernating = state;

        // Atualiza o estado global
        Hibernation.setHibernating(state);

        // Atualiza regras do jogo
        GameRuleHandler.setHibernationGameRules(server, state);

        // Gerencia otimização de memória
        if (CommonConfig.enableMemoryOptimization) {
            if (state && !wasHibernating) {
                // Entrando em hibernação
                Constants.LOG.info("Entrando em modo hibernação - iniciando otimizações");

                if (CommonConfig.saveBeforeHibernation) {
                    MemoryManager.saveImportantData(server);
                }

                MemoryManager.startMemoryOptimization(server);

            } else if (!state && wasHibernating) {
                // Saindo da hibernação
                Constants.LOG.info("Saindo do modo hibernação - parando otimizações");
                MemoryManager.stopMemoryOptimization();
            }
        }

        // Log da mudança de estado
        Constants.LOG.info("Estado de hibernação alterado: {} -> {}",
                wasHibernating ? "HIBERNANDO" : "ATIVO",
                state ? "HIBERNANDO" : "ATIVO");
    }

    /**
     * Força garbage collection (para uso por comandos)
     */
    public static void forceGarbageCollection() {
        if (CommonConfig.enableMemoryOptimization && CommonConfig.forceGarbageCollection) {
            long beforeGC = getUsedMemoryMB();
            System.gc();

            // Pequena pausa para permitir que o GC complete
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            long afterGC = getUsedMemoryMB();
            long memoryFreed = beforeGC - afterGC;

            Constants.LOG.info("GC manual executado: {}MB liberados (Antes: {}MB, Depois: {}MB)",
                    memoryFreed, beforeGC, afterGC);
        }
    }

    /**
     * Obtém informações de uso de memória
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
                "Memória: %dMB usada / %dMB máxima (%.1f%%) - Disponível: %dMB [Heap atual: %dMB]",
                usedMemory, maxMemory, usagePercent, availableMemory, totalHeapMB
        );
    }

    /**
     * Obtém memória usada em MB
     */
    private static long getUsedMemoryMB() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
    }

    /**
     * Verifica se o sistema de otimização está ativo
     */
    public static boolean isMemoryOptimizationEnabled() {
        return CommonConfig.enableMemoryOptimization;
    }

    /**
     * Recarrega configurações do arquivo
     */
    public static void reloadConfig() {
        Constants.LOG.info("Recarregando configurações...");
        ConfigManager.loadConfig();
        Constants.LOG.info("Configurações recarregadas com sucesso");
    }

    /**
     * Salva configurações atuais no arquivo
     */
    public static void saveConfig() {
        ConfigManager.saveConfig();
    }
}