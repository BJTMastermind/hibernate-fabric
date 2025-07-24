package com.nyakotech.hibernateforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Your NeoForge “@Mod” entrypoint.
 */
@Mod(Constants.MOD_ID)
public class HibernateforgeNeoforge {
    public HibernateforgeNeoforge(IEventBus modBus) {
        Constants.LOG.info("Loading NeoForge module");
        CommonClass.init();

        IEventBus GAME = NeoForge.EVENT_BUS;
        GAME.addListener(HibernationCommand::register);
        GAME.addListener(GameRuleHandler::onPlayerLogin);
        GAME.addListener(GameRuleHandler::onPlayerLogout);
        GAME.addListener(TickEventHandler::onServerTick);
        GAME.addListener(ChunkUnloadHandler::onLevelTick);
    }
}
