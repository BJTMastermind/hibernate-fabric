package me.bjtmastermind.hibernate_fabric.world;

import java.util.ArrayList;
import java.util.List;

import me.bjtmastermind.hibernate_fabric.HibernateFabric;
import me.bjtmastermind.hibernate_fabric.config.Config;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

public class ChunkUnloadHandler {

    public static void register() {
        ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.register((level, chunk, oldLevelType, newLevelType) -> {
            if (Config.enableMemoryOptimization && newLevelType.equals(FullChunkStatus.INACCESSIBLE)) {
                ChunkPos chunkPos = chunk.getPos();

                List<Entity> entities = new ArrayList<>();
                level.getAllEntities().forEach(entity -> entities.add(entity));

                List<Entity> entitiesToRemove = entities.stream()
                    .filter(entity -> ChunkUnloadHandler.isEntityInChunk(entity, chunkPos))
                    .filter(ChunkUnloadHandler::canEntityBeRemovedDuringHibernation)
                    .toList();

                for (Entity entity : entitiesToRemove) {
                    entity.discard();
                }

                if (!entitiesToRemove.isEmpty()) {
                    HibernateFabric.LOGGER.info(
                        "{} inactive entities removed from chunk ({},{}) in dimension {}",
                        entitiesToRemove.size(),
                        chunkPos.x, chunkPos.z,
                        level.dimensionTypeRegistration().getRegisteredName()
                    );
                }
            }
        });
    }

    private static boolean isEntityInChunk(Entity entity, ChunkPos chunkPos) {
        BlockPos entityPos = entity.blockPosition();
        return Math.floorDiv(entityPos.getX(), 16) == chunkPos.x && Math.floorDiv(entityPos.getZ(), 16) == chunkPos.z;
    }

    private static boolean canEntityBeRemovedDuringHibernation(Entity entity) {
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

        if (Config.removeEntities.contains(ResourceLocation.parse("minecraft:item")) &&
            entityId.equals(ResourceLocation.parse("minecraft:item")))
        {
            return entity.tickCount >= (Config.droppedItemMaxAgeSeconds * 20);
        } else {
            return Config.removeEntities.contains(entityId) && !entity.hasCustomName();
        }
    }
}
