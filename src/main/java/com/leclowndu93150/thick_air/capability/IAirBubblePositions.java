package com.leclowndu93150.thick_air.capability;

import com.leclowndu93150.thick_air.api.AirQualityLevel;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public interface IAirBubblePositions {

    Map<BlockPos, AirQualityLevel> getAirBubblePositions();

    Map<BlockPos, AirQualityLevel> getAirBubblePositionsView();

    int getSkipCountLeft();

    void setSkipCountLeft(int skipCountLeft);
}
