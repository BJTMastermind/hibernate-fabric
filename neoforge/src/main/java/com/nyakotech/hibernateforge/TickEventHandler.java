package com.nyakotech.hibernateforge;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class TickEventHandler {
    private static long tickCounter = 0;
    private static boolean wasHibernating = false;

    public static void onServerTick(ServerTickEvent.Pre event) {
        boolean isHibernating = Hibernation.isHibernating();

        // If state changed, reset counter
        if (isHibernating != wasHibernating) {
            tickCounter = 0;
            wasHibernating = isHibernating;
        }

        if (isHibernating) {
            tickCounter++;

            // Less frequent sleep with longer intervals
            if (tickCounter >= CommonConfig.ticksToSkip) {
                tickCounter = 0;

                try {
                    // Longer and less frequent sleep = less overhead
                    Thread.sleep(CommonConfig.sleepTimeMs * 2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Yield to allow other threads a chance without constant sleep
            if (tickCounter % 5 == 0) {
                Thread.yield();
            }
        }
    }
}
