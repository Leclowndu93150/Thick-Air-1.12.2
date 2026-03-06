package com.leclowndu93150.thick_air.handler;

import com.leclowndu93150.thick_air.api.AirQualityHelper;
import com.leclowndu93150.thick_air.api.AirQualityLevel;
import com.leclowndu93150.thick_air.network.PacketHandler;
import com.leclowndu93150.thick_air.network.PlayerAirSyncMessage;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TickAirHandler {

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) return;
        if (!AirQualityHelper.isSensitiveToAirQuality(entity)) return;
        if (entity.isInsideOfMaterial(net.minecraft.block.material.Material.WATER)) return;

        AirQualityLevel airQualityLevel = AirQualityHelper.getAirQualityAtLocation(entity);
        int airAmount = airQualityLevel.getAirAmountAfterProtection(entity);

        if (airAmount > 0) {
            int currentAir = entity.getAir();
            if (currentAir < 300) {
                entity.setAir(Math.min(300, currentAir + airAmount));
            }
        } else if (airAmount < 0) {
            int currentAir = entity.getAir();
            int newAir = currentAir + airAmount;
            entity.setAir(newAir);

            if (newAir <= -20) {
                entity.setAir(0);
                entity.attackEntityFrom(DamageSource.DROWN, 2.0f);
            }
        } else if (airQualityLevel.canBreathe && !airQualityLevel.isEntityProtected(entity)) {
            if (entity.world.getTotalWorldTime() % 4 == 0) {
                int currentAir = entity.getAir();
                if (currentAir < 300) {
                    entity.setAir(Math.min(300, currentAir + 1));
                }
            }
        }

        if (entity instanceof EntityPlayerMP) {
            PacketHandler.INSTANCE.sendTo(
                    new PlayerAirSyncMessage(entity.getAir(), airQualityLevel.ordinal()),
                    (EntityPlayerMP) entity);
        }
    }
}
