package com.nyakotech.hibernateforge;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

public class GameRuleHandler {
    /**
     * Call this once from your Fabric entrypoint (e.g. onInitialize()).
     */
    public static void register() {
        // When a player joins, turn game rules back ON
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                setHibernationGameRules(server, false)
        );

        // When the last player disconnects, turn game rules OFF
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                setHibernationGameRules(server, true)
        );

        // Optional: ensure rules are correct on server start
        ServerTickEvents.START_SERVER_TICK.register(server ->
                setHibernationGameRules(server, /* your initial hibernation state */ false)
        );
    }

    /**
     * Flip all the rules based on whether we're hibernating.
     */
    public static void setHibernationGameRules(MinecraftServer server, boolean hibernating) {
        GameRules rules = server.getGameRules();
        // daylight
        rules.getRule(GameRules.RULE_DAYLIGHT).set(!hibernating, server);
        // weather
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(!hibernating, server);
        // random ticks per chunk (0 when off, 3 by default)
        rules.getRule(GameRules.RULE_RANDOMTICKING)
                .set(hibernating ? 0 : 3, server);
        // mob spawning
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(!hibernating, server);
        // fire spread
        rules.getRule(GameRules.RULE_DOFIRETICK).set(!hibernating, server);
    }
}
