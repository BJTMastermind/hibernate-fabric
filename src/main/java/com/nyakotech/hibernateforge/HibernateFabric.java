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

public class HibernateFabric implements ModInitializer {
    private static boolean hibernating;

    @Override
    public void onInitialize() {
        loadConfig();

        // Set initial hibernation flag from config
        hibernating = Config.startEnabled;

        // Register everything
        HibernationCommand.register();
        MemoryCommand.register();
        GameRuleHandler.register();
        TickEventHandler.register();
        ChunkUnloadHandler.register();

        // Registers shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(MemoryManager::shutdown));
    }

    /** Exposed to your command logic */
    public static boolean isHibernating() { return hibernating; }

    public static void setHibernationState(MinecraftServer server, boolean state) {
        boolean wasHibernating = hibernating;
        hibernating = state;

        // Updates game rules
        GameRuleHandler.setHibernationGameRules(server, state);

        // Manages memory optimization
        if (state && !wasHibernating) {
            // Entering hibernation
            if (Config.enableMemoryOptimization) {
                if (Config.saveBeforeHibernation) {
                    MemoryManager.saveImportantData(server);
                }
                MemoryManager.startMemoryOptimization(server);
            }
        } else if (!state && wasHibernating) {
            // Exiting hibernation
            if (Config.enableMemoryOptimization) {
                MemoryManager.stopMemoryOptimization();
            }
        }
    }

    private void loadConfig() {
        try {
            Path cfgDir  = FabricLoader.getInstance().getConfigDir();
            Path cfgFile = cfgDir.resolve("hibernate-fabric.json");
            Gson gson    = new GsonBuilder().setPrettyPrinting().create();

            // If no config on disk, write defaults
            if (Files.notExists(cfgFile)) {
                JsonObject defaults = new JsonObject();
                defaults.addProperty("startEnabled", Config.startEnabled);
                defaults.addProperty("ticksToSkip", Config.ticksToSkip);
                defaults.addProperty("permissionLevel", Config.permissionLevel);
                defaults.addProperty("sleepTimeMs", Config.sleepTimeMs);

                // NEW SETTINGS FOR CPU OPTIMIZATION:
                defaults.addProperty("aggressiveCpuSaving", Config.aggressiveCpuSaving);
                defaults.addProperty("minSleepInterval", Config.minSleepInterval);
                defaults.addProperty("highLoadSleepMultiplier", Config.highLoadSleepMultiplier);
                defaults.addProperty("yieldInterval", Config.yieldInterval);

                Files.createDirectories(cfgDir);
                try (var writer = Files.newBufferedWriter(cfgFile, StandardOpenOption.CREATE_NEW)) {
                    gson.toJson(defaults, writer);
                }
            }

            // Read whatever's in the file, override CommonConfig
            try (var reader = Files.newBufferedReader(cfgFile)) {
                JsonObject obj = gson.fromJson(reader, JsonObject.class);
                Config.startEnabled    = obj.has("startEnabled")    ? obj.get("startEnabled").getAsBoolean() : Config.startEnabled;
                Config.ticksToSkip     = obj.has("ticksToSkip")     ? obj.get("ticksToSkip").getAsLong()      : Config.ticksToSkip;
                Config.permissionLevel = obj.has("permissionLevel") ? obj.get("permissionLevel").getAsInt()   : Config.permissionLevel;
                Config.sleepTimeMs     = obj.has("sleepTimeMs")     ? obj.get("sleepTimeMs").getAsInt()       : Config.sleepTimeMs;

                // NEW SETTINGS:
                Config.aggressiveCpuSaving = obj.has("aggressiveCpuSaving") ? obj.get("aggressiveCpuSaving").getAsBoolean() : Config.aggressiveCpuSaving;
                Config.minSleepInterval = obj.has("minSleepInterval") ? obj.get("minSleepInterval").getAsLong() : Config.minSleepInterval;
                Config.highLoadSleepMultiplier = obj.has("highLoadSleepMultiplier") ? obj.get("highLoadSleepMultiplier").getAsDouble() : Config.highLoadSleepMultiplier;
                Config.yieldInterval = obj.has("yieldInterval") ? obj.get("yieldInterval").getAsInt() : Config.yieldInterval;
            }

        } catch (IOException e) {
            System.err.println("Failed to load hibernateforge config, using defaults:");
            e.printStackTrace();
        }
    }

