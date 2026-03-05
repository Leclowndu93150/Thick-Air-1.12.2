package com.leclowndu93150.thick_air.network;

import com.leclowndu93150.thick_air.client.ClientAirData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PlayerAirSyncMessage implements IMessage {
    private int air;
    private int qualityOrdinal;

    public PlayerAirSyncMessage() {}

    public PlayerAirSyncMessage(int air, int qualityOrdinal) {
        this.air = air;
        this.qualityOrdinal = qualityOrdinal;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.air = buf.readShort();
        this.qualityOrdinal = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(air);
        buf.writeByte(qualityOrdinal);
    }

    public static class Handler implements IMessageHandler<PlayerAirSyncMessage, IMessage> {
        @Override
        public IMessage onMessage(PlayerAirSyncMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                ClientAirData.air = message.air;
                ClientAirData.qualityOrdinal = message.qualityOrdinal;
            });
            return null;
        }
    }
}
