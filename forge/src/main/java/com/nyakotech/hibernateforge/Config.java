// forge/src/main/java/com/nyakotech/hibernateforge/Config.java
package com.nyakotech.hibernateforge;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = HibernateforgeForge.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
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

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        CommonConfig.startEnabled     = START_ENABLED.get();
        CommonConfig.ticksToSkip      = TICKS_TO_SKIP.get();
        CommonConfig.permissionLevel  = PERMISSION_LEVEL.get();
    }
}
