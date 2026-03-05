package com.leclowndu93150.thick_air.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class SoulfireBottleItem extends Item {

    public SoulfireBottleItem() {
        super();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLiving;

            player.setAir(300);

            worldIn.playSound(null, player.posX, player.posY, player.posZ,
                    SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);

            if (worldIn instanceof WorldServer) {
                WorldServer serverWorld = (WorldServer) worldIn;
                for (int i = 0; i < 10; i++) {
                    double offsetX = worldIn.rand.nextGaussian() * 0.5;
                    double offsetY = worldIn.rand.nextDouble() * 1.0;
                    double offsetZ = worldIn.rand.nextGaussian() * 0.5;
                    serverWorld.spawnParticle(EnumParticleTypes.SPELL_WITCH,
                            player.posX + offsetX,
                            player.posY + offsetY + 0.5,
                            player.posZ + offsetZ,
                            1, 0, 0.1, 0, 0.05);
                }
            }

            player.addStat(StatList.getObjectUseStats(this));

            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }

        return stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.DRINK;
    }
}
