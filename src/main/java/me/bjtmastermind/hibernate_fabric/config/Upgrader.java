package me.bjtmastermind.hibernate_fabric.config;

import com.google.gson.JsonObject;

import me.bjtmastermind.hibernate_fabric.HibernateFabric;

public class Upgrader {

    public static boolean needsUpgrade() {
        JsonObject current = Config.getJson();
        if (current.has("logMemoryUsage")) {
            return true;
        }

        return false;
    }

    public static void upgrade() {
        HibernateFabric.LOGGER.info("Upgrading config...");

        JsonObject current = Config.getJson();

        // Get old values
        Boolean logMemoryUsage = current.has("logMemoryUsage") ? current.get("logMemoryUsage").getAsBoolean() : null;

        // Convert to new values
        if (logMemoryUsage != null) Config.logMemoryInfo = logMemoryUsage.booleanValue();

        // Update Config
        Config.save();
        HibernateFabric.LOGGER.info("Config upgraded!");
    }
}
