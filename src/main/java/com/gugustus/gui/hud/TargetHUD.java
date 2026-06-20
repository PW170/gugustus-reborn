package com.gugustus.gui.hud;

import com.gugustus.Gugustus;
import com.gugustus.module.visual.Interface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TargetHUD {

    private Minecraft mc = Minecraft.getMinecraft();

    public static EntityLivingBase currentTarget = null;

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Interface iface = Gugustus.moduleManager.getModule("Interface") instanceof Interface ? (Interface) Gugustus.moduleManager.getModule("Interface") : null;
        if (iface != null && iface.isEnabled() && iface.targetHud.getValue()) return;

        if (currentTarget == null || currentTarget.isDead) return;

        ScaledResolution sr = new ScaledResolution(mc);
        FontRenderer fr = mc.fontRendererObj;

        int x = sr.getScaledWidth() / 2 - 75;
        int y = sr.getScaledHeight() / 2 + 15;

        Gui.drawRect(x, y, x + 150, y + 40, 0x992C2C2E);
        Gui.drawRect(x, y, x + 1, y + 40, 0xFFF57C00);

        String name = currentTarget.getName();
        float health = currentTarget.getHealth();
        float maxHealth = currentTarget.getMaxHealth();
        double distance = mc.thePlayer.getDistanceToEntity(currentTarget);

        fr.drawString(name, x + 30, y + 4, 0xFFFFFFFF);
        String hpStr = String.format("%.1f / %.1f", health, maxHealth);
        fr.drawString(hpStr, x + 30, y + 14, health > 10 ? 0xFF55FF55 : 0xFFFF5555);
        fr.drawString("Dist: " + String.format("%.1f", distance), x + 30, y + 24, 0xFFB0B0B0);

        if (currentTarget instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) currentTarget;
            ItemStack weapon = player.getHeldItem();
            if (weapon != null) {
                fr.drawString(weapon.getDisplayName(), x + 120 - fr.getStringWidth(weapon.getDisplayName()) / 2, y + 30, 0xFFB0B0B0);
            }
        }
    }
}
