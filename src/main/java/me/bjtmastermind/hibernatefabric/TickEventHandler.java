package me.bjtmastermind.hibernatefabric;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class TickEventHandler {
    private static long tickCounter = 0;
    private static boolean wasHibernating = false;

    public static void register() {
        // Event when the server finishes initializing
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // If configured to hibernate on startup AND no players are online
            if (Config.startEnabled && server.getPlayerCount() == 0) {
                Constants.LOG.info("Server started with no players - activating hibernation.");
                HibernateFabric.setHibernationState(server, true);
            } else {
                Constants.LOG.info("Server started with hibernation disabled.");
                HibernateFabric.setHibernationState(server, false);
            }
        });

        // Player login: wake if hibernating
        ServerPlayConnectionEvents.JOIN.register((handler, client, server) -> {

            // ALWAYS disable hibernation when a player joins
            if (HibernateFabric.isHibernating()) {
                Constants.LOG.info("Player {} connected - disabling hibernation.", handler.getPlayer().getName().getString());
                HibernateFabric.setHibernationState(server, false);
            }
        });

        // Player logout: hibernate if no one left
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            String playerName = handler.getPlayer().getName().getString();

            // Waits one second to ensure the count is accurate
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            scheduler.schedule(() -> server.execute(() -> {
                // Check the actual player count on the server
                int actualPlayerCount = server.getPlayerCount();

                if (actualPlayerCount == 0 && !HibernateFabric.isHibernating()) {
                    Constants.LOG.info("Last player {} disconnected - activating hibernation.", playerName);
                    HibernateFabric.setHibernationState(server, true);
                } else if (actualPlayerCount > 0) {
                    Constants.LOG.debug("Player {} disconnected, but there are still {} players online.",
                            playerName, actualPlayerCount);
                }
            }), 1, TimeUnit.SECONDS);
        });

        // Each server tick with less CPU overhead
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            boolean isHibernating = HibernateFabric.isHibernating();

            // If hibernating but players are online, disable immediately
            if (isHibernating && server.getPlayerCount() >= 1) {
                Constants.LOG.warn("Hibernation was active with players online - disabling!");
                HibernateFabric.setHibernationState(server, false);
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
                if (tickCounter >= Config.ticksToSkip) {
                    tickCounter = 0;

                    try {
                        // Longer and less frequent sleep = less overhead
                        Thread.sleep(Config.sleepTimeMs * 2);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                // Yield to give other threads a chance without constant sleeping
                if (tickCounter % Config.yieldInterval == 0) {
                    Thread.yield();
                }
            }
        });
    }
}