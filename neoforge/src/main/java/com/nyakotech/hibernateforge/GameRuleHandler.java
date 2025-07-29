package com.nyakotech.hibernateforge;

import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

public class GameRuleHandler {

    // Listener for when the server finishes initializing
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();

        // If configured to hibernate on startup AND no players are online
        if (CommonConfig.startEnabled && server.getPlayerList().getPlayers().isEmpty()) {
            Constants.LOG.info("Server started with no players — enabling hibernation");
            Hibernation.setHibernating(true);
            setHibernationGameRules(server, true);
        } else {
            Constants.LOG.info("Server started with hibernation disabled");
            Hibernation.setHibernating(false);
            setHibernationGameRules(server, false);
        }
    }

    public static void onPlayerLogin(PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        MinecraftServer server = player.getServer();

        // ALWAYS disable hibernation when a player joins
        if (Hibernation.isHibernating()) {
            Constants.LOG.info("Player {} connected — disabling hibernation", player.getName().getString());
            Hibernation.setHibernating(false);
            setHibernationGameRules(server, false);
        }
    }

    public static void onPlayerLogout(PlayerLoggedOutEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        MinecraftServer server = player.getServer();

        // Waits one tick to ensure the player list is updated
        server.execute(() -> {
            // If no more players are online, enable hibernation
            if (server.getPlayerList().getPlayers().isEmpty()) {
                Constants.LOG.info("Last player {} disconnected — enabling hibernation", player.getName().getString());
                Hibernation.setHibernating(true);
                setHibernationGameRules(server, true);
            } else {
                Constants.LOG.debug("Player {} disconnected, but there are still {} players online",
                        player.getName().getString(), server.getPlayerList().getPlayers().size());
            }
        });
    }

    public static void setHibernationGameRules(MinecraftServer server, boolean hibernating) {
        var rules = server.getGameRules();
        rules.getRule(GameRules.RULE_DAYLIGHT)      .set(!hibernating, server);
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(!hibernating, server);
        rules.getRule(GameRules.RULE_RANDOMTICKING) .set(hibernating ? 0 : 3, server);
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(!hibernating, server);
        rules.getRule(GameRules.RULE_DOFIRETICK)   .set(!hibernating, server);
    }
}
