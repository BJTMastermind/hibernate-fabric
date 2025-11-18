package me.bjtmastermind.hibernate_fabric.commands;

import me.bjtmastermind.hibernate_fabric.HibernateFabric;
import me.bjtmastermind.hibernate_fabric.config.Config;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;

public class GameRuleHandler {

    public static void register() {
        // When the server fully initializes
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (Config.startEnabled && server.getPlayerCount() == 0) {
                HibernateFabric.LOGGER.info("Server started with no players - applying hibernation game rules.");
                setHibernationGameRules(server, true);
                return;
            }
            HibernateFabric.LOGGER.info("Server started - applying normal game rules.");
            setHibernationGameRules(server, false);
        });

        // When a player connects - ALWAYS disable hibernation game rules
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            setHibernationGameRules(server, false);
            mc304138WorkaroundFix(server);
        });

        // When a player disconnects
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            String playerName = handler.getPlayer().getName().getString();

            // Waits one tick to ensure the player list is updated
            server.execute(() -> {
                if (server.getPlayerCount() == 0) {
                    HibernateFabric.LOGGER.info("Last player {} disconnected - applying hibernation game rules.", playerName);
                    setHibernationGameRules(server, true);
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
        GameRules rules = server.getGameRules();

        // Daylight cycle - OFF during hibernation
        rules.getRule(GameRules.RULE_DAYLIGHT).set(hibernating ? false : Config.doDaylightCycle, server);

        // Weather cycle - OFF during hibernation
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(hibernating ? false : Config.doWeatherCycle, server);

        // Random tick speed - 0 during hibernation
        rules.getRule(GameRules.RULE_RANDOMTICKING).set(hibernating ? 0 : Config.randomTickSpeed, server);

        // Mob spawning - OFF during hibernation
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(hibernating ? false : Config.doMobSpawning, server);

        // Fire spread - OFF during hibernation
        rules.getRule(GameRules.RULE_DOFIRETICK).set(hibernating ? false : Config.doFireTick, server);
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