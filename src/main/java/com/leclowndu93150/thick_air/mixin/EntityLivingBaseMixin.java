package com.leclowndu93150.thick_air.mixin;

import com.leclowndu93150.thick_air.api.AirQualityHelper;
import com.leclowndu93150.thick_air.api.AirQualityLevel;
import com.leclowndu93150.thick_air.network.PacketHandler;
import com.leclowndu93150.thick_air.network.PlayerAirSyncMessage;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin extends Entity {

    @Unique
    private boolean thickair$shouldRestore = false;

    @Unique
    private int thickair$savedAir = 0;

    @Unique
    private boolean thickair$shouldSyncAfter = false;

    @Unique
    private int thickair$qualityOrdinal = 0;

    public EntityLivingBaseMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "onEntityUpdate", at = @At("HEAD"))
    private void thickair$beforeEntityUpdate(CallbackInfo ci) {
        thickair$shouldRestore = false;
        thickair$shouldSyncAfter = false;
        if (!this.world.isRemote) {
            EntityLivingBase self = (EntityLivingBase) (Object) this;
            if (!self.isInsideOfMaterial(Material.WATER) && AirQualityHelper.isSensitiveToAirQuality(self)) {
                AirQualityLevel quality = AirQualityHelper.getAirQualityAtLocation(self);
                thickair$qualityOrdinal = quality.ordinal();
                if (quality != AirQualityLevel.GREEN) {
                    thickair$savedAir = self.getAir();
                    thickair$shouldRestore = true;
                } else if (self instanceof EntityPlayerMP) {
                    thickair$shouldSyncAfter = true;
                }
            }
        }
    }

    @Inject(method = "onEntityUpdate", at = @At("RETURN"))
    private void thickair$afterEntityUpdate(CallbackInfo ci) {
        if (thickair$shouldRestore) {
            EntityLivingBase self = (EntityLivingBase) (Object) this;
            self.setAir(thickair$savedAir);
            thickair$shouldRestore = false;
        }
        if (thickair$shouldSyncAfter) {
            EntityPlayerMP player = (EntityPlayerMP) (Object) this;
            PacketHandler.INSTANCE.sendTo(
                    new PlayerAirSyncMessage(player.getAir(), thickair$qualityOrdinal),
                    player);
            thickair$shouldSyncAfter = false;
        }
    }
}
