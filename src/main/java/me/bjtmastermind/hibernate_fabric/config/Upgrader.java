package me.bjtmastermind.hibernate_fabric.config;

import com.google.gson.JsonObject;

import me.bjtmastermind.hibernate_fabric.HibernateFabric;

public class Upgrader {

    public static boolean needsUpgrade() {
        JsonObject current = Config.getJson();
        if (!current.has("awakeGameRules") || !current.has("hibernatingGameRules")) {
            return true;
        }

        if (current.has("restoreGameRulesAs")) {
            return true;
        }

        if (current.has("logMemoryUsage")) {
            return true;
        }

        return false;
    }

    public static void upgrade() {
        HibernateFabric.LOGGER.info("Upgrading config...");

        JsonObject current = Config.getJson();
        JsonObject restoreGameRulesAs = current.getAsJsonObject("restoreGameRulesAs");

        // Get old values
        Boolean doDaylightCycle = restoreGameRulesAs.has("doDaylightCycle") ? restoreGameRulesAs.get("doDaylightCycle").getAsBoolean() : null;
        Boolean doWeatherCycle = restoreGameRulesAs.has("doWeatherCycle") ? restoreGameRulesAs.get("doWeatherCycle").getAsBoolean() : null;
        Integer randomTickSpeed = restoreGameRulesAs.has("randomTickSpeed") ? restoreGameRulesAs.get("randomTickSpeed").getAsInt() : null;
        Boolean doMobSpawning = restoreGameRulesAs.has("doMobSpawning") ? restoreGameRulesAs.get("doMobSpawning").getAsBoolean() : null;
        Boolean doFireTick = restoreGameRulesAs.has("doFireTick") ? restoreGameRulesAs.get("doFireTick").getAsBoolean() : null;
        Boolean logMemoryUsage = current.has("logMemoryUsage") ? current.get("logMemoryUsage").getAsBoolean() : null;
        Integer fireSpreadRadiusAroundPlayer = current.has("fireSpreadRadiusAroundPlayer") ? current.get("fireSpreadRadiusAroundPlayer").getAsInt() : null;

        // Convert to new values
        if (doDaylightCycle != null) Config.awakeAdvanceTime = doDaylightCycle.booleanValue();
        if (doWeatherCycle != null) Config.awakeAdvanceWeather = doWeatherCycle.booleanValue();
        if (randomTickSpeed != null) Config.awakeRandomTickSpeed = randomTickSpeed.intValue();
        if (doMobSpawning != null) Config.awakeSpawnMobs = doMobSpawning.booleanValue();
        if (doFireTick != null) Config.awakeFireSpreadRadiusAroundPlayer = doFireTick.booleanValue() ? 128 : 0;
        if (logMemoryUsage != null) Config.logMemoryInfo = logMemoryUsage.booleanValue();
        if (fireSpreadRadiusAroundPlayer != null) Config.awakeFireSpreadRadiusAroundPlayer = fireSpreadRadiusAroundPlayer.intValue();

        // Update Config
        Config.save();
        HibernateFabric.LOGGER.info("Config upgraded!");
    }
}
