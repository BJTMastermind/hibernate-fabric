package com.nyakotech.hibernateforge;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class TickEventHandler {
    private static long lastTickTime = 0;
    private static long tickCounter = 0;

    public static void onServerTick(ServerTickEvent.Pre event) {
        if (Hibernation.isHibernating()) {
            tickCounter++;

            // Só pula ticks se atingir o limite configurado
            if (tickCounter >= CommonConfig.ticksToSkip) {
                tickCounter = 0;

                // Sleep muito curto para reduzir uso de CPU, não travando o servidor
                try {
                    Thread.sleep(CommonConfig.sleepTimeMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            tickCounter = 0; // Reset quando não hibernando
        }
    }
}