package com.leclowndu93150.thick_air;

import com.leclowndu93150.thick_air.block.SignalTorchBlock;
import com.leclowndu93150.thick_air.capability.AirBubbleCapability;
import com.leclowndu93150.thick_air.handler.AirBubbleTracker;
import com.leclowndu93150.thick_air.handler.ChokingAttackHandler;
import com.leclowndu93150.thick_air.handler.TickAirHandler;
import com.leclowndu93150.thick_air.network.PacketHandler;
import com.leclowndu93150.thick_air.proxy.CommonProxy;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ThickAir.MODID, name = ThickAir.MOD_NAME, version = Tags.VERSION,
        dependencies = "required-after:mixinbooter;after:baubles")
public class ThickAir {
    public static final String MODID = "thick_air";
    public static final String MOD_NAME = "Thick Air";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @SidedProxy(
            clientSide = "com.leclowndu93150.thick_air.proxy.ClientProxy",
            serverSide = "com.leclowndu93150.thick_air.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Config.init(event.getSuggestedConfigurationFile());
        AirBubbleCapability.register();
        PacketHandler.init();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new TickAirHandler());
        MinecraftForge.EVENT_BUS.register(new AirBubbleTracker());
        MinecraftForge.EVENT_BUS.register(new SignalTorchBlock.EventHandler());
        MinecraftForge.EVENT_BUS.register(new ChokingAttackHandler());
        MinecraftForge.EVENT_BUS.register(new LootHandler());
        proxy.init(event);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
