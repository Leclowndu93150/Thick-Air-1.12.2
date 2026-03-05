package com.leclowndu93150.thick_air.block;

import com.leclowndu93150.thick_air.api.AirQualityHelper;
import com.leclowndu93150.thick_air.api.AirQualityLevel;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class SafetyLanternBlock extends Block {
    public static final PropertyEnum<AirQualityLevel> AIR_QUALITY = PropertyEnum.create("air_quality", AirQualityLevel.class);
    public static final PropertyBool LOCKED = PropertyBool.create("locked");
    public static final PropertyBool HANGING = PropertyBool.create("hanging");

    private static final AxisAlignedBB STANDING_AABB = new AxisAlignedBB(0.3125, 0.0, 0.3125, 0.6875, 0.5625, 0.6875);
    private static final AxisAlignedBB HANGING_AABB = new AxisAlignedBB(0.3125, 0.125, 0.3125, 0.6875, 0.6875, 0.6875);

    public SafetyLanternBlock() {
        super(Material.IRON);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(AIR_QUALITY, AirQualityLevel.GREEN)
                .withProperty(LOCKED, false)
                .withProperty(HANGING, false));
        this.setHardness(3.5f);
        this.setResistance(3.5f);
        this.setTickRandomly(true);
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
        if (!worldIn.isRemote && !state.getValue(LOCKED)) {
            AirQualityLevel current = state.getValue(AIR_QUALITY);
            AirQualityLevel detected = AirQualityHelper.getAirQualityAtLocation(worldIn,
                    new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
            if (current != detected) {
                worldIn.setBlockState(pos, state.withProperty(AIR_QUALITY, detected), 3);
            }
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AIR_QUALITY, LOCKED, HANGING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        AirQualityLevel quality = AirQualityLevel.values()[meta & 3];
        boolean locked = (meta & 4) != 0;
        boolean hanging = (meta & 8) != 0;
        return this.getDefaultState()
                .withProperty(AIR_QUALITY, quality)
                .withProperty(LOCKED, locked)
                .withProperty(HANGING, hanging);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(AIR_QUALITY).ordinal();
        if (state.getValue(LOCKED)) meta |= 4;
        if (state.getValue(HANGING)) meta |= 8;
        return meta;
    }

    @Override
    public int getLightValue(IBlockState state) {
        return state.getValue(AIR_QUALITY).getLightLevel();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(HANGING) ? HANGING_AABB : STANDING_AABB;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                     EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemUsed = playerIn.getHeldItem(hand);
        AirQualityLevel lockedAirQuality = null;
        AirQualityLevel presentLockedAirQuality = null;
        boolean strippedDye = false;

        if (state.getValue(LOCKED)) {
            presentLockedAirQuality = state.getValue(AIR_QUALITY);
        }

        if (itemUsed.getItem() == Items.DYE) {
            int dmg = itemUsed.getMetadata();
            if (dmg == 2 && presentLockedAirQuality != AirQualityLevel.GREEN) {
                lockedAirQuality = AirQualityLevel.GREEN;
            } else if (dmg == 4 && presentLockedAirQuality != AirQualityLevel.BLUE) {
                lockedAirQuality = AirQualityLevel.BLUE;
            } else if (dmg == 11 && presentLockedAirQuality != AirQualityLevel.YELLOW) {
                lockedAirQuality = AirQualityLevel.YELLOW;
            } else if (dmg == 1 && presentLockedAirQuality != AirQualityLevel.RED) {
                lockedAirQuality = AirQualityLevel.RED;
            }
        } else if (itemUsed.getItem() instanceof ItemAxe && state.getValue(LOCKED)) {
            strippedDye = true;
        }

        boolean didAnything = false;
        IBlockState newBs = state;

        if (lockedAirQuality != null) {
            newBs = newBs.withProperty(AIR_QUALITY, lockedAirQuality).withProperty(LOCKED, true);
            if (!playerIn.capabilities.isCreativeMode) {
                itemUsed.shrink(1);
            }
            didAnything = true;
        } else if (strippedDye) {
            worldIn.playSound(playerIn, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);
            itemUsed.damageItem(1, playerIn);
            playerIn.swingArm(hand);
            newBs = newBs.withProperty(LOCKED, false);
            AirQualityLevel detected = AirQualityHelper.getAirQualityAtLocation(worldIn,
                    new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
            newBs = newBs.withProperty(AIR_QUALITY, detected);
            didAnything = true;
        }

        if (didAnything) {
            worldIn.setBlockState(pos, newBs);
            return true;
        }
        return false;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        worldIn.scheduleUpdate(pos, this, 1);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote && !state.getValue(LOCKED)) {
            AirQualityLevel current = state.getValue(AIR_QUALITY);
            AirQualityLevel detected = AirQualityHelper.getAirQualityAtLocation(worldIn,
                    new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
            if (current != detected) {
                worldIn.setBlockState(pos, state.withProperty(AIR_QUALITY, detected), 3);
            }
        }
        worldIn.scheduleUpdate(pos, this, 20);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        return blockState.getValue(AIR_QUALITY).getOutputSignal();
    }

}
