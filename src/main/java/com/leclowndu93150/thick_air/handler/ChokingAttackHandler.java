package com.leclowndu93150.thick_air.handler;

import com.leclowndu93150.thick_air.Config;
import com.leclowndu93150.thick_air.network.PacketHandler;
import com.leclowndu93150.thick_air.network.PlayerAirSyncMessage;
import com.leclowndu93150.thick_air.api.AirQualityHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChokingAttackHandler {

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (Config.chokingAmount <= 0) return;

        EntityLivingBase target = event.getEntityLiving();
        if (target.world.isRemote) return;
        if (!(target instanceof EntityPlayerMP)) return;

        Entity attacker = event.getSource().getTrueSource();
        if (attacker == null) return;

        ResourceLocation registryName = net.minecraft.entity.EntityList.getKey(attacker);
        if (registryName == null) return;

        if (Config.isChokingEntity(registryName.toString())) {
            EntityPlayerMP player = (EntityPlayerMP) target;
            int newAir = Math.max(-20, player.getAir() - Config.chokingAmount);
            player.setAir(newAir);

            int qualityOrdinal = AirQualityHelper.getAirQualityAtLocation(player).ordinal();
            PacketHandler.INSTANCE.sendTo(
                    new PlayerAirSyncMessage(player.getAir(), qualityOrdinal),
                    player);
        }
    }
}
