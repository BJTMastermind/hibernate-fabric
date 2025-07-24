package com.nyakotech.hibernateforge;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class TickEventHandler {
    public static void onServerTick(ServerTickEvent.Pre event) {
        if (Hibernation.isHibernating()) {
            try { Thread.sleep(CommonConfig.ticksToSkip); } catch (InterruptedException ignored) {}
        }
    }
}
