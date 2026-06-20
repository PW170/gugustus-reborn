package com.gugustus.gui.hud;

import com.gugustus.Gugustus;
import com.gugustus.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArrayListHUD {

    private Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        ScaledResolution sr = new ScaledResolution(mc);
        FontRenderer fr = mc.fontRendererObj;
        List<Module> enabledModules = new ArrayList<>();
        for (Module m : Gugustus.moduleManager.getModules()) {
            if (m.isEnabled()) {
                enabledModules.add(m);
            }
        }
        enabledModules.sort(Comparator.comparingInt(m -> -fr.getStringWidth(m.getName())));

        int y = 5;
        for (Module m : enabledModules) {
            String text = m.getName();
            int textWidth = fr.getStringWidth(text);
            Gui.drawRect(sr.getScaledWidth() - textWidth - 8, y, sr.getScaledWidth(), y + fr.FONT_HEIGHT + 4, 0x662C2C2E);
            Gui.drawRect(sr.getScaledWidth() - textWidth - 8, y, sr.getScaledWidth() - textWidth - 7, y + fr.FONT_HEIGHT + 4, 0xFFF57C00);
            fr.drawString(text, sr.getScaledWidth() - textWidth - 5, y + 2, 0xFFFFFFFF);
            y += fr.FONT_HEIGHT + 3;
        }
    }
}
