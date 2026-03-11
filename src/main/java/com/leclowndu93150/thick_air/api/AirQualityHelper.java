package com.leclowndu93150.thick_air.api;

import com.leclowndu93150.thick_air.Config;
import com.leclowndu93150.thick_air.capability.AirBubbleCapability;
import com.leclowndu93150.thick_air.capability.IAirBubblePositions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AirQualityHelper {

    private AirQualityHelper() {}

    public static AirQualityLevel getAirQualityAtLocation(EntityLivingBase entity) {
        return getAirQualityAtLocation(entity.world, entity.getPositionVector(), entity.getPositionEyes(1.0f));
    }

    public static AirQualityLevel getAirQualityAtLocation(World world, Vec3d pos) {
        return getAirQualityAtLocation(world, pos, pos);
    }

    public static AirQualityLevel getAirQualityAtLocation(World world, Vec3d feetPos, Vec3d eyePos) {
        BlockPos eyeBlockPos = new BlockPos(eyePos);
        IBlockState blockAtEyes = world.getBlockState(eyeBlockPos);
        AirQualityLevel airQualityAtEyes = getAirQualityAtEyes(blockAtEyes);
        if (airQualityAtEyes != null) return airQualityAtEyes;

        AirQualityLevel bestAirBubbleQuality = null;
        ChunkPos chunkAtCenter = new ChunkPos(eyeBlockPos);

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                int cx = chunkAtCenter.x + x;
                int cz = chunkAtCenter.z + z;
                if (!world.isBlockLoaded(new BlockPos(cx << 4, 0, cz << 4))) continue;
                Chunk chunk = world.getChunk(cx, cz);

                IAirBubblePositions capability = chunk.getCapability(AirBubbleCapability.AIR_BUBBLE_CAP, null);
                if (capability == null) continue;

                Map<BlockPos, AirQualityLevel> airBubblePositions = capability.getAirBubblePositionsView();
                List<BlockPos> staleEntries = null;

                for (Map.Entry<BlockPos, AirQualityLevel> entry : airBubblePositions.entrySet()) {
                    BlockPos blockPos = entry.getKey();
                    AirQualityLevel airQualityLevel = entry.getValue();

                    IBlockState actualState = chunk.getBlockState(blockPos);
                    AirQualityLevel actualQuality = getAirQualityFromBlock(actualState);
                    if (actualQuality == null) {
                        if (staleEntries == null) staleEntries = new ArrayList<>();
                        staleEntries.add(blockPos);
                        continue;
                    }

                    if (bestAirBubbleQuality == null || airQualityLevel.isBetterThan(bestAirBubbleQuality)) {
                        double distanceSq = eyePos.squareDistanceTo(
                                blockPos.getX() + 0.5,
                                blockPos.getY() + 0.5,
                                blockPos.getZ() + 0.5
                        );
                        double radiusSq = airQualityLevel.getAirProviderRadius() * airQualityLevel.getAirProviderRadius();

                        if (distanceSq < radiusSq) {
                            if (airQualityLevel == AirQualityLevel.GREEN) {
                                removeStaleEntries(capability, staleEntries, chunk);
                                return AirQualityLevel.GREEN;
                            } else {
                                bestAirBubbleQuality = airQualityLevel;
                            }
                        }
                    }
                }

                removeStaleEntries(capability, staleEntries, chunk);
            }
        }

        if (bestAirBubbleQuality != null) {
            return bestAirBubbleQuality;
        }

        String dimName = getDimensionName(world);
        return Config.getAirQualityAtLevelByDimension(dimName, (int) feetPos.y);
    }

    private static void removeStaleEntries(IAirBubblePositions capability, List<BlockPos> staleEntries, Chunk chunk) {
        if (staleEntries == null) return;
        for (BlockPos pos : staleEntries) {
            capability.getAirBubblePositions().remove(pos);
        }
        chunk.markDirty();
    }

    public static boolean isSensitiveToAirQuality(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            return !player.capabilities.isCreativeMode && !player.isSpectator();
        }
        return false;
    }

    private static AirQualityLevel getAirQualityAtEyes(IBlockState blockState) {
        if (blockState.getMaterial().isLiquid() || blockState.getMaterial() == net.minecraft.block.material.Material.WATER) {
            return AirQualityLevel.RED;
        }
        return getAirQualityFromBlock(blockState);
    }

    public static AirQualityLevel getAirQualityFromBlock(IBlockState blockState) {
        if (blockState.getBlock() instanceof com.leclowndu93150.thick_air.block.SafetyLanternBlock) {
            return null;
        }

        net.minecraft.util.ResourceLocation regName = blockState.getBlock().getRegistryName();
        if (regName == null) return null;

        return Config.getAirProviderQuality(regName.toString());
    }

    private static String getDimensionName(World world) {
        int dimId = world.provider.getDimension();
        switch (dimId) {
            case 0: return "minecraft:overworld";
            case -1: return "minecraft:the_nether";
            case 1: return "minecraft:the_end";
            default: return "unknown:" + dimId;
        }
    }
}
