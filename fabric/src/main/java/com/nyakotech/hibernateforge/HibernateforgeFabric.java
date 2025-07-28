package com.nyakotech.hibernateforge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class HibernateforgeFabric implements ModInitializer {
    private static boolean hibernating;

    @Override
    public void onInitialize() {
        loadConfig();

        // Set initial hibernation flag from config
        hibernating = CommonConfig.startEnabled;

        // Register everything
        HibernationCommand.register();
        MemoryCommand.register();
        GameRuleHandler.register();
        TickEventHandler.register();
        ChunkUnloadHandler.register();

        // Registra shutdown hook para limpeza
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MemoryManager.shutdown();
        }));
    }

    /** Exposed to your command logic */
    public static boolean isHibernating() { return hibernating; }

    public static void setHibernationState(MinecraftServer server, boolean state) {
        boolean wasHibernating = hibernating;
        hibernating = state;

        // Atualiza regras do jogo
        GameRuleHandler.setHibernationGameRules(server, state);

        // Gerencia otimização de memória
        if (state && !wasHibernating) {
            // Entrando em hibernação
            if (CommonConfig.enableMemoryOptimization) {
                if (CommonConfig.saveBeforeHibernation) {
                    MemoryManager.saveImportantData(server);
                }
                MemoryManager.startMemoryOptimization(server);
            }
        } else if (!state && wasHibernating) {
            // Saindo da hibernação
            if (CommonConfig.enableMemoryOptimization) {
                MemoryManager.stopMemoryOptimization();
            }
        }
    }

    private void loadConfig() {
        try {
            Path cfgDir  = FabricLoader.getInstance().getConfigDir();
            Path cfgFile = cfgDir.resolve("hibernateforge.json");
            Gson gson    = new GsonBuilder().setPrettyPrinting().create();

            // If no config on disk, write defaults
            if (Files.notExists(cfgFile)) {
                JsonObject defaults = createDefaultConfig();
                Files.createDirectories(cfgDir);
                try (var writer = Files.newBufferedWriter(cfgFile, StandardOpenOption.CREATE_NEW)) {
                    gson.toJson(defaults, writer);
                }
            }

            // Read whatever's in the file, override CommonConfig
            try (var reader = Files.newBufferedReader(cfgFile)) {
                JsonObject obj = gson.fromJson(reader, JsonObject.class);
                loadConfigValues(obj);
            }

        } catch (IOException e) {
            // If something goes wrong, stick with defaults and log to console
            System.err.println("Failed to load hibernateforge config, using defaults:");
            e.printStackTrace();
        }
    }

    private JsonObject createDefaultConfig() {
        JsonObject defaults = new JsonObject();

        // Configurações básicas
        defaults.addProperty("startEnabled", CommonConfig.startEnabled);
        defaults.addProperty("ticksToSkip", CommonConfig.ticksToSkip);
        defaults.addProperty("permissionLevel", CommonConfig.permissionLevel);

        // Configurações de memória
        defaults.addProperty("enableMemoryOptimization", CommonConfig.enableMemoryOptimization);
        defaults.addProperty("memoryCleanupIntervalSeconds", CommonConfig.memoryCleanupIntervalSeconds);
        defaults.addProperty("memoryThresholdPercent", CommonConfig.memoryThresholdPercent);
        defaults.addProperty("maxChunksToUnloadPerTick", CommonConfig.maxChunksToUnloadPerTick);
        defaults.addProperty("forceGarbageCollection", CommonConfig.forceGarbageCollection);
        defaults.addProperty("gcIntervalSeconds", CommonConfig.gcIntervalSeconds);
        defaults.addProperty("saveBeforeHibernation", CommonConfig.saveBeforeHibernation);
        defaults.addProperty("removeOldDroppedItems", CommonConfig.removeOldDroppedItems);
        defaults.addProperty("droppedItemMaxAgeSeconds", CommonConfig.droppedItemMaxAgeSeconds);
        defaults.addProperty("removeProjectiles", CommonConfig.removeProjectiles);
        defaults.addProperty("removeExperienceOrbs", CommonConfig.removeExperienceOrbs);
        defaults.addProperty("compactDataStructures", CommonConfig.compactDataStructures);
        defaults.addProperty("logMemoryUsage", CommonConfig.logMemoryUsage);

        return defaults;
    }

    private void loadConfigValues(JsonObject obj) {
        // Configurações básicas
        CommonConfig.startEnabled = getBoolean(obj, "startEnabled", CommonConfig.startEnabled);
        CommonConfig.ticksToSkip = getLong(obj, "ticksToSkip", CommonConfig.ticksToSkip);
        CommonConfig.permissionLevel = getInt(obj, "permissionLevel", CommonConfig.permissionLevel);

        // Configurações de memória
        CommonConfig.enableMemoryOptimization = getBoolean(obj, "enableMemoryOptimization", CommonConfig.enableMemoryOptimization);
        CommonConfig.memoryCleanupIntervalSeconds = getInt(obj, "memoryCleanupIntervalSeconds", CommonConfig.memoryCleanupIntervalSeconds);
        CommonConfig.memoryThresholdPercent = getDouble(obj, "memoryThresholdPercent", CommonConfig.memoryThresholdPercent);
        CommonConfig.maxChunksToUnloadPerTick = getInt(obj, "maxChunksToUnloadPerTick", CommonConfig.maxChunksToUnloadPerTick);
        CommonConfig.forceGarbageCollection = getBoolean(obj, "forceGarbageCollection", CommonConfig.forceGarbageCollection);
        CommonConfig.gcIntervalSeconds = getInt(obj, "gcIntervalSeconds", CommonConfig.gcIntervalSeconds);
        CommonConfig.saveBeforeHibernation = getBoolean(obj, "saveBeforeHibernation", CommonConfig.saveBeforeHibernation);
        CommonConfig.removeOldDroppedItems = getBoolean(obj, "removeOldDroppedItems", CommonConfig.removeOldDroppedItems);
        CommonConfig.droppedItemMaxAgeSeconds = getInt(obj, "droppedItemMaxAgeSeconds", CommonConfig.droppedItemMaxAgeSeconds);
        CommonConfig.removeProjectiles = getBoolean(obj, "removeProjectiles", CommonConfig.removeProjectiles);
        CommonConfig.removeExperienceOrbs = getBoolean(obj, "removeExperienceOrbs", CommonConfig.removeExperienceOrbs);
        CommonConfig.compactDataStructures = getBoolean(obj, "compactDataStructures", CommonConfig.compactDataStructures);
        CommonConfig.logMemoryUsage = getBoolean(obj, "logMemoryUsage", CommonConfig.logMemoryUsage);
    }

    // Métodos auxiliares para leitura de configuração
    private boolean getBoolean(JsonObject obj, String key, boolean defaultValue) {
        return obj.has(key) ? obj.get(key).getAsBoolean() : defaultValue;
    }

    private int getInt(JsonObject obj, String key, int defaultValue) {
        return obj.has(key) ? obj.get(key).getAsInt() : defaultValue;
    }

    private long getLong(JsonObject obj, String key, long defaultValue) {
        return obj.has(key) ? obj.get(key).getAsLong() : defaultValue;
    }

    private double getDouble(JsonObject obj, String key, double defaultValue) {
        return obj.has(key) ? obj.get(key).getAsDouble() : defaultValue;
    }
}