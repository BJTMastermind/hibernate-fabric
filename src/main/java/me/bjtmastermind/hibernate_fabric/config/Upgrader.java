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

        // Convert to new values
        if (doDaylightCycle != null) Config.awakeDoDaylightCycle = doDaylightCycle.booleanValue();
        if (doWeatherCycle != null) Config.awakeDoWeatherCycle = doWeatherCycle.booleanValue();
        if (randomTickSpeed != null) Config.awakeRandomTickSpeed = randomTickSpeed.intValue();
        if (doMobSpawning != null) Config.awakeDoMobSpawning = doMobSpawning.booleanValue();
        if (doFireTick != null) Config.awakeDoFireTick = doFireTick.booleanValue();
        if (logMemoryUsage != null) Config.logMemoryInfo = logMemoryUsage.booleanValue();

        // Update Config
        Config.save();
        HibernateFabric.LOGGER.info("Config upgraded!");
    }
}
