package me.bjtmastermind.hibernatefabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;

public class GameRuleHandler {

    public static void register() {
        // When the server fully initializes
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // If configured to hibernate on startup AND no players are online
            if (Config.startEnabled && server.getPlayerCount() == 0) {
                Constants.LOG.info("Server started with no players - applying hibernation game rules.");
                setHibernationGameRules(server, true);
            } else {
                Constants.LOG.info("Server started - applying normal game rules.");
                setHibernationGameRules(server, false);
            }
        });

        // When a player connects - ALWAYS disable hibernation game rules
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Constants.LOG.debug("Player {} connected - applying normal game rules.",
                    handler.getPlayer().getName().getString());
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
                    Constants.LOG.info("Last player {} disconnected - applying hibernation game rules.", playerName);
                    setHibernationGameRules(server, true);
                } else {
                    Constants.LOG.debug("Player {} disconnected, but there are still {} players online - keeping normal game rules.",
                            playerName, server.getPlayerList().getPlayers().size());
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
        rules.getRule(GameRules.RULE_DAYLIGHT).set(!hibernating, server);

        // Weather cycle - OFF during hibernation
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(!hibernating, server);

        // Random tick speed - 0 during hibernation, 3 normally
        rules.getRule(GameRules.RULE_RANDOMTICKING).set(hibernating ? 0 : 3, server);

        // Mob spawning - OFF during hibernation
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(!hibernating, server);

        // Fire spread - OFF during hibernation
        rules.getRule(GameRules.RULE_DOFIRETICK).set(!hibernating, server);

        if (hibernating) {
            Constants.LOG.debug("Game rules set for hibernation: daylight=false, weather=false, randomTick=0, mobSpawn=false, fire=false");
        } else {
            Constants.LOG.debug("Game rules set to normal mode: daylight=true, weather=true, randomTick=3, mobSpawn=true, fire=true");
        }
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