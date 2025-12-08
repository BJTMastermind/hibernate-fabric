package me.bjtmastermind.hibernate_fabric.world;

import java.util.List;

import me.bjtmastermind.hibernate_fabric.HibernateFabric;
import me.bjtmastermind.hibernate_fabric.config.Config;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

public class ChunkUnloadHandler {

    public static void register() {
        ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE.register((level, chunk, oldLevelType, newLevelType) -> {
            if (Config.enableMemoryOptimization && newLevelType.equals(FullChunkStatus.INACCESSIBLE)) {
                ChunkPos chunkPos = chunk.getPos();
                List<Entity> entities = level.getEntities(
                    EntityTypeTest.forClass(Entity.class),
                    new AABB(
                        chunkPos.x * 16, Double.MIN_VALUE, chunkPos.z * 16,
                        chunkPos.x * 16 + 16, Double.MAX_VALUE, chunkPos.z * 16 + 16
                    ),
                    entity -> true
                );

                List<Entity> entitiesToRemove = entities.stream()
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
