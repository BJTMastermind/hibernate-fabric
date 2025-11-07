package me.bjtmastermind.hibernatefabric;

import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class Config {
    public static boolean startEnabled = true;
    public static long ticksToSkip = 400L;
    public static int permissionLevel = 2;
    public static int sleepTimeMs = 75;

    public static boolean enableMemoryOptimization = true;
    public static int memoryCleanupIntervalSeconds = 30;
    public static double memoryThresholdPercent = 80.0;
    public static boolean forceGarbageCollection = true;
    public static int gcIntervalSeconds = 30;
    public static boolean saveBeforeHibernation = true;
    public static List<ResourceLocation> removeEntities = List.of(
        ResourceLocation.parse("minecraft:item"),
        ResourceLocation.parse("minecraft:firework_rocket"),
        ResourceLocation.parse("minecraft:arrow"),
        ResourceLocation.parse("minecraft:experience_orb")
    );
    public static int droppedItemMaxAgeSeconds = 300;
    public static boolean logMemoryUsage = true;

    public static boolean aggressiveCpuSaving = true;
    public static long minSleepInterval = 1500;
    public static double highLoadSleepMultiplier = 1.5;
    public static int yieldInterval = 8;
}