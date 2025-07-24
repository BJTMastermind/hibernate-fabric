package com.nyakotech.hibernateforge;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;

public class TickEventHandler {
    private static int onlinePlayers = 0;

    public static void register() {
        // Player login: increment count and wake if hibernating
        ServerPlayConnectionEvents.JOIN.register((handler, client, server) -> {
            onlinePlayers++;
            if (HibernateforgeFabric.isHibernating()) {
                HibernateforgeFabric.setHibernationState(server, false);
            }
        });

        // Player logout: decrement count and hibernate if no one left
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            onlinePlayers--;
            if (onlinePlayers <= 0 && HibernateforgeFabric.isHibernating()) {
                HibernateforgeFabric.setHibernationState(server, true);
            }
        });

        // Each server tick: if hibernating, sleep for configured ticksToSkip
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (HibernateforgeFabric.isHibernating()) {
                try {
                    Thread.sleep(CommonConfig.ticksToSkip);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
}
