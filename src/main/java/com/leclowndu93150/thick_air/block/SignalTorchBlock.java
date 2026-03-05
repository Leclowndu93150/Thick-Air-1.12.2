package com.leclowndu93150.thick_air.block;

import com.leclowndu93150.thick_air.Config;
import com.leclowndu93150.thick_air.ModRegistry;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class SignalTorchBlock extends BlockTorch {
    public static final PropertyBool LIT = PropertyBool.create("lit");

    public SignalTorchBlock() {
        super();
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.UP)
                .withProperty(LIT, true));
        this.setHardness(0.0f);
        this.setLightLevel(0.9375f);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, LIT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = super.getStateFromMeta(meta & 7);
        return state.withProperty(LIT, (meta & 8) == 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = super.getMetaFromState(state);
        if (!state.getValue(LIT)) {
            meta |= 8;
        }
        return meta;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                     EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!Config.enableSignalTorches) return false;

        boolean newLit = !state.getValue(LIT);
        worldIn.setBlockState(pos, state.withProperty(LIT, newLit));
        worldIn.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (stateIn.getValue(LIT)) {
            EnumFacing facing = stateIn.getValue(FACING);
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.7;
            double z = pos.getZ() + 0.5;

            if (facing != EnumFacing.UP) {
                EnumFacing opposite = facing.getOpposite();
                x += 0.27 * opposite.getXOffset();
                z += 0.27 * opposite.getZOffset();
                y += 0.22;
            }

            worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0, 0.0, 0.0);
            worldIn.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
            if (rand.nextInt(3) == 0) {
                worldIn.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, x, y + 0.5, z,
                        rand.nextGaussian() * 0.05, rand.nextDouble() * 0.05, rand.nextGaussian() * 0.05);
            }
        }
    }

    public static class EventHandler {
        @SubscribeEvent
        public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
            if (!Config.enableSignalTorches) return;

            World world = event.getWorld();
            BlockPos pos = event.getPos();
            IBlockState state = world.getBlockState(pos);
            EntityPlayer player = event.getEntityPlayer();

            if (!player.getHeldItem(event.getHand()).isEmpty()) return;

            if (state.getBlock() == Blocks.TORCH) {
                EnumFacing facing = state.getValue(BlockTorch.FACING);
                IBlockState signalState = ModRegistry.SIGNAL_TORCH_BLOCK.getDefaultState()
                        .withProperty(FACING, facing)
                        .withProperty(LIT, true);
                world.setBlockState(pos, signalState);
                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                event.setCanceled(true);
            }
        }
    }
}
