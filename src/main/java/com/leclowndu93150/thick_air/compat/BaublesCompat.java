package com.leclowndu93150.thick_air.compat;

import baubles.api.BaublesApi;
import com.leclowndu93150.thick_air.Config;
import com.leclowndu93150.thick_air.ThickAir;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

public class BaublesCompat {

    private static Boolean baublesLoaded = null;

    public static boolean isBaublesLoaded() {
        if (baublesLoaded == null) {
            baublesLoaded = Loader.isModLoaded("baubles");
        }
        return baublesLoaded;
    }

    public static ItemStack getRespiratorFromBaubles(EntityLivingBase entity) {
        if (!Config.enableBaubles || !isBaublesLoaded()) return ItemStack.EMPTY;
        if (!(entity instanceof EntityPlayer)) return ItemStack.EMPTY;
        return getRespiratorFromBaublesInternal((EntityPlayer) entity);
    }

    private static ItemStack getRespiratorFromBaublesInternal(EntityPlayer player) {
        int slot = BaublesApi.isBaubleEquipped(player,
                Item.getByNameOrId(ThickAir.MODID + ":respirator"));
        if (slot >= 0) {
            return BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }
}
