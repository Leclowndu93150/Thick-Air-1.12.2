package com.leclowndu93150.thick_air.mixin;

import com.leclowndu93150.thick_air.compat.BaublesCompat;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LayerArmorBase.class)
public class LayerBipedArmorMixin {

    @Redirect(method = "renderArmorLayer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityLivingBase;getItemStackFromSlot(Lnet/minecraft/inventory/EntityEquipmentSlot;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack thickair$getSlotWithBaubles(EntityLivingBase entity, EntityEquipmentSlot slot) {
        ItemStack original = entity.getItemStackFromSlot(slot);
        if (slot == EntityEquipmentSlot.HEAD && original.isEmpty()) {
            ItemStack baubleResp = BaublesCompat.getRespiratorFromBaubles(entity);
            if (!baubleResp.isEmpty()) {
                return baubleResp;
            }
        }
        return original;
    }
}
