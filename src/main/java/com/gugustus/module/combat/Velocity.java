package com.gugustus.module.combat;

import com.gugustus.module.Category;
import com.gugustus.module.Module;
import com.gugustus.settings.BooleanSetting;
import com.gugustus.settings.ModeSetting;
import com.gugustus.settings.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.lang.reflect.Field;

public class Velocity extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", new String[]{
            "Hypixel Modern", "Simple", "JumpReset", "Glide"
    }, "Hypixel Modern");
    private NumberSetting horizontal;
    private NumberSetting vertical;
    private BooleanSetting onlyGrounded;

    private int timerTicks = 0;

    public Velocity() {
        super("Velocity", Category.COMBAT, 0);
        horizontal = new NumberSetting("Horizontal", 80, 0, 100, 1);
        vertical = new NumberSetting("Vertical", 80, 0, 100, 1);
        onlyGrounded = new BooleanSetting("Only Grounded", true);
        addSetting(mode);
        addSetting(horizontal);
        addSetting(vertical);
        addSetting(onlyGrounded);
    }

    @Override
    public void onDisable() {
        setTimerSpeed(1.0f);
        timerTicks = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null) return;
        if (!mode.getValue().equals("Hypixel Modern")) return;

        float currentSpeed = getTimerSpeed();
        if (timerTicks > 0 && currentSpeed <= 1.0f) {
            float speed = 0.8f + (0.2f * (20 - timerTicks) / 20);
            setTimerSpeed(Math.min(speed, 1.0f));
            --timerTicks;
        } else if (currentSpeed < 1.0f) {
            setTimerSpeed(1.0f);
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.entity != mc.thePlayer) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (mode.getValue().equals("Hypixel Modern")) {
            if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.maxHurtTime > 0) {
                if ((mc.thePlayer.onGround || mc.thePlayer.fallDistance < 0.5) && timerTicks <= 0) {
                    timerTicks = 20;
                    mc.thePlayer.motionX *= 0.1;
                    mc.thePlayer.motionZ *= 0.1;
                    setTimerSpeed(0.8f);
                }
            }
            return;
        }

        if (onlyGrounded.getValue() && !mc.thePlayer.onGround) return;

        if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.maxHurtTime > 0) {
            switch (mode.getValue().toLowerCase()) {
                case "simple":
                    if (mc.thePlayer.motionX != 0 || mc.thePlayer.motionZ != 0) {
                        mc.thePlayer.motionX *= (1.0 - horizontal.getValue() / 100.0);
                        mc.thePlayer.motionZ *= (1.0 - horizontal.getValue() / 100.0);
                    }
                    mc.thePlayer.motionY *= (1.0 - vertical.getValue() / 100.0);
                    break;
                case "reset":
                    mc.thePlayer.motionX = 0;
                    mc.thePlayer.motionY = 0;
                    mc.thePlayer.motionZ = 0;
                    break;
                case "jumpreset":
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = 0.42;
                    }
                    mc.thePlayer.motionX *= (1.0 - horizontal.getValue() / 100.0);
                    mc.thePlayer.motionZ *= (1.0 - horizontal.getValue() / 100.0);
                    break;
                case "glide":
                    mc.thePlayer.motionY *= (1.0 - vertical.getValue() / 100.0);
                    mc.thePlayer.motionX *= (1.0 - horizontal.getValue() / 100.0);
                    mc.thePlayer.motionZ *= (1.0 - horizontal.getValue() / 100.0);
                    if (mc.thePlayer.motionY < -0.15) {
                        mc.thePlayer.motionY = -0.15;
                    }
                    break;
            }
        }
    }

    private float getTimerSpeed() {
        try {
            Field timerField = Minecraft.class.getDeclaredField("timer");
            timerField.setAccessible(true);
            Timer timer = (Timer) timerField.get(mc);
            Field speedField = Timer.class.getDeclaredField("timerSpeed");
            speedField.setAccessible(true);
            return speedField.getFloat(timer);
        } catch (Exception e) {
            return 1.0f;
        }
    }

    private void setTimerSpeed(float speed) {
        try {
            Field timerField = Minecraft.class.getDeclaredField("timer");
            timerField.setAccessible(true);
            Timer timer = (Timer) timerField.get(mc);
            Field speedField = Timer.class.getDeclaredField("timerSpeed");
            speedField.setAccessible(true);
            speedField.setFloat(timer, speed);
        } catch (Exception ignored) {
        }
    }
}
