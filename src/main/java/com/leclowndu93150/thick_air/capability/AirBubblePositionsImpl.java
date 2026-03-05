package com.leclowndu93150.thick_air.capability;

import com.leclowndu93150.thick_air.api.AirQualityLevel;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class AirBubblePositionsImpl implements IAirBubblePositions {
    private int skipCountLeft;
    private final Map<BlockPos, AirQualityLevel> airBubbleEntries = new LinkedHashMap<>();

    @Override
    public Map<BlockPos, AirQualityLevel> getAirBubblePositions() {
        return this.airBubbleEntries;
    }

    @Override
    public Map<BlockPos, AirQualityLevel> getAirBubblePositionsView() {
        return Collections.unmodifiableMap(this.airBubbleEntries);
    }

    @Override
    public int getSkipCountLeft() {
        return this.skipCountLeft;
    }

    @Override
    public void setSkipCountLeft(int skipCountLeft) {
        this.skipCountLeft = skipCountLeft;
    }
}
