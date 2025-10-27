package com.nyakotech.hibernateforge;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;

public class ChunkUnloadHandler {
    private static boolean chunksUnloaded = false;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (chunksUnloaded == HibernateFabric.isHibernating()) return;

            for (ServerWorld level : server.getWorlds()) {
                BlockPos spawn = level.getSpawnPoint().getPos();
                int chunkX = spawn.getX() >> 4;
                int chunkZ = spawn.getZ() >> 4;

                level.setChunkForced(chunkX, chunkZ, !HibernateFabric.isHibernating());
            }

            chunksUnloaded = HibernateFabric.isHibernating();
        });
    }
}
