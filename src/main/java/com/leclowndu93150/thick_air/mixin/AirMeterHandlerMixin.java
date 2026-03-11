package com.leclowndu93150.thick_air.mixin;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.fuzs.aquaacrobatics.client.handler.AirMeterHandler", remap = false)
public class AirMeterHandlerMixin {

    @Inject(method = "onRenderGameOverlay", at = @At("HEAD"), cancellable = true)
    private void thickair$cancelAirBar(RenderGameOverlayEvent.Pre evt, CallbackInfo ci) {
        if (evt.getType() == RenderGameOverlayEvent.ElementType.AIR) {
            ci.cancel();
        }
    }
}
