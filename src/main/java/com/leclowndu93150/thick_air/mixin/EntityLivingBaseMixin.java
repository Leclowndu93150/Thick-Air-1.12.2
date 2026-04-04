package com.leclowndu93150.thick_air.mixin;

import com.leclowndu93150.thick_air.api.AirQualityHelper;
import com.leclowndu93150.thick_air.api.AirQualityLevel;
import com.leclowndu93150.thick_air.compat.AquaAcrobaticsCompat;
import com.leclowndu93150.thick_air.handler.ChokingAttackHandler;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin extends Entity {

    @Unique
    private int thickair$airBeforeUpdate = -1;

    public EntityLivingBaseMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "onEntityUpdate", at = @At("HEAD"))
    private void thickair$beforeEntityUpdate(CallbackInfo ci) {
        thickair$airBeforeUpdate = -1;
        EntityLivingBase self = (EntityLivingBase) (Object) this;
        if (!self.isInsideOfMaterial(Material.WATER) && AirQualityHelper.isSensitiveToAirQuality(self) && self.getAir() < 300) {
            AirQualityLevel quality = AirQualityHelper.getAirQualityAtLocation(self);
            boolean aquaHandling = quality == AirQualityLevel.GREEN
                    && AquaAcrobaticsCompat.isSlowReplenishActive()
                    && ChokingAttackHandler.getChokingTargetAir(self) < 0;
            if (!aquaHandling) {
                thickair$airBeforeUpdate = self.getAir();
            }
        }
    }

    @Inject(method = "onEntityUpdate", at = @At("RETURN"))
    private void thickair$afterEntityUpdate(CallbackInfo ci) {
        if (thickair$airBeforeUpdate >= 0) {
            EntityLivingBase self = (EntityLivingBase) (Object) this;
            if (self.getAir() > thickair$airBeforeUpdate) {
                self.setAir(thickair$airBeforeUpdate);
            }
            thickair$airBeforeUpdate = -1;
        }
    }
}
