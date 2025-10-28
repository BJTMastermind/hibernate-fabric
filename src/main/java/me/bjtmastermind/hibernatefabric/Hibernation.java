package me.bjtmastermind.hibernatefabric;

/**
 * Loader-agnostic holder of whether the server is in “hibernation” mode.
 */
public class Hibernation {
    private static boolean hibernating = false;

    /** Called by your loader modules to set the state */
    public static void setHibernating(boolean state) {
        hibernating = state;
    }

    /** Called by mixins and game logic to query the state */
    public static boolean isHibernating() {
        return hibernating;
    }
}
