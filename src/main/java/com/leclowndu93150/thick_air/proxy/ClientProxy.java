package com.leclowndu93150.thick_air.proxy;

import com.leclowndu93150.thick_air.ModRegistry;
import com.leclowndu93150.thick_air.ThickAir;
import com.leclowndu93150.thick_air.api.AirQualityHelper;
import com.leclowndu93150.thick_air.api.AirQualityLevel;
import com.leclowndu93150.thick_air.client.AirBarRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
    }

    @Override
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new AirBarRenderer());

        ModRegistry.SAFETY_LANTERN_ITEM.addPropertyOverride(
                new ResourceLocation(ThickAir.MODID, "air_quality_level"),
                (stack, worldIn, entityIn) -> {
                    if (entityIn != null) {
                        AirQualityLevel quality = AirQualityHelper.getAirQualityAtLocation(entityIn);
                        return quality.getItemModelProperty();
                    }
                    return AirQualityLevel.YELLOW.getItemModelProperty();
                });
    }
}
