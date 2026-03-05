package com.leclowndu93150.thick_air.mixin;

import com.leclowndu93150.thick_air.handler.AirBubbleTracker;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldServerMixin {

    @Inject(method = "notifyBlockUpdate", at = @At("HEAD"))
    private void thickair$onBlockUpdate(BlockPos pos, IBlockState oldState, IBlockState newState, int flags, CallbackInfo ci) {
        World self = (World) (Object) this;
        if (!self.isRemote && oldState.getBlock() != newState.getBlock()) {
            AirBubbleTracker.onBlockStateChange((WorldServer) self, pos, oldState, newState);
        }
    }
}
