package com.leclowndu93150.thick_air.compat;

import net.minecraftforge.fml.common.Loader;

public class AquaAcrobaticsCompat {

    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = Loader.isModLoaded("aquaacrobatics");
        }
        return loaded;
    }

    public static boolean isSlowReplenishActive() {
        if (!isLoaded()) return false;
        return isSlowReplenishInternal();
    }

    private static boolean isSlowReplenishInternal() {
        return com.fuzs.aquaacrobatics.config.ConfigHandler.MiscellaneousConfig.slowAirReplenish;
    }
}
