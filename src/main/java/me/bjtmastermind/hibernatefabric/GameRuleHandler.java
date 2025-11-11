package me.bjtmastermind.hibernatefabric;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;

public class GameRuleHandler {
    private static boolean serverJustStarted = true;
    private static boolean firstPlayerJoined = false;

    public static void register() {
        // When the server fully initializes
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // If configured to hibernate on startup AND no players are online
            if (Config.startEnabled && server.getPlayerCount() == 0) {
                HibernateFabric.LOGGER.info("Server started with no players - applying hibernation game rules.");
                setHibernationGameRules(server, true);
            } else {
                HibernateFabric.LOGGER.info("Server started - applying normal game rules.");
                setHibernationGameRules(server, false);
            }
            serverJustStarted = false;
        });

        // When a player connects - ALWAYS disable hibernation game rules
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            firstPlayerJoined = true;
            String playerName = handler.getPlayer().getName().getString();

            HibernateFabric.LOGGER.debug("Player {} connected - applying normal game rules.", playerName);
            readGameRulesCookie(server);
            setHibernationGameRules(server, false);
            mc304138WorkaroundFix(server);
        });

        // When a player disconnects
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            String playerName = handler.getPlayer().getName().getString();

            // Waits one tick to ensure the player list is updated
            server.execute(() -> {
                // If no players are online, apply hibernation game rules
                if (server.getPlayerCount() == 0) {
                    HibernateFabric.LOGGER.info("Last player {} disconnected - applying hibernation game rules.", playerName);
                    setHibernationGameRules(server, true);
                } else {
                    HibernateFabric.LOGGER.debug("Player {} disconnected, but there are still {} players online - keeping normal game rules.",
                            playerName, server.getPlayerCount());
                }
            });
        });
    }

    /**
     * Sets the game rules based on the hibernation state
     * @param server The server
     * @param hibernating Whether it is hibernating (true) or not (false)
     */
    public static void setHibernationGameRules(MinecraftServer server, boolean hibernating) {
        if (hibernating && !serverJustStarted && firstPlayerJoined) writeGameRulesCookie(server);

        GameRules rules = server.getGameRules();

        // Daylight cycle - OFF during hibernation
        rules.getRule(GameRules.RULE_DAYLIGHT).set(hibernating ? false : GameRulesCookie.doDaylightCycle, server);

        // Weather cycle - OFF during hibernation
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(hibernating ? false : GameRulesCookie.doWeatherCycle, server);

        // Random tick speed - 0 during hibernation
        rules.getRule(GameRules.RULE_RANDOMTICKING).set(hibernating ? 0 : GameRulesCookie.randomTickSpeed, server);

        // Mob spawning - OFF during hibernation
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(hibernating ? false : GameRulesCookie.doMobSpawning, server);

        // Fire spread - OFF during hibernation
        rules.getRule(GameRules.RULE_DOFIRETICK).set(hibernating ? false : GameRulesCookie.doFireTick, server);

        HibernateFabric.LOGGER.debug(
            "Game rules set {}: daylight={}, weather={}, randomTick={}, mobSpawn={}, fire={}",
            hibernating ? "for hibernation" : "to normal mode",
            rules.getRule(GameRules.RULE_DAYLIGHT).get(),
            rules.getRule(GameRules.RULE_WEATHER_CYCLE).get(),
            rules.getRule(GameRules.RULE_RANDOMTICKING).get(),
            rules.getRule(GameRules.RULE_DOMOBSPAWNING).get(),
            rules.getRule(GameRules.RULE_DOFIRETICK).get()
        );
    }

    private static void readGameRulesCookie(MinecraftServer server) {
        if (!HibernateFabric.isHibernating()) return;

        Path cookieDir  = server.getServerDirectory();
        Path cookieFile = cookieDir.resolve(HibernateFabric.MOD_ID+"_gamerules.cookie.json");
        Gson gson = new Gson();

        if (Files.exists(cookieFile)) {
            HibernateFabric.LOGGER.info("Reading gamerules from cookie at: "+cookieFile.getFileName());
        }

        try {
            try (BufferedReader reader = Files.newBufferedReader(cookieFile)) {
                JsonObject obj = gson.fromJson(reader, JsonObject.class);
                GameRulesCookie.doDaylightCycle = obj.has("doDaylightCycle") ? obj.get("doDaylightCycle").getAsBoolean() : true;
                GameRulesCookie.doWeatherCycle = obj.has("doWeatherCycle") ? obj.get("doWeatherCycle").getAsBoolean() : true;
                GameRulesCookie.randomTickSpeed = obj.has("randomTickSpeed") ? obj.get("randomTickSpeed").getAsInt() : 3;
                GameRulesCookie.doMobSpawning = obj.has("doMobSpawning") ? obj.get("doMobSpawning").getAsBoolean() : true;
                GameRulesCookie.doFireTick = obj.has("doFireTick") ? obj.get("doFireSpread").getAsBoolean() : true;
            }
            Files.deleteIfExists(cookieFile);
        } catch (IOException e) {}
    }

    private static void writeGameRulesCookie(MinecraftServer server) {
        Path cookieDir  = server.getServerDirectory();
        Path cookieFile = cookieDir.resolve(HibernateFabric.MOD_ID+"_gamerules.cookie.json");
        Gson gson = new Gson();

        GameRules gamerules = server.getGameRules();

        if (!hasDefaultGameRules(gamerules)) {
            HibernateFabric.LOGGER.info("Writing gamerules to cookie at: "+cookieFile.getFileName());

            try {
                if (Files.notExists(cookieFile)) {
                    JsonObject cookie = new JsonObject();
                    if (!gamerules.getRule(GameRules.RULE_DAYLIGHT).get()) {
                        cookie.addProperty("doDaylightCycle", gamerules.getRule(GameRules.RULE_DAYLIGHT).get());
                    }

                    if (!gamerules.getRule(GameRules.RULE_WEATHER_CYCLE).get()) {
                        cookie.addProperty("doWeatherCycle", gamerules.getRule(GameRules.RULE_WEATHER_CYCLE).get());
                    }

                    if (gamerules.getRule(GameRules.RULE_RANDOMTICKING).get() != 3) {
                        cookie.addProperty("randomTickSpeed", gamerules.getRule(GameRules.RULE_RANDOMTICKING).get());
                    }

                    if (!gamerules.getRule(GameRules.RULE_DOMOBSPAWNING).get()) {
                        cookie.addProperty("doMobSpawning", gamerules.getRule(GameRules.RULE_DOMOBSPAWNING).get());
                    }

                    if (!gamerules.getRule(GameRules.RULE_DOFIRETICK).get()) {
                        cookie.addProperty("doFireTick", gamerules.getRule(GameRules.RULE_DOFIRETICK).get());
                    }

                    try (BufferedWriter writer = Files.newBufferedWriter(cookieFile, StandardOpenOption.CREATE_NEW)) {
                        gson.toJson(cookie, writer);
                    }
                }

            } catch (IOException e) {
                System.err.println("Failed to write gamerules cookie to disk!");
                e.printStackTrace();
            }
        }
    }

    private static boolean hasDefaultGameRules(GameRules gamerules) {
        return (
            gamerules.getRule(GameRules.RULE_DAYLIGHT).get() &&
            gamerules.getRule(GameRules.RULE_WEATHER_CYCLE).get() &&
            gamerules.getRule(GameRules.RULE_RANDOMTICKING).get() == 3 &&
            gamerules.getRule(GameRules.RULE_DOMOBSPAWNING).get() &&
            gamerules.getRule(GameRules.RULE_DOFIRETICK).get()
        );
    }

    // Workaround for https://bugs.mojang.com/browse/MC/issues/MC-304138
    private static void mc304138WorkaroundFix(MinecraftServer server) {
        Difficulty originalDifficulty = server.getWorldData().getDifficulty();
        switch (originalDifficulty) {
            case PEACEFUL -> server.setDifficulty(Difficulty.EASY, false);
            case EASY -> server.setDifficulty(Difficulty.NORMAL, false);
            case NORMAL -> server.setDifficulty(Difficulty.EASY, false);
            case HARD -> server.setDifficulty(Difficulty.NORMAL, false);
        }
        server.setDifficulty(originalDifficulty, false);
    }
}