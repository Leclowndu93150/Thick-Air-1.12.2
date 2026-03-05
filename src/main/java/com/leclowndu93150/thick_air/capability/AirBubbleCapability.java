package com.leclowndu93150.thick_air.capability;

import com.leclowndu93150.thick_air.ThickAir;
import com.leclowndu93150.thick_air.api.AirQualityLevel;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class AirBubbleCapability {

    @CapabilityInject(IAirBubblePositions.class)
    public static Capability<IAirBubblePositions> AIR_BUBBLE_CAP = null;

    public static final ResourceLocation CAP_KEY = new ResourceLocation(ThickAir.MODID, "air_bubble_positions");

    public static void register() {
        CapabilityManager.INSTANCE.register(IAirBubblePositions.class, new Storage(), AirBubblePositionsImpl::new);
        MinecraftForge.EVENT_BUS.register(new AirBubbleCapability());
    }

    @SubscribeEvent
    public void onAttachChunkCapabilities(AttachCapabilitiesEvent<Chunk> event) {
        event.addCapability(CAP_KEY, new Provider());
    }

    public static class Storage implements Capability.IStorage<IAirBubblePositions> {
        @Nullable
        @Override
        public NBTBase writeNBT(Capability<IAirBubblePositions> capability, IAirBubblePositions instance, EnumFacing side) {
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagList list = new NBTTagList();
            for (Map.Entry<BlockPos, AirQualityLevel> entry : instance.getAirBubblePositions().entrySet()) {
                NBTTagCompound entryTag = new NBTTagCompound();
                entryTag.setLong("pos", entry.getKey().toLong());
                entryTag.setByte("quality", (byte) entry.getValue().ordinal());
                list.appendTag(entryTag);
            }
            tag.setTag("entries", list);
            tag.setInteger("skipCountLeft", instance.getSkipCountLeft());
            return tag;
        }

        @Override
        public void readNBT(Capability<IAirBubblePositions> capability, IAirBubblePositions instance, EnumFacing side, NBTBase nbt) {
            if (nbt instanceof NBTTagCompound) {
                NBTTagCompound tag = (NBTTagCompound) nbt;
                NBTTagList list = tag.getTagList("entries", 10);
                instance.getAirBubblePositions().clear();
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound entryTag = list.getCompoundTagAt(i);
                    BlockPos pos = BlockPos.fromLong(entryTag.getLong("pos"));
                    int qualityOrd = entryTag.getByte("quality");
                    if (qualityOrd >= 0 && qualityOrd < AirQualityLevel.values().length) {
                        instance.getAirBubblePositions().put(pos, AirQualityLevel.values()[qualityOrd]);
                    }
                }
                instance.setSkipCountLeft(tag.getInteger("skipCountLeft"));
            }
        }
    }

    public static class Provider implements ICapabilitySerializable<NBTBase> {
        private final IAirBubblePositions instance = new AirBubblePositionsImpl();

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == AIR_BUBBLE_CAP;
        }

        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == AIR_BUBBLE_CAP ? AIR_BUBBLE_CAP.cast(instance) : null;
        }

        @Override
        public NBTBase serializeNBT() {
            return AIR_BUBBLE_CAP.getStorage().writeNBT(AIR_BUBBLE_CAP, instance, null);
        }

        @Override
        public void deserializeNBT(NBTBase nbt) {
            AIR_BUBBLE_CAP.getStorage().readNBT(AIR_BUBBLE_CAP, instance, null, nbt);
        }
    }
}
