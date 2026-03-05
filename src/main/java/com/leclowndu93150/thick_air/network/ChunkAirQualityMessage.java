package com.leclowndu93150.thick_air.network;

import com.leclowndu93150.thick_air.api.AirQualityLevel;
import com.leclowndu93150.thick_air.capability.AirBubbleCapability;
import com.leclowndu93150.thick_air.capability.IAirBubblePositions;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

public class ChunkAirQualityMessage implements IMessage {
    private int chunkX;
    private int chunkZ;
    private Map<BlockPos, AirQualityLevel> airBubblePositions;
    private Mode mode;

    public ChunkAirQualityMessage() {
        this.airBubblePositions = new HashMap<>();
    }

    public ChunkAirQualityMessage(ChunkPos chunkPos, Map<BlockPos, AirQualityLevel> positions, Mode mode) {
        this.chunkX = chunkPos.x;
        this.chunkZ = chunkPos.z;
        this.airBubblePositions = positions;
        this.mode = mode;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.chunkX = buf.readInt();
        this.chunkZ = buf.readInt();
        int size = buf.readInt();
        this.airBubblePositions = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            BlockPos pos = BlockPos.fromLong(buf.readLong());
            AirQualityLevel quality = AirQualityLevel.values()[buf.readByte()];
            this.airBubblePositions.put(pos, quality);
        }
        this.mode = Mode.values()[buf.readByte()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeInt(airBubblePositions.size());
        for (Map.Entry<BlockPos, AirQualityLevel> entry : airBubblePositions.entrySet()) {
            buf.writeLong(entry.getKey().toLong());
            buf.writeByte(entry.getValue().ordinal());
        }
        buf.writeByte(mode.ordinal());
    }

    public enum Mode {
        REPLACE, REMOVE, ADD
    }

    public static class Handler implements IMessageHandler<ChunkAirQualityMessage, IMessage> {
        @Override
        public IMessage onMessage(ChunkAirQualityMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                World world = Minecraft.getMinecraft().world;
                if (world != null && world.isBlockLoaded(new BlockPos(message.chunkX << 4, 0, message.chunkZ << 4))) {
                    Chunk chunk = world.getChunk(message.chunkX, message.chunkZ);
                    IAirBubblePositions capability = chunk.getCapability(AirBubbleCapability.AIR_BUBBLE_CAP, null);
                    if (capability != null) {
                        Map<BlockPos, AirQualityLevel> positions = capability.getAirBubblePositions();
                        switch (message.mode) {
                            case REPLACE:
                                positions.clear();
                                positions.putAll(message.airBubblePositions);
                                break;
                            case ADD:
                                positions.putAll(message.airBubblePositions);
                                break;
                            case REMOVE:
                                for (BlockPos pos : message.airBubblePositions.keySet()) {
                                    positions.remove(pos);
                                }
                                break;
                        }
                    }
                }
            });
            return null;
        }
    }
}
