package com.gugustus.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderUtil {

    public static void drawRoundedRect(int x, int y, int width, int height, int radius, int color) {
        Gui.drawRect(x + radius, y, x + width - radius, y + height, color);
        Gui.drawRect(x, y + radius, x + width, y + height - radius, color);
        drawCircle(x + radius, y + radius, radius, 0, 90, color);
        drawCircle(x + width - radius, y + radius, radius, 90, 180, color);
        drawCircle(x + width - radius, y + height - radius, radius, 180, 270, color);
        drawCircle(x + radius, y + height - radius, radius, 270, 360, color);
    }

    public static void drawCircle(int x, int y, int radius, int startAngle, int endAngle, int color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
        for (int i = startAngle; i <= endAngle; i++) {
            double rad = Math.toRadians(i);
            worldRenderer.pos(x + Math.sin(rad) * radius, y - Math.cos(rad) * radius, 0.0)
                    .color(red, green, blue, alpha).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawBorderedRoundedRect(int x, int y, int width, int height, int radius, int bgColor, int borderColor) {
        drawRoundedRect(x + 1, y + 1, width - 2, height - 2, radius - 1, bgColor);
        drawRoundedRect(x, y, width, height, radius, borderColor);
    }

    public static void drawGradientRect(int x, int y, int width, int height, int startColor, int endColor) {
        Gui.drawRect(x, y, x + width, y + height, startColor);
    }
}
