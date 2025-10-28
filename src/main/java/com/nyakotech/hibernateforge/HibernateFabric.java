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

                // NEW SETTINGS FOR MEMORY OPTIMIZATION:
                defaults.addProperty("enableMemoryOptimization", Config.enableMemoryOptimization);
                defaults.addProperty("memoryCleanupIntervalSeconds", Config.memoryCleanupIntervalSeconds);
                defaults.addProperty("memoryThresholdPercent", Config.memoryThresholdPercent);
                defaults.addProperty("forceGarbageCollection", Config.forceGarbageCollection);
                defaults.addProperty("gcIntervalSeconds", Config.gcIntervalSeconds);
                defaults.addProperty("saveBeforeHibernation", Config.saveBeforeHibernation);
                defaults.addProperty("removeOldDroppedItems", Config.removeOldDroppedItems);
                defaults.addProperty("droppedItemMaxAgeSeconds", Config.droppedItemMaxAgeSeconds);
                defaults.addProperty("removeProjectiles", Config.removeProjectiles);
                defaults.addProperty("removeExperienceOrbs", Config.removeExperienceOrbs);
                defaults.addProperty("logMemoryUsage", Config.logMemoryUsage);

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

            // Read whatever's in the file, override Config class
            try (var reader = Files.newBufferedReader(cfgFile)) {
                JsonObject obj = gson.fromJson(reader, JsonObject.class);
                Config.startEnabled    = obj.has("startEnabled")    ? obj.get("startEnabled").getAsBoolean() : Config.startEnabled;
                Config.ticksToSkip     = obj.has("ticksToSkip")     ? obj.get("ticksToSkip").getAsLong()      : Config.ticksToSkip;
                Config.permissionLevel = obj.has("permissionLevel") ? obj.get("permissionLevel").getAsInt()   : Config.permissionLevel;
                Config.sleepTimeMs     = obj.has("sleepTimeMs")     ? obj.get("sleepTimeMs").getAsInt()       : Config.sleepTimeMs;

                // NEW MEMORY OPTIMIZATION SETTINGS:
                Config.enableMemoryOptimization = obj.has("enableMemoryOptimization") ? obj.get("enableMemoryOptimization").getAsBoolean() : Config.enableMemoryOptimization;
                Config.memoryCleanupIntervalSeconds = obj.has("memoryCleanupIntervalSeconds") ? obj.get("memoryCleanupIntervalSeconds").getAsInt() : Config.memoryCleanupIntervalSeconds;
                Config.memoryThresholdPercent = obj.has("memoryThresholdPercent") ? obj.get("memoryThresholdPercent").getAsDouble() : Config.memoryThresholdPercent;
                Config.forceGarbageCollection = obj.has("forceGarbageCollection") ? obj.get("forceGarbageCollection").getAsBoolean() : Config.forceGarbageCollection;
                Config.gcIntervalSeconds = obj.has("gcIntervalSeconds") ? obj.get("gcIntervalSeconds").getAsInt() : Config.gcIntervalSeconds;
                Config.saveBeforeHibernation = obj.has("saveBeforeHibernation") ? obj.get("saveBeforeHibernation").getAsBoolean() : Config.saveBeforeHibernation;
                Config.removeOldDroppedItems = obj.has("removeOldDroppedItems") ? obj.get("removeOldDroppedItems").getAsBoolean() : Config.removeOldDroppedItems;
                Config.droppedItemMaxAgeSeconds = obj.has("droppedItemMaxAgeSeconds") ? obj.get("droppedItemMaxAgeSeconds").getAsInt() : Config.droppedItemMaxAgeSeconds;
                Config.removeProjectiles = obj.has("removeProjectiles") ? obj.get("removeProjectiles").getAsBoolean() : Config.removeProjectiles;
                Config.removeExperienceOrbs = obj.has("removeExperienceOrbs") ? obj.get("removeExperienceOrbs").getAsBoolean() : Config.removeExperienceOrbs;
                Config.logMemoryUsage = obj.has("logMemoryUsage") ? obj.get("logMemoryUsage").getAsBoolean() : Config.logMemoryUsage;

                // NEW SETTINGS:
                Config.aggressiveCpuSaving = obj.has("aggressiveCpuSaving") ? obj.get("aggressiveCpuSaving").getAsBoolean() : Config.aggressiveCpuSaving;
                Config.minSleepInterval = obj.has("minSleepInterval") ? obj.get("minSleepInterval").getAsLong() : Config.minSleepInterval;
                Config.highLoadSleepMultiplier = obj.has("highLoadSleepMultiplier") ? obj.get("highLoadSleepMultiplier").getAsDouble() : Config.highLoadSleepMultiplier;
                Config.yieldInterval = obj.has("yieldInterval") ? obj.get("yieldInterval").getAsInt() : Config.yieldInterval;
            }

        } catch (IOException e) {
            System.err.println("Failed to load hibernate-fabric config, using defaults:");
            e.printStackTrace();
        }
    }
}