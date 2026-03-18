package com.leclowndu93150.thick_air.handler;

import com.leclowndu93150.thick_air.api.AirQualityHelper;
import com.leclowndu93150.thick_air.api.AirQualityLevel;
import com.leclowndu93150.thick_air.capability.AirBubbleCapability;
import com.leclowndu93150.thick_air.capability.IAirBubblePositions;
import com.leclowndu93150.thick_air.network.ChunkAirQualityMessage;
import com.leclowndu93150.thick_air.network.PacketHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AirBubbleTracker {
    private static final Map<Integer, Set<ChunkPos>> CHUNKS_TO_SCAN = new ConcurrentHashMap<>();
    private static final Map<Integer, List<Map.Entry<ChunkPos, BlockPos>>> CHUNK_SCANNING_PROGRESS = new ConcurrentHashMap<>();

    public static void onBlockStateChange(WorldServer world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        ChunkPos chunkPos = new ChunkPos(pos);
        if (!world.isBlockLoaded(pos)) return;
        Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);

        IAirBubblePositions capability = chunk.getCapability(AirBubbleCapability.AIR_BUBBLE_CAP, null);
        if (capability == null) return;

        if (oldState.getBlock() != newState.getBlock()) {
            if (AirQualityHelper.getAirQualityFromBlock(oldState) != null) {
                AirQualityLevel removed = capability.getAirBubblePositions().remove(pos);
                if (removed != null) {
                    chunk.markDirty();
                    PacketHandler.INSTANCE.sendToAllTracking(
                            new ChunkAirQualityMessage(chunkPos, Collections.singletonMap(pos, removed), ChunkAirQualityMessage.Mode.REMOVE),
                            new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 0));
                }
            }

            AirQualityLevel airQualityLevel = AirQualityHelper.getAirQualityFromBlock(newState);
            if (airQualityLevel != null) {
                capability.getAirBubblePositions().put(pos, airQualityLevel);
                chunk.markDirty();
                PacketHandler.INSTANCE.sendToAllTracking(
                        new ChunkAirQualityMessage(chunkPos, Collections.singletonMap(pos, airQualityLevel), ChunkAirQualityMessage.Mode.ADD),
                        new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 0));
            }
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (event.getWorld() instanceof WorldServer) {
            int dim = event.getWorld().provider.getDimension();
            Chunk chunk = event.getChunk();
            CHUNKS_TO_SCAN.computeIfAbsent(dim, k -> ConcurrentHashMap.newKeySet()).add(chunk.getPos());
            CHUNK_SCANNING_PROGRESS.computeIfAbsent(dim, k -> Collections.synchronizedList(new LinkedList<>()))
                    .add(new AbstractMap.SimpleEntry<>(chunk.getPos(), getChunkStartingPosition(chunk)));
        }
    }

    @SubscribeEvent
    public void onChunkPopulated(PopulateChunkEvent.Post event) {
        if (event.getWorld() instanceof WorldServer) {
            int dim = event.getWorld().provider.getDimension();
            ChunkPos chunkPos = new ChunkPos(event.getChunkX(), event.getChunkZ());
            CHUNKS_TO_SCAN.computeIfAbsent(dim, k -> ConcurrentHashMap.newKeySet()).add(chunkPos);
            Chunk chunk = event.getWorld().getChunk(event.getChunkX(), event.getChunkZ());
            CHUNK_SCANNING_PROGRESS.computeIfAbsent(dim, k -> Collections.synchronizedList(new LinkedList<>()))
                    .add(new AbstractMap.SimpleEntry<>(chunkPos, getChunkStartingPosition(chunk)));
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getWorld() instanceof WorldServer) {
            int dim = event.getWorld().provider.getDimension();
            Set<ChunkPos> set = CHUNKS_TO_SCAN.get(dim);
            if (set != null) set.remove(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public void onChunkWatch(ChunkWatchEvent.Watch event) {
        EntityPlayerMP player = event.getPlayer();
        Chunk chunk = event.getChunkInstance();
        if (chunk == null) return;

        IAirBubblePositions capability = chunk.getCapability(AirBubbleCapability.AIR_BUBBLE_CAP, null);
        if (capability == null) return;

        Map<BlockPos, AirQualityLevel> airBubblePositions = capability.getAirBubblePositionsView();
        PacketHandler.INSTANCE.sendTo(
                new ChunkAirQualityMessage(chunk.getPos(), airBubblePositions, ChunkAirQualityMessage.Mode.REPLACE),
                player);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld() instanceof WorldServer) {
            int dim = event.getWorld().provider.getDimension();
            CHUNKS_TO_SCAN.remove(dim);
            CHUNK_SCANNING_PROGRESS.remove(dim);
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.world instanceof WorldServer)) return;
        WorldServer world = (WorldServer) event.world;
        int dim = world.provider.getDimension();

        List<Map.Entry<ChunkPos, BlockPos>> progress = CHUNK_SCANNING_PROGRESS.get(dim);
        if (progress == null || progress.isEmpty()) return;

        Set<ChunkPos> toScan = CHUNKS_TO_SCAN.get(dim);
        if (toScan == null) return;

        synchronized (progress) {
            if (progress.isEmpty()) return;

            ListIterator<Map.Entry<ChunkPos, BlockPos>> iterator = progress.listIterator();
            Map.Entry<ChunkPos, BlockPos> entry = iterator.next();
            ChunkPos chunkPos = entry.getKey();

            if (toScan.contains(chunkPos)) {
                if (!world.isBlockLoaded(new BlockPos(chunkPos.x << 4, 0, chunkPos.z << 4))) return;
                Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);

                IAirBubblePositions capability = chunk.getCapability(AirBubbleCapability.AIR_BUBBLE_CAP, null);
                if (capability == null) {
                    iterator.remove();
                    return;
                }

                capability.setSkipCountLeft(8);
                HashMap<BlockPos, AirQualityLevel> airBubblePositions = new HashMap<>();
                BlockPos blockPos = collectAirQualityPositions(chunk, entry.getValue(), airBubblePositions);

                if (entry.getValue().equals(getChunkStartingPosition(chunk))) {
                    capability.getAirBubblePositions().clear();
                    capability.getAirBubblePositions().putAll(airBubblePositions);
                    sendToTracking(world, chunkPos, airBubblePositions, ChunkAirQualityMessage.Mode.REPLACE);
                    chunk.markDirty();
                } else if (!airBubblePositions.isEmpty()) {
                    capability.getAirBubblePositions().putAll(airBubblePositions);
                    sendToTracking(world, chunkPos, airBubblePositions, ChunkAirQualityMessage.Mode.ADD);
                    chunk.markDirty();
                }

                if (blockPos != null) {
                    iterator.set(new AbstractMap.SimpleEntry<>(chunkPos, blockPos));
                    return;
                }
            }
            iterator.remove();
            toScan.remove(chunkPos);
        }
    }

    private void sendToTracking(WorldServer world, ChunkPos chunkPos, Map<BlockPos, AirQualityLevel> positions, ChunkAirQualityMessage.Mode mode) {
        ChunkAirQualityMessage msg = new ChunkAirQualityMessage(chunkPos, positions, mode);
        int centerX = (chunkPos.x << 4) + 8;
        int centerZ = (chunkPos.z << 4) + 8;
        PacketHandler.INSTANCE.sendToAllTracking(msg,
                new NetworkRegistry.TargetPoint(world.provider.getDimension(), centerX, 64, centerZ, 0));
    }

    private static BlockPos getChunkStartingPosition(Chunk chunk) {
        int posX = chunk.getPos().getXStart();
        int posY = 0;
        int posZ = chunk.getPos().getZStart();
        return new BlockPos(posX, posY, posZ);
    }

    private static BlockPos collectAirQualityPositions(Chunk chunk, BlockPos startingPosition, Map<BlockPos, AirQualityLevel> airBubbleEntries) {
        int minX = chunk.getPos().getXStart();
        int minY = 0;
        int minZ = chunk.getPos().getZStart();
        int startX = startingPosition.getX() - minX;
        int startY = startingPosition.getY();
        int startZ = startingPosition.getZ() - minZ;
        int iterations = 0;

        for (int dx = startX; dx < 16; dx++, startZ = 0) {
            for (int dz = startZ; dz < 16; dz++, startY = minY) {
                int posX = minX + dx;
                int posZ = minZ + dz;
                int maxY = chunk.getHeightValue(dx, dz);
                for (int posY = startY; posY < maxY; posY++, iterations++) {
                    BlockPos blockPos = new BlockPos(posX, posY, posZ);
                    if (iterations >= 98304) {
                        return blockPos;
                    }
                    IBlockState blockState = chunk.getBlockState(blockPos);
                    AirQualityLevel airQualityLevel = AirQualityHelper.getAirQualityFromBlock(blockState);
                    if (airQualityLevel != null) {
                        airBubbleEntries.put(blockPos, airQualityLevel);
                    }
                }
            }
        }
        return null;
    }
}
