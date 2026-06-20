package com.gugustus.module.combat;

import com.gugustus.module.Category;
import com.gugustus.module.Module;
import com.gugustus.settings.BooleanSetting;
import com.gugustus.settings.NumberSetting;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.Random;

public class Autoclicker extends Module {

    private NumberSetting minCPS;
    private NumberSetting maxCPS;
    private NumberSetting jitter;
    private BooleanSetting blockhit;
    private NumberSetting blockhitEvery;
    private BooleanSetting onlySwords;

    private Random random = new Random();
    private long lastClickTime = 0;
    private long lastBlockhitTime = 0;
    private int clicksSinceBlockhit = 0;

    public Autoclicker() {
        super("Autoclicker", Category.COMBAT, 0);
        minCPS = new NumberSetting("Min CPS", 12, 1, 30, 1);
        maxCPS = new NumberSetting("Max CPS", 20, 1, 30, 1);
        jitter = new NumberSetting("Jitter", 0, 0, 100, 1);
        blockhit = new BooleanSetting("Blockhit", false);
        blockhitEvery = new NumberSetting("Blockhit every N", 4, 1, 20, 1);
        onlySwords = new BooleanSetting("Only swords", true);
        addSetting(minCPS);
        addSetting(maxCPS);
        addSetting(jitter);
        addSetting(blockhit);
        addSetting(blockhitEvery);
        addSetting(onlySwords);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (onlySwords.getValue() && !(mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) {
            return;
        }

        if (!Mouse.isButtonDown(0)) {
            lastClickTime = 0;
            return;
        }

        if (mc.currentScreen != null) return;

        double cps = minCPS.getValue() + random.nextDouble() * (maxCPS.getValue() - minCPS.getValue());
        long delay = (long) (1000.0 / cps);
        long now = System.currentTimeMillis();

        if (now - lastClickTime >= delay) {
            lastClickTime = now;
            mc.thePlayer.swingItem();
            if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null) {
                mc.playerController.attackEntity(mc.thePlayer, mc.objectMouseOver.entityHit);
            }

            clicksSinceBlockhit++;

            if (blockhit.getValue() && clicksSinceBlockhit >= blockhitEvery.getValueInt()) {
                clicksSinceBlockhit = 0;
                long now2 = System.currentTimeMillis();
                if (now2 - lastBlockhitTime > 500) {
                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                    lastBlockhitTime = now2;
                }
            }
        }
    }
}
