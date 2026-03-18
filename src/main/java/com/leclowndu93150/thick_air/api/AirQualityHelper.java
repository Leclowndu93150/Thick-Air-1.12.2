package com.leclowndu93150.thick_air.api;

import com.leclowndu93150.thick_air.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

import java.util.WeakHashMap;

public final class AirQualityHelper {

    private static final int SCAN_RADIUS_H = 9;
    private static final int SCAN_RADIUS_V = 4;
    private static final int SCAN_INTERVAL = 3;

    private static final WeakHashMap<EntityLivingBase, CachedResult> cache = new WeakHashMap<>();

    private AirQualityHelper() {}

    public static AirQualityLevel getAirQualityAtLocation(EntityLivingBase entity) {
        CachedResult cached = cache.get(entity);
        long tick = entity.world.getTotalWorldTime();
        if (cached != null && tick - cached.tick < SCAN_INTERVAL) {
            return cached.quality;
        }
        AirQualityLevel result = scanAirQuality(entity.world, entity.getPositionVector(), entity.getPositionEyes(1.0f));
        cache.put(entity, new CachedResult(result, tick));
        return result;
    }

    public static AirQualityLevel getAirQualityAtLocation(World world, Vec3d pos) {
        return scanAirQuality(world, pos, pos);
    }

    private static AirQualityLevel scanAirQuality(World world, Vec3d feetPos, Vec3d eyePos) {
        BlockPos eyeBlockPos = new BlockPos(eyePos);
        IBlockState blockAtEyes = world.getBlockState(eyeBlockPos);
        AirQualityLevel airQualityAtEyes = getAirQualityAtEyes(blockAtEyes);
        if (airQualityAtEyes != null) return airQualityAtEyes;

        AirQualityLevel bestQuality = null;

        BlockPos min = eyeBlockPos.add(-SCAN_RADIUS_H, -SCAN_RADIUS_V, -SCAN_RADIUS_H);
        BlockPos max = eyeBlockPos.add(SCAN_RADIUS_H, SCAN_RADIUS_V, SCAN_RADIUS_H);

        if (!world.isBlockLoaded(min) || !world.isBlockLoaded(max)) {
            return Config.getAirQualityAtLevelByDimension(getDimensionName(world), (int) feetPos.y);
        }

        ChunkCache chunkCache = new ChunkCache(world, min, max, 0);

        for (int x = -SCAN_RADIUS_H; x <= SCAN_RADIUS_H; x++) {
            for (int z = -SCAN_RADIUS_H; z <= SCAN_RADIUS_H; z++) {
                for (int y = -SCAN_RADIUS_V; y <= SCAN_RADIUS_V; y++) {
                    BlockPos blockPos = eyeBlockPos.add(x, y, z);
                    IBlockState state = chunkCache.getBlockState(blockPos);
                    AirQualityLevel quality = getAirQualityFromBlock(state);
                    if (quality == null) continue;

                    double radiusSq = quality.getAirProviderRadius() * quality.getAirProviderRadius();
                    double distanceSq = eyePos.squareDistanceTo(
                            blockPos.getX() + 0.5,
                            blockPos.getY() + 0.5,
                            blockPos.getZ() + 0.5
                    );

                    if (distanceSq < radiusSq) {
                        if (quality == AirQualityLevel.GREEN) return AirQualityLevel.GREEN;
                        if (bestQuality == null || quality.isBetterThan(bestQuality)) {
                            bestQuality = quality;
                        }
                    }
                }
            }
        }

        if (bestQuality != null) return bestQuality;

        return Config.getAirQualityAtLevelByDimension(getDimensionName(world), (int) feetPos.y);
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

    private static class CachedResult {
        final AirQualityLevel quality;
        final long tick;

        CachedResult(AirQualityLevel quality, long tick) {
            this.quality = quality;
            this.tick = tick;
        }
    }
}
