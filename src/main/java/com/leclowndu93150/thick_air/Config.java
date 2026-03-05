package com.leclowndu93150.thick_air;

import com.leclowndu93150.thick_air.api.AirQualityLevel;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.*;

public class Config {
    private static Configuration config;

    public static boolean enableSignalTorches = true;
    public static boolean enableBaubles = true;
    public static int drownedChoking = 0;
    public static double yellowAirProviderRadius = 6.0;
    public static double blueAirProviderRadius = 6.0;
    public static double redAirProviderRadius = 3.0;
    public static double greenAirProviderRadius = 9.0;
    private static String[] dimensionLines = {
            "minecraft:overworld=yellow,40:green,128:yellow",
            "minecraft:the_nether=yellow",
            "minecraft:the_end=red"
    };
    private static Map<String, DimensionEntry> dimensionEntries = null;

    private static String[] greenProviderBlocks = {
            "minecraft:end_portal",
            "minecraft:end_gateway",
            "minecraft:portal"
    };
    private static String[] blueProviderBlocks = {
            "minecraft:soul_sand"
    };
    private static String[] yellowProviderBlocks = {
            "minecraft:torch",
            "minecraft:lit_furnace",
            "minecraft:lit_redstone_lamp",
            "minecraft:glowstone",
            "minecraft:redstone_torch",
            "minecraft:lit_pumpkin",
            "minecraft:fire"
    };
    private static String[] redProviderBlocks = {
            "minecraft:lava",
            "minecraft:flowing_lava"
    };
    private static Map<String, AirQualityLevel> airProviderMap = null;

    public static AirQualityLevel getAirProviderQuality(String registryName) {
        if (airProviderMap == null) {
            airProviderMap = new HashMap<>();
            for (String s : greenProviderBlocks) airProviderMap.put(s.trim(), AirQualityLevel.GREEN);
            for (String s : blueProviderBlocks) airProviderMap.put(s.trim(), AirQualityLevel.BLUE);
            for (String s : yellowProviderBlocks) airProviderMap.put(s.trim(), AirQualityLevel.YELLOW);
            for (String s : redProviderBlocks) airProviderMap.put(s.trim(), AirQualityLevel.RED);
        }
        return airProviderMap.get(registryName);
    }

    public static void init(File file) {
        config = new Configuration(file);
        config.load();
        syncConfig();
    }

    private static void syncConfig() {
        enableSignalTorches = config.getBoolean("enableSignalTorches", Configuration.CATEGORY_GENERAL,
                true, "Whether to allow right-clicking torches to make them spray particle effects");

        enableBaubles = config.getBoolean("enableBaubles", Configuration.CATEGORY_GENERAL,
                true, "Whether to allow the Respirator to be worn as a Baubles HEAD slot item (requires Baubles mod)");

        drownedChoking = config.getInt("drownedChoking", Configuration.CATEGORY_GENERAL,
                0, 0, 72000, "How much air an attack from certain mobs removes. Set to 0 to disable.");

        yellowAirProviderRadius = config.getFloat("yellowAirProviderRadius", "ranges",
                6.0f, 1.0f, 32.0f, "Radius of yellow air provider bubbles");

        blueAirProviderRadius = config.getFloat("blueAirProviderRadius", "ranges",
                6.0f, 1.0f, 32.0f, "Radius of blue air provider bubbles");

        redAirProviderRadius = config.getFloat("redAirProviderRadius", "ranges",
                3.0f, 1.0f, 32.0f, "Radius of red air provider bubbles");

        greenAirProviderRadius = config.getFloat("greenAirProviderRadius", "ranges",
                9.0f, 1.0f, 32.0f, "Radius of green air provider bubbles");

        greenProviderBlocks = config.getStringList("greenAirProviderBlocks", "air_providers",
                greenProviderBlocks,
                "Blocks that create GREEN air bubbles (breathable, refills air).\n" +
                "Use registry names like modid:blockname.");
        blueProviderBlocks = config.getStringList("blueAirProviderBlocks", "air_providers",
                blueProviderBlocks,
                "Blocks that create BLUE air bubbles (breathable, does not refill air).\n" +
                "Use registry names like modid:blockname.");
        yellowProviderBlocks = config.getStringList("yellowAirProviderBlocks", "air_providers",
                yellowProviderBlocks,
                "Blocks that create YELLOW air bubbles (slowly drains air).\n" +
                "Use registry names like modid:blockname.");
        redProviderBlocks = config.getStringList("redAirProviderBlocks", "air_providers",
                redProviderBlocks,
                "Blocks that create RED air bubbles (quickly drains air).\n" +
                "Use registry names like modid:blockname.");
        airProviderMap = null;

        Property dimensionProp = config.get(Configuration.CATEGORY_GENERAL, "dimensions", dimensionLines,
                "Air qualities at different heights in different dimensions.\n" +
                "Syntax: dimension_id=default_quality,height:quality,height:quality\n" +
                "Entries must be in ascending height order.\n" +
                "If a dimension is not listed, it defaults to GREEN everywhere.");
        dimensionLines = dimensionProp.getStringList();
        dimensionEntries = null;

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static AirQualityLevel getAirQualityAtLevelByDimension(String dimension, int y) {
        if (dimensionEntries == null) {
            dimensionEntries = new HashMap<>();
            for (String line : dimensionLines) {
                Map.Entry<String, DimensionEntry> entry = parseDimensionLine(line);
                if (entry != null) {
                    dimensionEntries.put(entry.getKey(), entry.getValue());
                }
            }
        }

        DimensionEntry entry = dimensionEntries.get(dimension);
        if (entry != null) {
            List<int[]> heights = entry.heights;
            for (int i = heights.size() - 1; i >= 0; i--) {
                if (y >= heights.get(i)[0]) {
                    return AirQualityLevel.values()[heights.get(i)[1]];
                }
            }
            return entry.baseQuality;
        }
        return AirQualityLevel.GREEN;
    }

    private static Map.Entry<String, DimensionEntry> parseDimensionLine(String line) {
        String[] dimensionVals = line.split("=");
        if (dimensionVals.length != 2) return null;

        String dimKey = dimensionVals[0].trim();
        String[] heightAndRest = dimensionVals[1].split(",", 2);

        AirQualityLevel baseQuality;
        try {
            baseQuality = AirQualityLevel.valueOf(heightAndRest[0].trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }

        List<int[]> heights = new ArrayList<>();
        if (heightAndRest.length == 2) {
            String[] heightPairStrs = heightAndRest[1].split(",");
            int prevHeight = Integer.MIN_VALUE;
            for (String heightPairStr : heightPairStrs) {
                String[] pairStr = heightPairStr.trim().split(":");
                if (pairStr.length != 2) return null;

                int height;
                try {
                    height = Integer.parseInt(pairStr[0].trim());
                } catch (NumberFormatException e) {
                    return null;
                }
                if (height <= prevHeight) return null;
                prevHeight = height;

                AirQualityLevel quality;
                try {
                    quality = AirQualityLevel.valueOf(pairStr[1].trim().toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    return null;
                }
                heights.add(new int[]{height, quality.ordinal()});
            }
        }

        return new AbstractMap.SimpleEntry<>(dimKey, new DimensionEntry(baseQuality, heights));
    }

    private static class DimensionEntry {
        final AirQualityLevel baseQuality;
        final List<int[]> heights;

        DimensionEntry(AirQualityLevel baseQuality, List<int[]> heights) {
            this.baseQuality = baseQuality;
            this.heights = heights;
        }
    }
}
