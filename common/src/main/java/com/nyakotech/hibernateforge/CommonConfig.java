package com.nyakotech.hibernateforge;

public class CommonConfig {
    public static boolean startEnabled = true;
    public static long ticksToSkip = 200L;
    public static int permissionLevel = 2;
    public static int sleepTimeMs = 50;

    public static boolean enableMemoryOptimization = true;
    public static int memoryCleanupIntervalSeconds = 30;
    public static double memoryThresholdPercent = 80.0;
    public static int maxChunksToUnloadPerTick = 10;
    public static boolean forceGarbageCollection = true;
    public static int gcIntervalSeconds = 30;
    public static boolean saveBeforeHibernation = true;
    public static boolean removeOldDroppedItems = true;
    public static int droppedItemMaxAgeSeconds = 300;
    public static boolean removeProjectiles = true;
    public static boolean removeExperienceOrbs = true;
    public static boolean compactDataStructures = true;
    public static boolean logMemoryUsage = true;
}