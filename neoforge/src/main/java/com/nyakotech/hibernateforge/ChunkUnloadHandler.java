package com.nyakotech.hibernateforge;

import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class ChunkUnloadHandler {
    private static boolean chunksUnloaded;

    public static void onLevelTick(LevelTickEvent.Post event) {
        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos spawn = level.getSharedSpawnPos();
        int cx = spawn.getX() >> 4, cz = spawn.getZ() >> 4;
        boolean h = Hibernation.isHibernating();
        if (h && !chunksUnloaded) {
            level.setChunkForced(cx, cz, false);
            chunksUnloaded = true;
        } else if (!h && chunksUnloaded) {
            level.setChunkForced(cx, cz, true);
            chunksUnloaded = false;
        }
    }
}
