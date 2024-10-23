package com.nyakotech.hibernateforge;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = HibernateForge.MODID)
public class GameRuleHandler {
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!HibernateForge.isHibernating()) {
            setHibernationGameRules(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        setHibernationGameRules(false);
    }

    public static void setHibernationGameRules(boolean hibernating) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        GameRules rules = server.getGameRules();

        rules.getRule(GameRules.RULE_DAYLIGHT).set(!hibernating, server);
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(!hibernating, server);
        rules.getRule(GameRules.RULE_RANDOMTICKING).set(hibernating ? 0 : 3, server); // Default is 3
        rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(!hibernating, server);
        rules.getRule(GameRules.RULE_DOFIRETICK).set(!hibernating, server);
    }
}
