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
import net.minecraft.resources.Identifier;

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
    public static List<Identifier> removeEntities = List.of(
        Identifier.parse("minecraft:item"),
        Identifier.parse("minecraft:firework_rocket"),
        Identifier.parse("minecraft:arrow"),
        Identifier.parse("minecraft:experience_orb")
    );
    public static int droppedItemMaxAgeSeconds = 300;
    public static boolean logMemoryUsage = true;

    public static boolean aggressiveCpuSaving = true;
    public static long minSleepInterval = 1500;
    public static double highLoadSleepMultiplier = 1.5;
    public static int yieldInterval = 8;

    public static boolean advanceTime = true;
    public static boolean advanceWeather = true;
    public static int randomTickSpeed = 3;
    public static boolean spawnMobs = true;
    public static int fireSpreadRadiusAroundPlayer = 128;

    public static void load() {
        try {
            Path cfgDir = FabricLoader.getInstance().getConfigDir();
            Path cfgFile = cfgDir.resolve("hibernate-fabric.json");
            Gson gson = setupGson();

            // If no config on disk, write defaults
            if (Files.notExists(cfgFile)) {
                save();
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
                advanceTime = restoreGameRulesAs.has("advance_time") ?
                    restoreGameRulesAs.get("advance_time").getAsBoolean() :
                    advanceTime;
                advanceWeather = restoreGameRulesAs.has("advance_weather") ?
                    restoreGameRulesAs.get("advance_weather").getAsBoolean() :
                    advanceWeather;
                randomTickSpeed = restoreGameRulesAs.has("random_tick_speed") ?
                    restoreGameRulesAs.get("random_tick_speed").getAsInt() :
                    randomTickSpeed;
                spawnMobs = restoreGameRulesAs.has("spawn_mobs") ?
                    restoreGameRulesAs.get("spawn_mobs").getAsBoolean() :
                    spawnMobs;
                fireSpreadRadiusAroundPlayer = restoreGameRulesAs.has("fire_spread_radius_around_player") ?
                    restoreGameRulesAs.get("fire_spread_radius_around_player").getAsInt() :
                    fireSpreadRadiusAroundPlayer;
            }

        } catch (IOException e) {
            System.err.println("Failed to load hibernate-fabric config, using defaults:");
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Path cfgDir = FabricLoader.getInstance().getConfigDir();
            Path cfgFile = cfgDir.resolve("hibernate-fabric.json");
            Gson gson = setupGson();

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
            for (Identifier id : removeEntities) {
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
            restoreGameRulesAs.addProperty("advance_time", advanceTime);
            restoreGameRulesAs.addProperty("advance_weather", advanceWeather);
            restoreGameRulesAs.addProperty("random_tick_speed", randomTickSpeed);
            restoreGameRulesAs.addProperty("spawn_mobs", spawnMobs);
            restoreGameRulesAs.addProperty("fire_spread_radius_around_player", fireSpreadRadiusAroundPlayer);
            defaults.add("restoreGameRulesAs", restoreGameRulesAs);

            Files.createDirectories(cfgDir);
            try (BufferedWriter writer = Files.newBufferedWriter(cfgFile, StandardOpenOption.CREATE)) {
                gson.toJson(defaults, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save hibernate-fabric config.");
            e.printStackTrace();
        }
    }

    public static JsonObject getJson() {
        try {
            Path cfgDir = FabricLoader.getInstance().getConfigDir();
            Path cfgFile = cfgDir.resolve("hibernate-fabric.json");
            Gson gson = setupGson();

            if (!Files.exists(cfgFile)) {
                return new JsonObject();
            }

            // Read whatever's in the file
            try (BufferedReader reader = Files.newBufferedReader(cfgFile)) {
                JsonObject obj = gson.fromJson(reader, JsonObject.class);
                return obj;
            }
        } catch (IOException e) {
            System.err.println("Failed to load hibernate-fabric config.");
            e.printStackTrace();
        }
        return null;
    }

    private static Gson setupGson() {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(Identifier.class, new JsonSerializer<Identifier>() {
                @Override
                public JsonElement serialize(Identifier src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.toString());
                }
            })
            .registerTypeAdapter(Identifier.class, new JsonDeserializer<Identifier>() {
                @Override
                public Identifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    return Identifier.tryParse(json.getAsString());
                }
            })
            .setPrettyPrinting()
            .create();
        return gson;
    }

    // Parses the 'removeEntities' array from the config file
    private static List<Identifier> parseRemoveEntitiesList(JsonObject obj) {
        JsonArray removeEntitiesArray = obj.getAsJsonArray("removeEntities");
        List<Identifier> removeEntities = new ArrayList<>();

        for (JsonElement element : removeEntitiesArray) {
            Identifier entityId = Identifier.parse(element.getAsString());

            if (BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
                removeEntities.add(entityId);
            }
        }
        return removeEntities;
    }
}