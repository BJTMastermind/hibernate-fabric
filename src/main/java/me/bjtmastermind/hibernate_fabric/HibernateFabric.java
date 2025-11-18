package me.bjtmastermind.hibernate_fabric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.bjtmastermind.hibernate_fabric.commands.GameRuleHandler;
import me.bjtmastermind.hibernate_fabric.commands.HibernateCommand;
import me.bjtmastermind.hibernate_fabric.config.Config;
import me.bjtmastermind.hibernate_fabric.world.ChunkUnloadHandler;
import me.bjtmastermind.hibernate_fabric.world.TickEventHandler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;

public class HibernateFabric implements ModInitializer {
    public static final String MOD_ID = "hibernate-fabric";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean hibernating;

    @Override
    public void onInitialize() {
        Config.load();

        // Set initial hibernation flag from config
        hibernating = Config.startEnabled;

        // Register everything
        HibernateCommand.register();
        GameRuleHandler.register();
        TickEventHandler.register();
        ChunkUnloadHandler.register();

        // Registers shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(MemoryManager::shutdown));
    }

    public static boolean isHibernating() {
        return hibernating;
    }

    public static void setHibernationState(MinecraftServer server, boolean state) {
        boolean wasHibernating = hibernating;
        hibernating = state;

        // Updates game rules
        GameRuleHandler.setHibernationGameRules(server, state);

        // Manages memory optimization
        if (!Config.enableMemoryOptimization) {
            return;
        }

        if (state && !wasHibernating) {
            // Entering hibernation
            if (Config.saveBeforeHibernation) {
                MemoryManager.saveImportantData(server);
            }
            MemoryManager.startMemoryOptimization(server);
        } else if (!state && wasHibernating) {
            // Exiting hibernation
            MemoryManager.stopMemoryOptimization();
        }
    }
}
