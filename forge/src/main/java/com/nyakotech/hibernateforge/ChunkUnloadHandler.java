package com.nyakotech.hibernateforge;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.fml.LogicalSide;

@Mod.EventBusSubscriber(modid = HibernateforgeForge.MODID)
public class ChunkUnloadHandler {

    private static boolean chunksUnloaded = false;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.side != LogicalSide.SERVER || event.phase != TickEvent.Phase.END) {
            return;
        }

        if (HibernateforgeForge.isHibernating() && !chunksUnloaded) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            for (ServerLevel level : server.getAllLevels()) {
                // Remove the spawn chunk ticket
                ChunkPos spawnChunk = new ChunkPos(level.getSharedSpawnPos());
                level.getChunkSource().removeRegionTicket(TicketType.START, spawnChunk, 11, Unit.INSTANCE);
            }
            chunksUnloaded = true;
        } else if (!HibernateforgeForge.isHibernating() && chunksUnloaded) {
            // Re-add the spawn chunk ticket when hibernation ends
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            for (ServerLevel level : server.getAllLevels()) {
                ChunkPos spawnChunk = new ChunkPos(level.getSharedSpawnPos());
                level.getChunkSource().addRegionTicket(TicketType.START, spawnChunk, 11, Unit.INSTANCE);
            }
            chunksUnloaded = false;
        }
    }
}
