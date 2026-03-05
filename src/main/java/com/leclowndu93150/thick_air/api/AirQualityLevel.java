package com.leclowndu93150.thick_air.api;

import com.leclowndu93150.thick_air.Config;
import com.leclowndu93150.thick_air.ThickAir;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum AirQualityLevel implements IStringSerializable {
    GREEN(true, true),
    BLUE(true, false),
    YELLOW(false, false),
    RED(false, false);

    public final boolean canBreathe;
    public final boolean canRefillAir;

    AirQualityLevel(boolean canBreathe, boolean canRefillAir) {
        this.canBreathe = canBreathe;
        this.canRefillAir = canRefillAir;
    }

    @Override
    public String getName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public double getAirProviderRadius() {
        switch (this) {
            case RED: return Config.redAirProviderRadius;
            case GREEN: return Config.greenAirProviderRadius;
            case YELLOW: return Config.yellowAirProviderRadius;
            case BLUE: return Config.blueAirProviderRadius;
            default: return 6.0;
        }
    }

    public int getLightLevel() {
        return 15 - this.ordinal() * 3;
    }

    public int getOutputSignal() {
        return this.ordinal() + 1;
    }

    public boolean isBetterThan(AirQualityLevel other) {
        return this.ordinal() < other.ordinal();
    }

    public int getAirAmountAfterProtection(EntityLivingBase entity) {
        return this.isProtected(entity) ? 0 : this.getAirAmount(entity);
    }

    boolean isProtected(EntityLivingBase entity) {
        if (entity.isPotionActive(MobEffects.WATER_BREATHING)) return true;
        if (entity.isHandActive()) {
            ItemStack useItem = entity.getActiveItemStack();
            if (useItem.getItem().getRegistryName() != null &&
                    useItem.getItem().getRegistryName().getNamespace().equals(ThickAir.MODID) &&
                    useItem.getItem().getRegistryName().getPath().contains("air_bladder")) {
                return true;
            }
        }
        return isProtectedViaBreathingEquipment(entity);
    }

    private boolean isProtectedViaBreathingEquipment(EntityLivingBase entity) {
        if (this == GREEN || this == BLUE) return false;

        ItemStack helmet = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (checkRespirator(helmet, entity)) return true;

        ItemStack baubleResp = com.leclowndu93150.thick_air.compat.BaublesCompat.getRespiratorFromBaubles(entity);
        if (checkRespirator(baubleResp, entity)) return true;

        return false;
    }

    private boolean checkRespirator(ItemStack stack, EntityLivingBase entity) {
        if (!stack.isEmpty() && stack.getItem().getRegistryName() != null) {
            String regName = stack.getItem().getRegistryName().toString();
            if (regName.equals(ThickAir.MODID + ":respirator")) {
                if (entity.world.getTotalWorldTime() % (20 * 15) == 0) {
                    stack.damageItem(1, entity);
                }
                return true;
            }
        }
        return false;
    }

    int getAirAmount(EntityLivingBase entity) {
        switch (this) {
            case GREEN:
                return 4;
            case BLUE:
                return 0;
            case YELLOW: {
                if (entity.world.getTotalWorldTime() % 4 != 0) return 0;
                int respiration = EnchantmentHelper.getRespirationModifier(entity);
                return entity.getRNG().nextInt(respiration + 1) == 0 ? -1 : 0;
            }
            case RED: {
                int respiration = EnchantmentHelper.getRespirationModifier(entity);
                return entity.getRNG().nextInt(respiration + 1) == 0 ? -1 : 0;
            }
            default:
                return 0;
        }
    }

    public float getItemModelProperty() {
        return this.ordinal() / 10.0f;
    }
}
