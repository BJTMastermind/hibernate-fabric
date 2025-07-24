package com.nyakotech.hibernateforge;

import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

public class GameRuleHandler {
    public static void onPlayerLogin(PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        MinecraftServer server = player.getServer();
        Hibernation.setHibernating(false);
        GameRuleHandler.setHibernationGameRules(server, false);
    }

    public static void onPlayerLogout(PlayerLoggedOutEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        MinecraftServer server = player.getServer();
        if (server.getPlayerList().getPlayers().isEmpty()) {
            Hibernation.setHibernating(true);
            GameRuleHandler.setHibernationGameRules(server, true);
        }
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
