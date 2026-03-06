package com.leclowndu93150.thick_air.compat;

import com.unseen.nb.client.particles.ParticleSoul;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NetherBackportCompat {

    private static Boolean nbLoaded = null;

    public static boolean isLoaded() {
        if (nbLoaded == null) {
            nbLoaded = Loader.isModLoaded("nb");
        }
        return nbLoaded;
    }

    @SideOnly(Side.CLIENT)
    public static void spawnSoulParticles(EntityPlayer player) {
        if (!isLoaded()) return;
        spawnSoulParticlesInternal(player);
    }

    @SideOnly(Side.CLIENT)
    private static void spawnSoulParticlesInternal(EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        for (int i = 0; i < 10; i++) {
            double offsetX = player.world.rand.nextGaussian() * 0.5;
            double offsetY = player.world.rand.nextDouble() * 1.0;
            double offsetZ = player.world.rand.nextGaussian() * 0.5;
            mc.effectRenderer.addEffect(
                    new ParticleSoul(
                            player.world,
                            player.posX + offsetX,
                            player.posY + offsetY + 0.5,
                            player.posZ + offsetZ,
                            0, 0.1, 0
                    )
            );
        }
    }
}
