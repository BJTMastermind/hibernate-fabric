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

public class HibernateforgeFabric implements ModInitializer {
    private static boolean hibernating;

    @Override
    public void onInitialize() {
        loadConfig();

        // Set initial hibernation flag from config
        hibernating = CommonConfig.startEnabled;

        // Register everything
        HibernationCommand.register();
        GameRuleHandler.register();
        TickEventHandler.register();
        ChunkUnloadHandler.register();
    }

    /** Exposed to your command logic */
    public static boolean isHibernating() { return hibernating; }

    public static void setHibernationState(MinecraftServer server, boolean state) {
        hibernating = state;
        GameRuleHandler.setHibernationGameRules(server, state);
    }

    private void loadConfig() {
        try {
            Path cfgDir  = FabricLoader.getInstance().getConfigDir();
            Path cfgFile = cfgDir.resolve("hibernateforge.json");
            Gson gson    = new GsonBuilder().setPrettyPrinting().create();

            // If no config on disk, write defaults
            if (Files.notExists(cfgFile)) {
                JsonObject defaults = new JsonObject();
                defaults.addProperty("startEnabled", CommonConfig.startEnabled);
                defaults.addProperty("ticksToSkip",  CommonConfig.ticksToSkip);
                defaults.addProperty("permissionLevel", CommonConfig.permissionLevel);

                Files.createDirectories(cfgDir);
                try (var writer = Files.newBufferedWriter(cfgFile, StandardOpenOption.CREATE_NEW)) {
                    gson.toJson(defaults, writer);
                }
            }

            // Read whatever’s in the file, override CommonConfig
            try (var reader = Files.newBufferedReader(cfgFile)) {
                JsonObject obj = gson.fromJson(reader, JsonObject.class);
                CommonConfig.startEnabled    = obj.has("startEnabled")    ? obj.get("startEnabled").getAsBoolean() : CommonConfig.startEnabled;
                CommonConfig.ticksToSkip     = obj.has("ticksToSkip")     ? obj.get("ticksToSkip").getAsLong()      : CommonConfig.ticksToSkip;
                CommonConfig.permissionLevel = obj.has("permissionLevel") ? obj.get("permissionLevel").getAsInt()   : CommonConfig.permissionLevel;
            }

        } catch (IOException e) {
            // If something goes wrong, stick with defaults and log to console
            System.err.println("Failed to load hibernateforge config, using defaults:");
            e.printStackTrace();
        }
    }
}
