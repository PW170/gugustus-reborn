package com.gugustus.gui.hud;

import com.gugustus.Gugustus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Watermark {

    private Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        ScaledResolution sr = new ScaledResolution(mc);
        FontRenderer fr = mc.fontRendererObj;

        String watermark = "Gugustus Reborn v" + Gugustus.VERSION;
        int fps = Minecraft.getDebugFPS();

        fr.drawString(watermark, 5, 5, 0xFFF57C00);
        fr.drawString("FPS: " + fps, 5, 17, 0xFFB0B0B0);
    }
}
