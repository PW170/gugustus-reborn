package com.gugustus.gui;

import net.minecraft.client.renderer.GlStateManager;

import java.util.Random;

public class GuiMainMenu extends net.minecraft.client.gui.GuiMainMenu {

    private static final int STAR_COUNT = 150;
    private final Star[] stars = new Star[STAR_COUNT];
    private final Random random = new Random();
    private long lastUpdate = System.currentTimeMillis();

    public GuiMainMenu() {
        for (int i = 0; i < STAR_COUNT; i++) {
            stars[i] = new Star();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGalaxyBackground();
        drawTitle();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawGalaxyBackground() {
        long now = System.currentTimeMillis();
        float delta = (now - lastUpdate) / 1000.0f;
        lastUpdate = now;
        if (delta > 0.05f) delta = 0.05f;

        drawRect(0, 0, width, height, 0xFF0B0B1A);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(770, 771);

        for (Star star : stars) {
            star.update(delta, width, height);

            float alpha = star.alpha * (0.6f + 0.4f * (float) Math.sin(now / 2000.0 * star.speed + star.seed));
            float size = star.size * (0.8f + 0.2f * (float) Math.sin(now / 1500.0 * star.speed + star.seed + 1.0));

            GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
            drawRect((int) star.x, (int) star.y, (int) (star.x + size), (int) (star.y + size), 0xFFFFFFFF);
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawTitle() {
        String title = "Gugustus Reborn";
        String subtitle = "1.8.9";

        GlStateManager.pushMatrix();
        float scale = 2.5f;
        float subScale = 1.2f;

        int titleWidth = mc.fontRendererObj.getStringWidth(title);
        int subWidth = mc.fontRendererObj.getStringWidth(subtitle);

        float titleScaledWidth = titleWidth * scale;
        float subScaledWidth = subWidth * subScale;

        float titleX = (width - titleScaledWidth) / 2.0f;
        float subX = (width - subScaledWidth) / 2.0f;

        int titleY = 30;
        int subY = titleY + (int) (mc.fontRendererObj.FONT_HEIGHT * scale) + 8;

        GlStateManager.scale(scale, scale, 1.0f);

        mc.fontRendererObj.drawString(title,
                titleX / scale + 1.0f,
                titleY / scale + 1.0f,
                0xBB000000, false);
        mc.fontRendererObj.drawString(title,
                titleX / scale,
                titleY / scale,
                0xFFFFFFFF, false);

        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.scale(subScale, subScale, 1.0f);

        mc.fontRendererObj.drawString(subtitle,
                subX / subScale + 1.0f,
                subY / subScale + 1.0f,
                0x88000000, false);
        mc.fontRendererObj.drawString(subtitle,
                subX / subScale,
                subY / subScale,
                0xFFAAAAAA, false);

        GlStateManager.popMatrix();
    }

    private class Star {
        float x, y;
        float speed;
        float size;
        float alpha;
        double seed;

        Star() {
            x = random.nextFloat() * 2000 - 500;
            y = random.nextFloat() * 1500 - 500;
            speed = 0.3f + random.nextFloat() * 0.7f;
            size = 1.0f + random.nextFloat() * 2.0f;
            alpha = 0.3f + random.nextFloat() * 0.7f;
            seed = random.nextDouble() * 100.0;
        }

        void update(float delta, int screenW, int screenH) {
            y += speed * 60.0f * delta;

            if (y > screenH + 10) {
                y = -10;
                x = random.nextFloat() * screenW;
                speed = 0.3f + random.nextFloat() * 0.7f;
                size = 1.0f + random.nextFloat() * 2.0f;
                alpha = 0.3f + random.nextFloat() * 0.7f;
            }
        }
    }
}
