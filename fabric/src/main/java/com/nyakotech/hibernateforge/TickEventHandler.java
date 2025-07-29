package com.nyakotech.hibernateforge;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;

public class TickEventHandler {
    private static int onlinePlayers = 0;
    private static long tickCounter = 0;
    private static boolean wasHibernating = false;

    public static void register() {
        // Event when the server finishes initializing
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // If configured to hibernate on startup AND no players are online
            if (CommonConfig.startEnabled && server.getPlayerList().getPlayers().isEmpty()) {
                Constants.LOG.info("Server started with no players – activating hibernation.");
                HibernateforgeFabric.setHibernationState(server, true);
            } else {
                Constants.LOG.info("Server started with hibernation disabled.");
                HibernateforgeFabric.setHibernationState(server, false);
                onlinePlayers = server.getPlayerList().getPlayers().size();
            }
        });

        // Player login: increment count and wake if hibernating
        ServerPlayConnectionEvents.JOIN.register((handler, client, server) -> {
            onlinePlayers++;

            // ALWAYS disable hibernation when a player joins
            if (HibernateforgeFabric.isHibernating()) {
                Constants.LOG.info("Player {} connected – disabling hibernation.", handler.getPlayer().getName().getString());
                HibernateforgeFabric.setHibernationState(server, false);
            }
        });

        // Player logout: decrement count and hibernate if no one left
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            onlinePlayers--;
            String playerName = handler.getPlayer().getName().getString();

            // Waits one tick to ensure the count is accurate
            server.execute(() -> {
                // Check the actual player count on the server
                int actualPlayerCount = server.getPlayerList().getPlayers().size();

                if (actualPlayerCount == 0 && !HibernateforgeFabric.isHibernating()) {
                    Constants.LOG.info("Last player {} disconnected – activating hibernation.", playerName);
                    HibernateforgeFabric.setHibernationState(server, true);
                } else if (actualPlayerCount > 0) {
                    Constants.LOG.debug("Player {} disconnected, but there are still {} players online.",
                            playerName, actualPlayerCount);
                }

                // Synchronize counter with reality
                onlinePlayers = actualPlayerCount;
            });
        });

        // Each server tick with less CPU overhead
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            boolean isHibernating = HibernateforgeFabric.isHibernating();

            // If hibernating but players are online, disable immediately
            if (isHibernating && !server.getPlayerList().getPlayers().isEmpty()) {
                Constants.LOG.warn("Hibernation was active with players online – disabling!");
                HibernateforgeFabric.setHibernationState(server, false);
                onlinePlayers = server.getPlayerList().getPlayers().size();
                return;
            }

            // If the state changed, reset the counter
            if (isHibernating != wasHibernating) {
                tickCounter = 0;
                wasHibernating = isHibernating;
            }

            if (isHibernating) {
                tickCounter++;

                // Less frequent sleep with longer duration
                if (tickCounter >= CommonConfig.ticksToSkip) {
                    tickCounter = 0;

                    try {
                        // Longer and less frequent sleep = less overhead
                        Thread.sleep(CommonConfig.sleepTimeMs * 2);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                // Yield to give other threads a chance without constant sleeping
                if (tickCounter % CommonConfig.yieldInterval == 0) {
                    Thread.yield();
                }
            }
        });
    }
}