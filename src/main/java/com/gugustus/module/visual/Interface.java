package com.gugustus.module.visual;

import com.gugustus.Gugustus;
import com.gugustus.module.Category;
import com.gugustus.module.Module;
import com.gugustus.settings.BooleanSetting;
import com.gugustus.settings.NumberSetting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Interface extends Module {

    public final BooleanSetting watermark = new BooleanSetting("Watermark", true);
    public final BooleanSetting moduleList = new BooleanSetting("Module List", true);
    public final BooleanSetting armor = new BooleanSetting("Armor", true);
    public final BooleanSetting potions = new BooleanSetting("Potions", true);
    public final BooleanSetting targetHud = new BooleanSetting("Target HUD", true);
    public final BooleanSetting bps = new BooleanSetting("BPS", true);
    public final BooleanSetting keystrokes = new BooleanSetting("Keystrokes", true);
    public final BooleanSetting info = new BooleanSetting("Info", true);

    public final NumberSetting bgAlpha = new NumberSetting("BG Alpha", 140, 0, 255, 1);
    public final NumberSetting textHeight = new NumberSetting("Text Height", 0, 0, 10, 1);
    public final NumberSetting logoSize = new NumberSetting("Logo Size", 60, 20, 150, 1);

    private static final ResourceLocation LOGO = new ResourceLocation("gugustus", "textures/gui/logo.png");

    private long lastX, lastZ, lastTime;
    private double currentBPS;
    private final List<Long> leftClicks = new ArrayList<>();
    private final List<Long> rightClicks = new ArrayList<>();

    private boolean wPressed, aPressed, sPressed, dPressed, jumpPressed;
    private float keystrokeAnim = 0;

    public Interface() {
        super("Interface", Category.VISUAL, 0);
        addSetting(watermark);
        addSetting(moduleList);
        addSetting(armor);
        addSetting(potions);
        addSetting(targetHud);
        addSetting(bps);
        addSetting(keystrokes);
        addSetting(info);
        addSetting(bgAlpha);
        addSetting(textHeight);
    }

    @Override
    public void onEnable() {
        lastX = (long) (mc.thePlayer != null ? mc.thePlayer.posX : 0);
        lastZ = (long) (mc.thePlayer != null ? mc.thePlayer.posZ : 0);
        lastTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null) return;

        long now = System.currentTimeMillis();
        long dt = now - lastTime;
        if (dt > 50) {
            double dx = mc.thePlayer.posX - lastX;
            double dz = mc.thePlayer.posZ - lastZ;
            currentBPS = Math.sqrt(dx * dx + dz * dz) / (dt / 1000.0);
            lastX = (long) mc.thePlayer.posX;
            lastZ = (long) mc.thePlayer.posZ;
            lastTime = now;
        }

        wPressed = mc.thePlayer.movementInput.moveForward > 0;
        sPressed = mc.thePlayer.movementInput.moveForward < 0;
        aPressed = mc.thePlayer.movementInput.moveStrafe > 0;
        dPressed = mc.thePlayer.movementInput.moveStrafe < 0;
        jumpPressed = mc.thePlayer.movementInput.jump;

        leftClicks.removeIf(t -> t + 1000L < now);
        rightClicks.removeIf(t -> t + 1000L < now);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;

        ScaledResolution sr = new ScaledResolution(mc);
        int bg = (0x2C2C2E | ((int) bgAlpha.getValue() << 24));

        if (watermark.getValue()) drawWatermark(sr);
        if (moduleList.getValue()) drawModuleList(sr, bg);
        if (armor.getValue()) drawArmorHUD(sr);
        if (potions.getValue()) drawPotionHUD(sr);
        if (targetHud.getValue()) drawTargetHUD(sr, bg);
        if (bps.getValue()) drawBPS(sr, bg);
        if (keystrokes.getValue()) drawKeystrokes(sr, bg);
        if (info.getValue()) drawInfo(sr, bg);
    }

    private void drawWatermark(ScaledResolution sr) {
        int size = (int) logoSize.getValue();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(LOGO);
        Gui.drawModalRectWithCustomSizedTexture(5, 5, 0, 0, size, size, size, size);
        mc.fontRendererObj.drawStringWithShadow("FPS: " + mc.getDebugFPS(), 10 + size, 9, 0xFFB0B0B0);
    }

    private void drawModuleList(ScaledResolution sr, int bg) {
        List<Module> enabled = new ArrayList<>();
        for (Module m : Gugustus.moduleManager.getModules()) {
            if (m.isEnabled()) enabled.add(m);
        }
        enabled.sort(Comparator.comparingInt(m -> -mc.fontRendererObj.getStringWidth(m.getName())));

        int y = 5;
        for (Module m : enabled) {
            String text = m.getName();
            int tw = mc.fontRendererObj.getStringWidth(text);
            Gui.drawRect(sr.getScaledWidth() - tw - 8, y, sr.getScaledWidth(), y + mc.fontRendererObj.FONT_HEIGHT + 4, bg);
            Gui.drawRect(sr.getScaledWidth() - tw - 8, y, sr.getScaledWidth() - tw - 7, y + mc.fontRendererObj.FONT_HEIGHT + 4, 0xFFF57C00);
            mc.fontRendererObj.drawString(text, sr.getScaledWidth() - tw - 5, y + 2, 0xFFFFFFFF);
            y += mc.fontRendererObj.FONT_HEIGHT + 3 + (int) textHeight.getValue();
        }
    }

    private void drawArmorHUD(ScaledResolution sr) {
        List<ItemStack> armorStacks = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = mc.thePlayer.inventory.armorInventory[i];
            if (stack != null) armorStacks.add(stack);
        }
        if (armorStacks.isEmpty()) return;

        int startX = sr.getScaledWidth() / 2 - (armorStacks.size() * 18) / 2;
        int y = sr.getScaledHeight() - 60;

        GlStateManager.enableBlend();
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < armorStacks.size(); i++) {
            ItemStack stack = armorStacks.get(i);
            int x = startX + i * 18;
            mc.getRenderItem().renderItemIntoGUI(stack, x, y);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, stack, x, y, null);
            if (stack.isItemDamaged()) {
                String dur = (stack.getMaxDamage() - stack.getItemDamage()) + "";
                mc.fontRendererObj.drawStringWithShadow(dur, x + 9 - mc.fontRendererObj.getStringWidth(dur) / 2, y + 16, 0xFFFFFFFF);
            }
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableBlend();
    }

    private void drawPotionHUD(ScaledResolution sr) {
        int x = 5;
        int y = 30;
        for (PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            String name = potion.getName();
            int amplifier = effect.getAmplifier() + 1;
            String duration = Potion.getDurationString(effect);
            mc.fontRendererObj.drawStringWithShadow(name + " " + amplifier, x, y, potion.getLiquidColor());
            mc.fontRendererObj.drawStringWithShadow(duration, x, y + mc.fontRendererObj.FONT_HEIGHT, 0xFFB0B0B0);
            y += mc.fontRendererObj.FONT_HEIGHT * 2 + 2;
        }
    }

    private void drawTargetHUD(ScaledResolution sr, int bg) {
        if (com.gugustus.gui.hud.TargetHUD.currentTarget == null
                || com.gugustus.gui.hud.TargetHUD.currentTarget.isDead) return;

        EntityPlayer target = (EntityPlayer) com.gugustus.gui.hud.TargetHUD.currentTarget;
        int x = sr.getScaledWidth() / 2 - 75;
        int y = sr.getScaledHeight() / 2 + 15;

        Gui.drawRect(x, y, x + 150, y + 40, bg);
        Gui.drawRect(x, y, x + 1, y + 40, 0xFFF57C00);

        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        double dist = mc.thePlayer.getDistanceToEntity(target);

        mc.fontRendererObj.drawString(target.getName(), x + 5, y + 3, 0xFFFFFFFF);
        mc.fontRendererObj.drawString(String.format("%.1f / %.1f", health, maxHealth), x + 5, y + 14,
                health > 10 ? 0xFF55FF55 : 0xFFFF5555);
        mc.fontRendererObj.drawString("Dist: " + String.format("%.1f", dist), x + 5, y + 25, 0xFFB0B0B0);

        ItemStack weapon = target.getHeldItem();
        if (weapon != null) {
            mc.fontRendererObj.drawString(weapon.getDisplayName(), x + 155 - mc.fontRendererObj.getStringWidth(weapon.getDisplayName()), y + 3, 0xFFB0B0B0);
        }
    }

    private void drawBPS(ScaledResolution sr, int bg) {
        String text = String.format("%.1f bps", currentBPS);
        int tw = mc.fontRendererObj.getStringWidth(text);
        int x = sr.getScaledWidth() / 2 - tw / 2;
        int y = sr.getScaledHeight() / 2 + 50;
        Gui.drawRect(x - 4, y - 2, x + tw + 4, y + mc.fontRendererObj.FONT_HEIGHT + 2, bg);
        mc.fontRendererObj.drawString(text, x, y, 0xFFFFFFFF);
    }

    private void drawKeystrokes(ScaledResolution sr, int bg) {
        int keySize = 18;
        int gap = 2;
        int startX = (int) (sr.getScaledWidth() / 2f - keySize * 1.5f - gap);
        int startY = sr.getScaledHeight() - 120;

        drawKey(startX + keySize + gap, startY, keySize, "W", wPressed, bg);
        drawKey(startX, startY + keySize + gap, keySize, "A", aPressed, bg);
        drawKey(startX + keySize + gap, startY + keySize + gap, keySize, "S", sPressed, bg);
        drawKey(startX + (keySize + gap) * 2, startY + keySize + gap, keySize, "D", dPressed, bg);
        drawKey(startX + keySize + gap, startY + (keySize + gap) * 2, keySize + 8, "SPACE", jumpPressed, bg);
    }

    private void drawKey(int x, int y, int w, String label, boolean pressed, int bg) {
        int color = pressed ? 0xFFF57C00 : bg;
        int textColor = pressed ? 0xFF000000 : 0xFFFFFFFF;
        Gui.drawRect(x, y, x + w, y + 18, color);
        int tw = mc.fontRendererObj.getStringWidth(label);
        mc.fontRendererObj.drawString(label, x + (w - tw) / 2, y + 5, textColor);
    }

    private void drawInfo(ScaledResolution sr, int bg) {
        int cpsL = leftClicks.size();
        int cpsR = rightClicks.size();
        String cpsText = cpsL + " | " + cpsR + " CPS";
        String fpsText = mc.getDebugFPS() + " FPS";
        String nameText = mc.thePlayer.getName();

        int x = 5;
        int y = sr.getScaledHeight() - 50;

        Gui.drawRect(x, y, x + mc.fontRendererObj.getStringWidth(cpsText) + 6, y + mc.fontRendererObj.FONT_HEIGHT + 4, bg);
        mc.fontRendererObj.drawString(cpsText, x + 3, y + 2, 0xFFFFFFFF);

        y += mc.fontRendererObj.FONT_HEIGHT + 6;
        Gui.drawRect(x, y, x + mc.fontRendererObj.getStringWidth(fpsText) + 6, y + mc.fontRendererObj.FONT_HEIGHT + 4, bg);
        mc.fontRendererObj.drawString(fpsText, x + 3, y + 2, 0xFFFFFFFF);

        y += mc.fontRendererObj.FONT_HEIGHT + 6;
        Gui.drawRect(x, y, x + mc.fontRendererObj.getStringWidth(nameText) + 6, y + mc.fontRendererObj.FONT_HEIGHT + 4, bg);
        mc.fontRendererObj.drawString(nameText, x + 3, y + 2, 0xFFF57C00);
    }

    @SubscribeEvent
    public void onTickCPS(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;
        if (mc.objectMouseOver != null && mc.gameSettings.keyBindAttack.isKeyDown()) {
            leftClicks.add(System.currentTimeMillis());
        }
        if (mc.objectMouseOver != null && mc.gameSettings.keyBindUseItem.isKeyDown()) {
            rightClicks.add(System.currentTimeMillis());
        }
    }
}
