package me.bjtmastermind.hibernate_fabric.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.bjtmastermind.hibernate_fabric.HibernateFabric;
import me.bjtmastermind.hibernate_fabric.config.Config;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.gamerules.GameRules;

public class GameRuleHandler {

    public static void register() {
        // When the server fully initializes
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (Config.startEnabled && server.getPlayerCount() == 0) {
                HibernateFabric.LOGGER.info("Server started with no players - applying hibernation game rules.");
                setHibernationGameRules(server, true);
                asyncEntitySpawnFixForDevEnv(server);
                return;
            }
            HibernateFabric.LOGGER.info("Server started - applying normal game rules.");
            setHibernationGameRules(server, false);
            asyncEntitySpawnFixForDevEnv(server);
        });

        // When a player connects - ALWAYS disable hibernation game rules
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            setHibernationGameRules(server, false);
            mc304138WorkaroundFix(server);
        });

        // When a player disconnects
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            String playerName = handler.getPlayer().getName().getString();

            // Waits one tick to ensure the player list is updated
            server.execute(() -> {
                if (server.getPlayerCount() == 0) {
                    HibernateFabric.LOGGER.info("Last player {} disconnected - applying hibernation game rules.", playerName);
                    setHibernationGameRules(server, true);
                }
            });
        });
    }

    /**
     * Sets the game rules based on the hibernation state
     * @param server The server
     * @param hibernating Whether it is hibernating (true) or not (false)
     */
    public static void setHibernationGameRules(MinecraftServer server, boolean hibernating) {
        GameRules rules = server.getWorldData().getGameRules();

        // Advance time - OFF during hibernation
        rules.set(GameRules.ADVANCE_TIME, hibernating ? false : Config.advanceTime, server);

        // Advance weather - OFF during hibernation
        rules.set(GameRules.ADVANCE_WEATHER, hibernating ? false : Config.advanceWeather, server);

        // Random tick speed - 0 during hibernation
        rules.set(GameRules.RANDOM_TICK_SPEED, hibernating ? 0 : Config.randomTickSpeed, server);

        // Spawn mobs - OFF during hibernation
        rules.set(GameRules.SPAWN_MOBS, hibernating ? false : Config.spawnMobs, server);

        // Fire spread radius - 0 during hibernation
        rules.set(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, hibernating ? 0 : Config.fireSpreadRadiusAroundPlayer, server);
    }

    // Workaround for https://bugs.mojang.com/browse/MC/issues/MC-304138
    private static void mc304138WorkaroundFix(MinecraftServer server) {
        Difficulty originalDifficulty = server.getWorldData().getDifficulty();
        switch (originalDifficulty) {
            case PEACEFUL -> server.setDifficulty(Difficulty.EASY, false);
            case EASY -> server.setDifficulty(Difficulty.NORMAL, false);
            case NORMAL -> server.setDifficulty(Difficulty.EASY, false);
            case HARD -> server.setDifficulty(Difficulty.NORMAL, false);
        }
        server.setDifficulty(originalDifficulty, false);
    }

    private static void asyncEntitySpawnFixForDevEnv(MinecraftServer server) {
        if (FabricLoader.getInstance().isModLoaded("async") && FabricLoader.getInstance().isDevelopmentEnvironment()) {
            HibernateFabric.LOGGER.info("Async detected - disabling async entity spawn.");

            CommandSourceStack source = server.createCommandSourceStack()
                .withPermission(PermissionSet.ALL_PERMISSIONS)
                .withSuppressedOutput();

            try {
                server.getCommands().getDispatcher().execute("async config setAsyncEntitySpawn false", source);
            } catch (CommandSyntaxException e) {
                HibernateFabric.LOGGER.error("Failed to disable async entity spawn: ", e);
            }
        }
    }
}