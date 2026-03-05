package com.leclowndu93150.thick_air.network;

import com.leclowndu93150.thick_air.ThickAir;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ThickAir.MODID);

    private static int nextId = 0;

    public static void init() {
        INSTANCE.registerMessage(ChunkAirQualityMessage.Handler.class, ChunkAirQualityMessage.class, nextId++, Side.CLIENT);
        INSTANCE.registerMessage(PlayerAirSyncMessage.Handler.class, PlayerAirSyncMessage.class, nextId++, Side.CLIENT);
    }
}
