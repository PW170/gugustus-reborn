package com.gugustus.gui.hud;

import com.gugustus.Gugustus;
import com.gugustus.module.visual.Interface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Watermark {

    private Minecraft mc = Minecraft.getMinecraft();
    private static final ResourceLocation LOGO = new ResourceLocation("gugustus", "textures/gui/logo.png");

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Interface iface = Gugustus.moduleManager.getModule("Interface") instanceof Interface ? (Interface) Gugustus.moduleManager.getModule("Interface") : null;
        if (iface != null && iface.isEnabled() && iface.watermark.getValue()) return;

        int size = 60;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(LOGO);
        Gui.drawModalRectWithCustomSizedTexture(5, 5, 0, 0, size, size, size, size);
        mc.fontRendererObj.drawStringWithShadow("FPS: " + mc.getDebugFPS(), 10 + size, 9, 0xFFB0B0B0);
    }
}
