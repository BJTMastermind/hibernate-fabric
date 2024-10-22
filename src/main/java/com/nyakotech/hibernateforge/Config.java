package com.nyakotech.hibernateforge;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = HibernateForge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue START_ENABLED = BUILDER
            .comment("Whether to start the mod enabled")
            .define("startHibernated", true);

    private static final ForgeConfigSpec.LongValue TICKS_TO_SKIP = BUILDER
            .comment("Ticks to skip during hibernation")
            .defineInRange("ticksToSkip", 1000L, 1L, Long.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue PERMISSION_LEVEL = BUILDER
            .comment("Permission level needed to execute commands")
            .defineInRange("permissionLevel", 2, 0, 4);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean startEnabled;
    public static Long ticksToSkip;
    public static int permissionLevel;


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        startEnabled = START_ENABLED.get();
        ticksToSkip = TICKS_TO_SKIP.get();
        permissionLevel = PERMISSION_LEVEL.get();
    }
}
