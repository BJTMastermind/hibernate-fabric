package me.bjtmastermind.hibernate_fabric.config;

import com.google.gson.JsonObject;

import me.bjtmastermind.hibernate_fabric.HibernateFabric;

public class Upgrader {

    public static boolean needsUpgrade() {
        JsonObject current = Config.getJson();
        if (!current.has("restoreGameRulesAs")) {
            return false;
        }

        JsonObject restoreGameRulesAs = current.getAsJsonObject("restoreGameRulesAs");
        if (restoreGameRulesAs.has("doDaylightCycle") ||
            restoreGameRulesAs.has("doWeatherCycle") ||
            restoreGameRulesAs.has("randomTickSpeed") ||
            restoreGameRulesAs.has("doMobSpawning") ||
            restoreGameRulesAs.has("doFireTick")) {
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

        // Convert to new values
        if (doDaylightCycle != null) Config.advanceTime = doDaylightCycle.booleanValue();
        if (doDaylightCycle != null) Config.advanceWeather = doWeatherCycle.booleanValue();
        if (doDaylightCycle != null) Config.randomTickSpeed = randomTickSpeed.intValue();
        if (doDaylightCycle != null) Config.spawnMobs = doMobSpawning.booleanValue();
        if (doDaylightCycle != null) Config.fireSpreadRadiusAroundPlayer = doFireTick.booleanValue() ? 128 : 0;

        // Update Config
        Config.save();
        HibernateFabric.LOGGER.info("Config upgraded!");
    }
}
