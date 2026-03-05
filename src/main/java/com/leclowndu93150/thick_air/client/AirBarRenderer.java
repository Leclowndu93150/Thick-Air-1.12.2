package com.leclowndu93150.thick_air.client;

import com.leclowndu93150.thick_air.api.AirQualityLevel;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AirBarRenderer extends Gui {

    private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");

    @SubscribeEvent
    public void onRenderOverlayPost(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.AIR) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null || player.isCreative() || player.isSpectator()) return;
        if (player.isInsideOfMaterial(Material.WATER)) return;

        int air = ClientAirData.air;
        if (air >= 300) return;

        AirQualityLevel quality = AirQualityLevel.values()[ClientAirData.qualityOrdinal];
        if (quality.canBreathe && air >= 300) return;

        ScaledResolution res = event.getResolution();
        int width = res.getScaledWidth();
        int height = res.getScaledHeight();

        mc.getTextureManager().bindTexture(ICONS);
        GlStateManager.enableBlend();

        int left = width / 2 + 91;
        int top = height - 49;

        int full = MathHelper.ceil((double) (air - 2) * 10.0 / 300.0);
        int partial = MathHelper.ceil((double) air * 10.0 / 300.0) - full;

        for (int i = 0; i < full + partial; i++) {
            drawTexturedModalRect(left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
        }

        GlStateManager.disableBlend();
    }
}