    private JsonObject createDefaultConfig() {
        JsonObject defaults = new JsonObject();

        // Basic settings
        defaults.addProperty("startEnabled", Config.startEnabled);
        defaults.addProperty("ticksToSkip", Config.ticksToSkip);
        defaults.addProperty("permissionLevel", Config.permissionLevel);

        // Memory settings
        defaults.addProperty("enableMemoryOptimization", Config.enableMemoryOptimization);
        defaults.addProperty("memoryCleanupIntervalSeconds", Config.memoryCleanupIntervalSeconds);
        defaults.addProperty("memoryThresholdPercent", Config.memoryThresholdPercent);
        defaults.addProperty("maxChunksToUnloadPerTick", Config.maxChunksToUnloadPerTick);
        defaults.addProperty("forceGarbageCollection", Config.forceGarbageCollection);
        defaults.addProperty("gcIntervalSeconds", Config.gcIntervalSeconds);
        defaults.addProperty("saveBeforeHibernation", Config.saveBeforeHibernation);
        defaults.addProperty("removeOldDroppedItems", Config.removeOldDroppedItems);
        defaults.addProperty("droppedItemMaxAgeSeconds", Config.droppedItemMaxAgeSeconds);
        defaults.addProperty("removeProjectiles", Config.removeProjectiles);
        defaults.addProperty("removeExperienceOrbs", Config.removeExperienceOrbs);
        defaults.addProperty("compactDataStructures", Config.compactDataStructures);
        defaults.addProperty("logMemoryUsage", Config.logMemoryUsage);

        return defaults;
    }

    private void loadConfigValues(JsonObject obj) {
        // Basic settings
        Config.startEnabled = getBoolean(obj, "startEnabled", Config.startEnabled);
        Config.ticksToSkip = getLong(obj, "ticksToSkip", Config.ticksToSkip);
        Config.permissionLevel = getInt(obj, "permissionLevel", Config.permissionLevel);

        // Memory settings
        Config.enableMemoryOptimization = getBoolean(obj, "enableMemoryOptimization", Config.enableMemoryOptimization);
        Config.memoryCleanupIntervalSeconds = getInt(obj, "memoryCleanupIntervalSeconds", Config.memoryCleanupIntervalSeconds);
        Config.memoryThresholdPercent = getDouble(obj, "memoryThresholdPercent", Config.memoryThresholdPercent);
        Config.maxChunksToUnloadPerTick = getInt(obj, "maxChunksToUnloadPerTick", Config.maxChunksToUnloadPerTick);
        Config.forceGarbageCollection = getBoolean(obj, "forceGarbageCollection", Config.forceGarbageCollection);
        Config.gcIntervalSeconds = getInt(obj, "gcIntervalSeconds", Config.gcIntervalSeconds);
        Config.saveBeforeHibernation = getBoolean(obj, "saveBeforeHibernation", Config.saveBeforeHibernation);
        Config.removeOldDroppedItems = getBoolean(obj, "removeOldDroppedItems", Config.removeOldDroppedItems);
        Config.droppedItemMaxAgeSeconds = getInt(obj, "droppedItemMaxAgeSeconds", Config.droppedItemMaxAgeSeconds);
        Config.removeProjectiles = getBoolean(obj, "removeProjectiles", Config.removeProjectiles);
        Config.removeExperienceOrbs = getBoolean(obj, "removeExperienceOrbs", Config.removeExperienceOrbs);
        Config.compactDataStructures = getBoolean(obj, "compactDataStructures", Config.compactDataStructures);
        Config.logMemoryUsage = getBoolean(obj, "logMemoryUsage", Config.logMemoryUsage);
    }

    // Helper methods for configuration reading
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