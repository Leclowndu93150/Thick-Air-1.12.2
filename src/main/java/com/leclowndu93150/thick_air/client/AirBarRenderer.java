package com.leclowndu93150.thick_air.client;

import com.leclowndu93150.thick_air.ModRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class AirBarRenderer extends Gui {

    private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");
    private static final ResourceLocation AIR_EMPTY_TEXTURE = new ResourceLocation("thick_air", "textures/gui/air_empty.png");

    private boolean wasUnderwater = false;
    private int waterExitCooldown = 0;
    private int tickCount = 0;
    private int previousAir = 300;
    private final Random random = new Random();

    @SubscribeEvent
    public void onRenderOverlayPost(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.AIR) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null || player.isCreative() || player.isSpectator()) return;

        tickCount++;

        boolean underwater = player.isInsideOfMaterial(Material.WATER);

        if (underwater) {
            wasUnderwater = true;
            int air = ClientAirData.air;
            renderEmptyDots(mc, player, air, 300, event.getResolution(), true);
            handleBubblePopSound(player, air, 300);
            return;
        }

        if (wasUnderwater) {
            wasUnderwater = false;
            waterExitCooldown = 2;
        }
        if (waterExitCooldown > 0) {
            waterExitCooldown--;
            return;
        }

        int air = ClientAirData.air;
        if (air >= 300) {
            previousAir = 300;
            return;
        }

        ScaledResolution res = event.getResolution();
        int width = res.getScaledWidth();
        int height = res.getScaledHeight();

        int xRight = width / 2 + 91;
        int top = height - GuiIngameForge.right_height;

        int fullBubbles = MathHelper.ceil((float) ((air - 2) * 10) / 300);
        int poppingBubble = MathHelper.ceil((float) (air * 10) / 300);
        boolean isPopping = fullBubbles != poppingBubble;

        int emptyDelayOffset = air > 0 ? 1 : 0;
        int emptyBubbles = 10 - MathHelper.ceil((float) ((air + emptyDelayOffset) * 10) / 300);

        handleBubblePopSound(player, air, 300);

        GlStateManager.enableBlend();

        mc.getTextureManager().bindTexture(ICONS);
        for (int i = 0; i < 10; i++) {
            int x = xRight - i * 8 - 9;
            int idx = i + 1;
            if (idx <= fullBubbles) {
                drawTexturedModalRect(x, top, 16, 18, 9, 9);
            } else if (isPopping && idx == poppingBubble) {
                drawTexturedModalRect(x, top, 25, 18, 9, 9);
            }
        }

        for (int i = 0; i < 10; i++) {
            int idx = i + 1;
            if (idx > fullBubbles && !(isPopping && idx == poppingBubble) && idx > 10 - emptyBubbles) {
                int x = xRight - i * 8 - 9;
                int wobble = emptyBubbles == 10 && tickCount % 2 == 0 ? random.nextInt(2) : 0;
                drawSprite(mc, x, top + wobble);
            }
        }

        GuiIngameForge.right_height += 10;
        GlStateManager.disableBlend();
    }

    private void renderEmptyDots(Minecraft mc, EntityPlayer player, int air, int maxAir, ScaledResolution res, boolean vanillaAlreadyRendered) {
        if (air >= maxAir) {
            previousAir = maxAir;
            return;
        }

        int width = res.getScaledWidth();
        int height = res.getScaledHeight();

        int xRight = width / 2 + 91;
        int top = vanillaAlreadyRendered
                ? height - GuiIngameForge.right_height + 10
                : height - GuiIngameForge.right_height;

        int fullBubbles = MathHelper.ceil((float) ((air - 2) * 10) / maxAir);
        int poppingBubble = MathHelper.ceil((float) (air * 10) / maxAir);
        boolean isPopping = fullBubbles != poppingBubble;

        int emptyDelayOffset = air > 0 ? 1 : 0;
        int emptyBubbles = 10 - MathHelper.ceil((float) ((air + emptyDelayOffset) * 10) / maxAir);
        if (emptyBubbles <= 0) return;

        GlStateManager.enableBlend();

        for (int i = 0; i < 10; i++) {
            int idx = i + 1;
            if (idx > fullBubbles && !(isPopping && idx == poppingBubble) && idx > 10 - emptyBubbles) {
                int x = xRight - i * 8 - 9;
                int wobble = emptyBubbles == 10 && tickCount % 2 == 0 ? random.nextInt(2) : 0;
                drawSprite(mc, x, top + wobble);
            }
        }

        GlStateManager.disableBlend();
    }

    private void handleBubblePopSound(EntityPlayer player, int air, int maxAir) {
        if (air >= previousAir) {
            previousAir = air;
            return;
        }

        int oldFullBubbles = MathHelper.ceil((float) ((previousAir - 2) * 10) / maxAir);
        int newFullBubbles = MathHelper.ceil((float) ((air - 2) * 10) / maxAir);

        if (newFullBubbles < oldFullBubbles) {
            int emptyBubbles = 10 - MathHelper.ceil((float) (air * 10) / maxAir);
            for (int bubble = oldFullBubbles; bubble > newFullBubbles; bubble--) {
                float volume = 0.5F + 0.1F * Math.max(0, emptyBubbles - 3 + 1);
                float pitch = 1.0F + 0.1F * Math.max(0, emptyBubbles - 5 + 1);
                player.playSound(ModRegistry.BUBBLE_POP, volume, pitch);
            }
        }

        previousAir = air;
    }

    private void drawSprite(Minecraft mc, int x, int y) {
        mc.getTextureManager().bindTexture(AIR_EMPTY_TEXTURE);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + 9, 0).tex(0, 1).endVertex();
        buffer.pos(x + 9, y + 9, 0).tex(1, 1).endVertex();
        buffer.pos(x + 9, y, 0).tex(1, 0).endVertex();
        buffer.pos(x, y, 0).tex(0, 0).endVertex();
        tessellator.draw();
    }
}
