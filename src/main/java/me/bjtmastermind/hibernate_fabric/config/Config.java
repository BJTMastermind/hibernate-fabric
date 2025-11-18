package me.bjtmastermind.hibernate_fabric.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class Config {
    public static boolean startEnabled = true;
    public static long ticksToSkip = 400L;
    public static int permissionLevel = 2;
    public static int sleepTimeMs = 75;

    public static boolean enableMemoryOptimization = true;
    public static int memoryCleanupIntervalSeconds = 30;
    public static double memoryThresholdPercent = 80.0;
    public static boolean forceGarbageCollection = true;
    public static int gcIntervalSeconds = 30;
    public static boolean saveBeforeHibernation = true;
    public static List<ResourceLocation> removeEntities = List.of(
        ResourceLocation.parse("minecraft:item"),
        ResourceLocation.parse("minecraft:firework_rocket"),
        ResourceLocation.parse("minecraft:arrow"),
        ResourceLocation.parse("minecraft:experience_orb")
    );
    public static int droppedItemMaxAgeSeconds = 300;
    public static boolean logMemoryUsage = true;

    public static boolean aggressiveCpuSaving = true;
    public static long minSleepInterval = 1500;
    public static double highLoadSleepMultiplier = 1.5;
    public static int yieldInterval = 8;

    public static boolean doDaylightCycle = true;
    public static boolean doWeatherCycle = true;
    public static int randomTickSpeed = 3;
    public static boolean doMobSpawning = true;
    public static boolean doFireTick = true;

    public static void load() {
        try {
            Path cfgDir = FabricLoader.getInstance().getConfigDir();
            Path cfgFile = cfgDir.resolve("hibernate-fabric.json");
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(ResourceLocation.class, new JsonSerializer<ResourceLocation>() {
                    @Override
                    public JsonElement serialize(ResourceLocation src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.toString());
                    }
                })
                .registerTypeAdapter(ResourceLocation.class, new JsonDeserializer<ResourceLocation>() {
                    @Override
                    public ResourceLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return ResourceLocation.tryParse(json.getAsString());
                    }
                })
                .setPrettyPrinting()
                .create();

            // If no config on disk, write defaults
            if (Files.notExists(cfgFile)) {
                JsonObject defaults = new JsonObject();
                defaults.addProperty("startEnabled", startEnabled);
                defaults.addProperty("ticksToSkip", ticksToSkip);
                defaults.addProperty("permissionLevel", permissionLevel);
                defaults.addProperty("sleepTimeMs", sleepTimeMs);

                // NEW SETTINGS FOR MEMORY OPTIMIZATION:
                defaults.addProperty("enableMemoryOptimization", enableMemoryOptimization);
                defaults.addProperty("memoryCleanupIntervalSeconds", memoryCleanupIntervalSeconds);
                defaults.addProperty("memoryThresholdPercent", memoryThresholdPercent);
                defaults.addProperty("forceGarbageCollection", forceGarbageCollection);
                defaults.addProperty("gcIntervalSeconds", gcIntervalSeconds);
                defaults.addProperty("saveBeforeHibernation", saveBeforeHibernation);
                JsonArray removeEntitiesArray = new JsonArray();
                for (ResourceLocation id : removeEntities) {
                    removeEntitiesArray.add(id.toString());
                }
                defaults.add("removeEntities", removeEntitiesArray);
                defaults.addProperty("droppedItemMaxAgeSeconds", droppedItemMaxAgeSeconds);
                defaults.addProperty("logMemoryUsage", logMemoryUsage);

                // NEW SETTINGS FOR CPU OPTIMIZATION:
                defaults.addProperty("aggressiveCpuSaving", aggressiveCpuSaving);
                defaults.addProperty("minSleepInterval", minSleepInterval);
                defaults.addProperty("highLoadSleepMultiplier", highLoadSleepMultiplier);
                defaults.addProperty("yieldInterval", yieldInterval);

                // NEW SETTINGS FOR RESTORING GAMERULE SETTINGS:
                JsonObject restoreGameRulesAs = new JsonObject();
                restoreGameRulesAs.addProperty("doDaylightCycle", doDaylightCycle);
                restoreGameRulesAs.addProperty("doWeatherCycle", doWeatherCycle);
                restoreGameRulesAs.addProperty("randomTickSpeed", randomTickSpeed);
                restoreGameRulesAs.addProperty("doMobSpawning", doMobSpawning);
                restoreGameRulesAs.addProperty("doFireTick", doFireTick);
                defaults.add("restoreGameRulesAs", restoreGameRulesAs);

                Files.createDirectories(cfgDir);
                try (BufferedWriter writer = Files.newBufferedWriter(cfgFile, StandardOpenOption.CREATE_NEW)) {
                    gson.toJson(defaults, writer);
                }
            }

            // Read whatever's in the file, override Config class
            try (BufferedReader reader = Files.newBufferedReader(cfgFile)) {
                JsonObject obj = gson.fromJson(reader, JsonObject.class);
                startEnabled = obj.has("startEnabled") ? obj.get("startEnabled").getAsBoolean() : startEnabled;
                ticksToSkip = obj.has("ticksToSkip") ? obj.get("ticksToSkip").getAsLong() : ticksToSkip;
                permissionLevel = obj.has("permissionLevel") ? obj.get("permissionLevel").getAsInt() : permissionLevel;
                sleepTimeMs = obj.has("sleepTimeMs") ? obj.get("sleepTimeMs").getAsInt() : sleepTimeMs;

                // NEW MEMORY OPTIMIZATION SETTINGS:
                enableMemoryOptimization = obj.has("enableMemoryOptimization") ? obj.get("enableMemoryOptimization").getAsBoolean() : enableMemoryOptimization;
                memoryCleanupIntervalSeconds = obj.has("memoryCleanupIntervalSeconds") ? obj.get("memoryCleanupIntervalSeconds").getAsInt() : memoryCleanupIntervalSeconds;
                memoryThresholdPercent = obj.has("memoryThresholdPercent") ? obj.get("memoryThresholdPercent").getAsDouble() : memoryThresholdPercent;
                forceGarbageCollection = obj.has("forceGarbageCollection") ? obj.get("forceGarbageCollection").getAsBoolean() : forceGarbageCollection;
                gcIntervalSeconds = obj.has("gcIntervalSeconds") ? obj.get("gcIntervalSeconds").getAsInt() : gcIntervalSeconds;
                saveBeforeHibernation = obj.has("saveBeforeHibernation") ? obj.get("saveBeforeHibernation").getAsBoolean() : saveBeforeHibernation;
                removeEntities = obj.has("removeEntities") ? parseRemoveEntitiesList(obj) : removeEntities;
                droppedItemMaxAgeSeconds = obj.has("droppedItemMaxAgeSeconds") ? obj.get("droppedItemMaxAgeSeconds").getAsInt() : droppedItemMaxAgeSeconds;
                logMemoryUsage = obj.has("logMemoryUsage") ? obj.get("logMemoryUsage").getAsBoolean() : logMemoryUsage;

                // NEW SETTINGS:
                aggressiveCpuSaving = obj.has("aggressiveCpuSaving") ? obj.get("aggressiveCpuSaving").getAsBoolean() : aggressiveCpuSaving;
                minSleepInterval = obj.has("minSleepInterval") ? obj.get("minSleepInterval").getAsLong() : minSleepInterval;
                highLoadSleepMultiplier = obj.has("highLoadSleepMultiplier") ? obj.get("highLoadSleepMultiplier").getAsDouble() : highLoadSleepMultiplier;
                yieldInterval = obj.has("yieldInterval") ? obj.get("yieldInterval").getAsInt() : yieldInterval;

                // NEW GAMERULES SETTINGS:
                JsonObject restoreGameRulesAs = obj.has("restoreGameRulesAs") ? obj.getAsJsonObject("restoreGameRulesAs") : new JsonObject();
                doDaylightCycle = restoreGameRulesAs.has("doDaylightCycle") ? restoreGameRulesAs.get("doDaylightCycle").getAsBoolean() : doDaylightCycle;
                doWeatherCycle = restoreGameRulesAs.has("doWeatherCycle") ? restoreGameRulesAs.get("doWeatherCycle").getAsBoolean() : doWeatherCycle;
                randomTickSpeed = restoreGameRulesAs.has("randomTickSpeed") ? restoreGameRulesAs.get("randomTickSpeed").getAsInt() : randomTickSpeed;
                doMobSpawning = restoreGameRulesAs.has("doMobSpawning") ? restoreGameRulesAs.get("doMobSpawning").getAsBoolean() : doMobSpawning;
                doFireTick = restoreGameRulesAs.has("doFireTick") ? restoreGameRulesAs.get("doFireTick").getAsBoolean() : doFireTick;
            }

        } catch (IOException e) {
            System.err.println("Failed to load hibernate-fabric config, using defaults:");
            e.printStackTrace();
        }
    }

    // Parses the 'removeEntities' array from the config file
    private static List<ResourceLocation> parseRemoveEntitiesList(JsonObject obj) {
        JsonArray removeEntitiesArray = obj.getAsJsonArray("removeEntities");
        List<ResourceLocation> removeEntities = new ArrayList<>();

        for (JsonElement element : removeEntitiesArray) {
            ResourceLocation entityId = ResourceLocation.parse(element.getAsString());

            if (BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
                removeEntities.add(entityId);
            }
        }
        return removeEntities;
    }
}