package com.leclowndu93150.thick_air.mixin;

import com.leclowndu93150.thick_air.api.AirQualityHelper;
import com.leclowndu93150.thick_air.api.AirQualityLevel;
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
    private int thickair$savedAir = -1;

    public EntityLivingBaseMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "onEntityUpdate", at = @At("HEAD"))
    private void thickair$beforeEntityUpdate(CallbackInfo ci) {
        if (!this.world.isRemote) {
            EntityLivingBase self = (EntityLivingBase) (Object) this;
            if (!self.isInsideOfMaterial(Material.WATER) && AirQualityHelper.isSensitiveToAirQuality(self)) {
                AirQualityLevel quality = AirQualityHelper.getAirQualityAtLocation(self);
                if (!quality.canBreathe) {
                    thickair$savedAir = self.getAir();
                    return;
                }
            }
        }
        thickair$savedAir = -1;
    }

    @Inject(method = "onEntityUpdate", at = @At("RETURN"))
    private void thickair$afterEntityUpdate(CallbackInfo ci) {
        if (thickair$savedAir != -1) {
            EntityLivingBase self = (EntityLivingBase) (Object) this;
            self.setAir(thickair$savedAir);
            thickair$savedAir = -1;
        }
    }
}
