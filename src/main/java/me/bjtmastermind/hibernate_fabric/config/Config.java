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
    private static Path cfgDir = FabricLoader.getInstance().getConfigDir();
    private static Gson gson = setupGson();

    public static final Path configFile = cfgDir.resolve("hibernate-fabric.json");

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
    public static boolean logMemoryInfo = false;

    public static boolean aggressiveCpuSaving = true;
    public static long minSleepInterval = 1500;
    public static double highLoadSleepMultiplier = 1.5;
    public static int yieldInterval = 8;

    public static boolean awakeAdvanceTime = true;
    public static boolean awakeAdvanceWeather = true;
    public static int awakeRandomTickSpeed = 3;
    public static boolean awakeSpawnMobs = true;
    public static int awakeFireSpreadRadiusAroundPlayer = 128;

    public static boolean hibernatingAdvanceTime = false;
    public static boolean hibernatingAdvanceWeather = false;
    public static int hibernatingRandomTickSpeed = 0;
    public static boolean hibernatingSpawnMobs = false;
    public static int hibernatingFireSpreadRadiusAroundPlayer = 0;

    public static void load() {
        try {
            // If no config on disk, write defaults
            if (Files.notExists(configFile)) {
                save();
            }

            // Read whatever's in the file, override Config class
            try (BufferedReader reader = Files.newBufferedReader(configFile)) {
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
                logMemoryInfo = obj.has("logMemoryInfo") ? obj.get("logMemoryInfo").getAsBoolean() : logMemoryInfo;

                // NEW SETTINGS:
                aggressiveCpuSaving = obj.has("aggressiveCpuSaving") ? obj.get("aggressiveCpuSaving").getAsBoolean() : aggressiveCpuSaving;
                minSleepInterval = obj.has("minSleepInterval") ? obj.get("minSleepInterval").getAsLong() : minSleepInterval;
                highLoadSleepMultiplier = obj.has("highLoadSleepMultiplier") ? obj.get("highLoadSleepMultiplier").getAsDouble() : highLoadSleepMultiplier;
                yieldInterval = obj.has("yieldInterval") ? obj.get("yieldInterval").getAsInt() : yieldInterval;

                // NEW GAMERULES SETTINGS:
                JsonObject awakeGameRules = obj.has("awakeGameRules") ? obj.getAsJsonObject("awakeGameRules") : new JsonObject();
                awakeAdvanceTime = awakeGameRules.has("advance_time") ?
                    awakeGameRules.get("advance_time").getAsBoolean() :
                    awakeAdvanceTime;
                awakeAdvanceWeather = awakeGameRules.has("advance_weather") ?
                    awakeGameRules.get("advance_weather").getAsBoolean() :
                    awakeAdvanceWeather;
                awakeRandomTickSpeed = awakeGameRules.has("random_tick_speed") ?
                    awakeGameRules.get("random_tick_speed").getAsInt() :
                    awakeRandomTickSpeed;
                awakeSpawnMobs = awakeGameRules.has("spawn_mobs") ?
                    awakeGameRules.get("spawn_mobs").getAsBoolean() :
                    awakeSpawnMobs;
                awakeFireSpreadRadiusAroundPlayer = awakeGameRules.has("fire_spread_radius_around_player") ?
                    awakeGameRules.get("fire_spread_radius_around_player").getAsInt() :
                    awakeFireSpreadRadiusAroundPlayer;

                JsonObject hibernatingGameRules = obj.has("hibernatingGameRules") ? obj.getAsJsonObject("hibernatingGameRules") : new JsonObject();
                hibernatingAdvanceTime = hibernatingGameRules.has("advance_time") ?
                    hibernatingGameRules.get("advance_time").getAsBoolean() :
                    hibernatingAdvanceTime;
                hibernatingAdvanceWeather = hibernatingGameRules.has("advance_weather") ?
                    hibernatingGameRules.get("advance_weather").getAsBoolean() :
                    hibernatingAdvanceWeather;
                hibernatingRandomTickSpeed = hibernatingGameRules.has("random_tick_speed") ?
                    hibernatingGameRules.get("random_tick_speed").getAsInt() :
                    hibernatingRandomTickSpeed;
                hibernatingSpawnMobs = hibernatingGameRules.has("spawn_mobs") ?
                    hibernatingGameRules.get("spawn_mobs").getAsBoolean() :
                    hibernatingSpawnMobs;
                hibernatingFireSpreadRadiusAroundPlayer = hibernatingGameRules.has("fire_spread_radius_around_player") ?
                    hibernatingGameRules.get("fire_spread_radius_around_player").getAsInt() :
                    hibernatingFireSpreadRadiusAroundPlayer;
            }

        } catch (IOException e) {
            System.err.println("Failed to load hibernate-fabric config, using defaults:");
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
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
            defaults.addProperty("logMemoryInfo", logMemoryInfo);

            // NEW SETTINGS FOR CPU OPTIMIZATION:
            defaults.addProperty("aggressiveCpuSaving", aggressiveCpuSaving);
            defaults.addProperty("minSleepInterval", minSleepInterval);
            defaults.addProperty("highLoadSleepMultiplier", highLoadSleepMultiplier);
            defaults.addProperty("yieldInterval", yieldInterval);

            // NEW SETTINGS FOR RESTORING GAMERULE SETTINGS:
            JsonObject awakeGameRules = new JsonObject();
            awakeGameRules.addProperty("advance_time", awakeAdvanceTime);
            awakeGameRules.addProperty("advance_weather", awakeAdvanceWeather);
            awakeGameRules.addProperty("random_tick_speed", awakeRandomTickSpeed);
            awakeGameRules.addProperty("spawn_mobs", awakeSpawnMobs);
            awakeGameRules.addProperty("fire_spread_radius_around_player", awakeFireSpreadRadiusAroundPlayer);
            defaults.add("awakeGameRules", awakeGameRules);

            JsonObject hibernatingGameRules = new JsonObject();
            hibernatingGameRules.addProperty("advance_time", hibernatingAdvanceTime);
            hibernatingGameRules.addProperty("advance_weather", hibernatingAdvanceWeather);
            hibernatingGameRules.addProperty("random_tick_speed", hibernatingRandomTickSpeed);
            hibernatingGameRules.addProperty("spawn_mobs", hibernatingSpawnMobs);
            hibernatingGameRules.addProperty("fire_spread_radius_around_player", hibernatingFireSpreadRadiusAroundPlayer);
            defaults.add("hibernatingGameRules", hibernatingGameRules);

            Files.createDirectories(cfgDir);
            try (BufferedWriter writer = Files.newBufferedWriter(configFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                gson.toJson(defaults, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save hibernate-fabric config.");
            e.printStackTrace();
        }
    }

    public static JsonObject getJson() {
        try {
            if (!Files.exists(configFile)) {
                return new JsonObject();
            }

            // Read whatever's in the file
            try (BufferedReader reader = Files.newBufferedReader(configFile)) {
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