package me.bjtmastermind.hibernate_fabric.world;

import me.bjtmastermind.hibernate_fabric.HibernateFabric;
import me.bjtmastermind.hibernate_fabric.MemoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class ChunkUnloadHandler {
    private static boolean chunksUnloaded = false;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (chunksUnloaded == HibernateFabric.isHibernating()) {
                return;
            }

            MemoryManager.forceLoadChunksWithRemovableEntities(server);
            chunksUnloaded = HibernateFabric.isHibernating();
        });
    }
}
