package com.leclowndu93150.thick_air.item;

import com.leclowndu93150.thick_air.api.AirQualityHelper;
import com.leclowndu93150.thick_air.api.AirQualityLevel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class AirBladderItem extends Item {

    public AirBladderItem() {
        super();
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        AirQualityLevel airQualityLevel = AirQualityHelper.getAirQualityAtLocation(playerIn);
        ItemStack itemInHand = playerIn.getHeldItem(handIn);

        boolean canRefill = airQualityLevel.canRefillAir && itemInHand.isItemDamaged();
        boolean canProvideAir = !airQualityLevel.canRefillAir
                && itemInHand.getItemDamage() < itemInHand.getMaxDamage()
                && playerIn.getAir() < 300;

        if (canRefill || canProvideAir) {
            playerIn.setActiveHand(handIn);
            return new ActionResult<>(EnumActionResult.SUCCESS, itemInHand);
        }
        return new ActionResult<>(EnumActionResult.PASS, itemInHand);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {
        boolean stopUsing = true;
        AirQualityLevel airQualityLevel = AirQualityHelper.getAirQualityAtLocation(entity);

        if (airQualityLevel.canRefillAir) {
            if (stack.isItemDamaged()) {
                stack.setItemDamage(Math.max(0, stack.getItemDamage() - 4));
                stopUsing = false;
            }
        } else if (stack.getItemDamage() < stack.getMaxDamage()) {
            int i = 4;
            while (i-- > 0 && entity.getAir() < 300 && stack.getItemDamage() < stack.getMaxDamage()) {
                entity.setAir(entity.getAir() + 1);
                stack.setItemDamage(stack.getItemDamage() + 1);
                stopUsing = false;
            }
        }

        if (!stopUsing) {
            int remaining = getMaxItemUseDuration(stack) - count;
            if (remaining >= 7 && remaining % 4 == 0) {
                entity.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 0.5f, entity.world.rand.nextFloat() * 0.1f + 0.9f);
            }
        } else {
            entity.stopActiveHand();
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLiving;
            if (player.getAir() >= 300) {
                if (!AirQualityHelper.getAirQualityAtLocation(entityLiving).canRefillAir) {
                    player.getCooldownTracker().setCooldown(this, 150);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        int remaining = stack.getMaxDamage() - stack.getItemDamage();
        tooltip.add(TextFormatting.GRAY + "Air: " + remaining + "/" + stack.getMaxDamage());
        tooltip.add(TextFormatting.BLUE + "Use in green air to refill");
        tooltip.add(TextFormatting.YELLOW + "Use in bad air to breathe");
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }
}
