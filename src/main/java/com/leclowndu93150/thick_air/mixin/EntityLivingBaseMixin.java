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
    private boolean thickair$shouldRestore = false;

    @Unique
    private int thickair$savedAir = 0;

    public EntityLivingBaseMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "onEntityUpdate", at = @At("HEAD"))
    private void thickair$beforeEntityUpdate(CallbackInfo ci) {
        thickair$shouldRestore = false;
        if (!this.world.isRemote) {
            EntityLivingBase self = (EntityLivingBase) (Object) this;
            if (!self.isInsideOfMaterial(Material.WATER) && AirQualityHelper.isSensitiveToAirQuality(self)) {
                AirQualityLevel quality = AirQualityHelper.getAirQualityAtLocation(self);
                if (quality != AirQualityLevel.GREEN) {
                    thickair$savedAir = self.getAir();
                    thickair$shouldRestore = true;
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
    }
}
