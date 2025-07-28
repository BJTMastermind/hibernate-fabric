package com.nyakotech.hibernateforge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Gerenciador de configuração para NeoForge
 */
public class ConfigManager {
    private static final String CONFIG_FILENAME = "hibernateforge.json";

    public static void loadConfig() {
        try {
            Path configDir = FMLPaths.CONFIGDIR.get();
            Path configFile = configDir.resolve(CONFIG_FILENAME);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // Se não existe arquivo de config, cria com valores padrão
            if (Files.notExists(configFile)) {
                JsonObject defaults = createDefaultConfig();
                Files.createDirectories(configDir);
                try (var writer = Files.newBufferedWriter(configFile, StandardOpenOption.CREATE_NEW)) {
                    gson.toJson(defaults, writer);
                }
                Constants.LOG.info("Arquivo de configuração criado: {}", configFile);
            }

            // Lê o arquivo e aplica as configurações
            try (var reader = Files.newBufferedReader(configFile)) {
                JsonObject config = gson.fromJson(reader, JsonObject.class);
                applyConfigValues(config);
                Constants.LOG.info("Configuração carregada com sucesso");
            }

        } catch (IOException e) {
            Constants.LOG.error("Erro ao carregar configuração, usando valores padrão: ", e);
        }
    }

    private static JsonObject createDefaultConfig() {
        JsonObject config = new JsonObject();

        // Adiciona comentário sobre a configuração
        config.addProperty("_comment", "Configuração do Hibernateforge - Edite conforme necessário");

        // Configurações básicas
        config.addProperty("startEnabled", CommonConfig.startEnabled);
        config.addProperty("ticksToSkip", CommonConfig.ticksToSkip);
        config.addProperty("permissionLevel", CommonConfig.permissionLevel);

        // Configurações de memória
        config.addProperty("enableMemoryOptimization", CommonConfig.enableMemoryOptimization);
        config.addProperty("memoryCleanupIntervalSeconds", CommonConfig.memoryCleanupIntervalSeconds);
        config.addProperty("memoryThresholdPercent", CommonConfig.memoryThresholdPercent);
        config.addProperty("maxChunksToUnloadPerTick", CommonConfig.maxChunksToUnloadPerTick);
        config.addProperty("forceGarbageCollection", CommonConfig.forceGarbageCollection);
        config.addProperty("gcIntervalSeconds", CommonConfig.gcIntervalSeconds);
        config.addProperty("saveBeforeHibernation", CommonConfig.saveBeforeHibernation);
        config.addProperty("removeOldDroppedItems", CommonConfig.removeOldDroppedItems);
        config.addProperty("droppedItemMaxAgeSeconds", CommonConfig.droppedItemMaxAgeSeconds);
        config.addProperty("removeProjectiles", CommonConfig.removeProjectiles);
        config.addProperty("removeExperienceOrbs", CommonConfig.removeExperienceOrbs);
        config.addProperty("compactDataStructures", CommonConfig.compactDataStructures);
        config.addProperty("logMemoryUsage", CommonConfig.logMemoryUsage);

        return config;
    }

    private static void applyConfigValues(JsonObject config) {
        // Configurações básicas
        CommonConfig.startEnabled = getBoolean(config, "startEnabled", CommonConfig.startEnabled);
        CommonConfig.ticksToSkip = getLong(config, "ticksToSkip", CommonConfig.ticksToSkip);
        CommonConfig.permissionLevel = getInt(config, "permissionLevel", CommonConfig.permissionLevel);

        // Configurações de memória
        CommonConfig.enableMemoryOptimization = getBoolean(config, "enableMemoryOptimization", CommonConfig.enableMemoryOptimization);
        CommonConfig.memoryCleanupIntervalSeconds = getInt(config, "memoryCleanupIntervalSeconds", CommonConfig.memoryCleanupIntervalSeconds);
        CommonConfig.memoryThresholdPercent = getDouble(config, "memoryThresholdPercent", CommonConfig.memoryThresholdPercent);
        CommonConfig.maxChunksToUnloadPerTick = getInt(config, "maxChunksToUnloadPerTick", CommonConfig.maxChunksToUnloadPerTick);
        CommonConfig.forceGarbageCollection = getBoolean(config, "forceGarbageCollection", CommonConfig.forceGarbageCollection);
        CommonConfig.gcIntervalSeconds = getInt(config, "gcIntervalSeconds", CommonConfig.gcIntervalSeconds);
        CommonConfig.saveBeforeHibernation = getBoolean(config, "saveBeforeHibernation", CommonConfig.saveBeforeHibernation);
        CommonConfig.removeOldDroppedItems = getBoolean(config, "removeOldDroppedItems", CommonConfig.removeOldDroppedItems);
        CommonConfig.droppedItemMaxAgeSeconds = getInt(config, "droppedItemMaxAgeSeconds", CommonConfig.droppedItemMaxAgeSeconds);
        CommonConfig.removeProjectiles = getBoolean(config, "removeProjectiles", CommonConfig.removeProjectiles);
        CommonConfig.removeExperienceOrbs = getBoolean(config, "removeExperienceOrbs", CommonConfig.removeExperienceOrbs);
        CommonConfig.compactDataStructures = getBoolean(config, "compactDataStructures", CommonConfig.compactDataStructures);
        CommonConfig.logMemoryUsage = getBoolean(config, "logMemoryUsage", CommonConfig.logMemoryUsage);
    }

    // Métodos auxiliares para leitura segura da configuração
    private static boolean getBoolean(JsonObject config, String key, boolean defaultValue) {
        try {
            return config.has(key) ? config.get(key).getAsBoolean() : defaultValue;
        } catch (Exception e) {
            Constants.LOG.warn("Erro ao ler configuração '{}', usando valor padrão: {}", key, defaultValue);
            return defaultValue;
        }
    }

    private static int getInt(JsonObject config, String key, int defaultValue) {
        try {
            return config.has(key) ? config.get(key).getAsInt() : defaultValue;
        } catch (Exception e) {
            Constants.LOG.warn("Erro ao ler configuração '{}', usando valor padrão: {}", key, defaultValue);
            return defaultValue;
        }
    }

    private static long getLong(JsonObject config, String key, long defaultValue) {
        try {
            return config.has(key) ? config.get(key).getAsLong() : defaultValue;
        } catch (Exception e) {
            Constants.LOG.warn("Erro ao ler configuração '{}', usando valor padrão: {}", key, defaultValue);
            return defaultValue;
        }
    }

    private static double getDouble(JsonObject config, String key, double defaultValue) {
        try {
            return config.has(key) ? config.get(key).getAsDouble() : defaultValue;
        } catch (Exception e) {
            Constants.LOG.warn("Erro ao ler configuração '{}', usando valor padrão: {}", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Salva as configurações atuais no arquivo
     */
    public static void saveConfig() {
        try {
            Path configDir = FMLPaths.CONFIGDIR.get();
            Path configFile = configDir.resolve(CONFIG_FILENAME);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            JsonObject config = createDefaultConfig(); // Usa os valores atuais

            try (var writer = Files.newBufferedWriter(configFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                gson.toJson(config, writer);
            }

            Constants.LOG.info("Configuração salva em: {}", configFile);

        } catch (IOException e) {
            Constants.LOG.error("Erro ao salvar configuração: ", e);
        }
    }
}