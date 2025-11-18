package me.bjtmastermind.hibernate_fabric.world;

import me.bjtmastermind.hibernate_fabric.HibernateFabric;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class ChunkUnloadHandler {
    private static boolean chunksUnloaded = false;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (chunksUnloaded == HibernateFabric.isHibernating()) {
                return;
            }

            for (ServerLevel level : server.getAllLevels()) {
                BlockPos spawn = level.getRespawnData().pos();
                int chunkX = spawn.getX() >> 4;
                int chunkZ = spawn.getZ() >> 4;

                level.setChunkForced(chunkX, chunkZ, !HibernateFabric.isHibernating());
            }

            chunksUnloaded = HibernateFabric.isHibernating();
        });
    }
}
