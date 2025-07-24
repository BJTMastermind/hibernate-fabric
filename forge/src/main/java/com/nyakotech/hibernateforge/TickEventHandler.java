package com.nyakotech.hibernateforge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HibernateforgeForge.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class TickEventHandler {
    private static int onlinePlayers = 0;

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        onlinePlayers++;
        if (HibernateforgeForge.isHibernating()) {
            HibernateforgeForge.setHibernationState(false);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        onlinePlayers--;
        if (onlinePlayers <= 0 && HibernateforgeForge.isHibernating()) {
            HibernateforgeForge.setHibernationState(true);
            // Optionally log or notify that hibernation has started
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.side != LogicalSide.SERVER || event.phase != TickEvent.Phase.START) {
            return;
        }

        if (HibernateforgeForge.isHibernating()) {
            try {
                Thread.sleep(Config.ticksToSkip); // Sleep duration from config
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
