package com.gugustus.module.movement;

import com.gugustus.module.Category;
import com.gugustus.module.Module;
import com.gugustus.settings.ModeSetting;
import com.gugustus.settings.NumberSetting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Flight extends Module {

    private NumberSetting speed;
    private ModeSetting mode;
    private NumberSetting vertical;

    public Flight() {
        super("Flight", Category.MOVEMENT, 0);
        speed = new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1);
        mode = new ModeSetting("Mode", new String[]{"Creative", "Vanilla"}, "Creative");
        vertical = new NumberSetting("Vertical", 0.1, 0.01, 1.0, 0.01);
        addSetting(speed);
        addSetting(mode);
        addSetting(vertical);
    }

    @Override
    public void onEnable() {
        if (mode.is("Creative")) {
            mc.thePlayer.capabilities.allowFlying = true;
            mc.thePlayer.capabilities.isFlying = true;
        }
    }

    @Override
    public void onDisable() {
        if (!mc.thePlayer.capabilities.isCreativeMode) {
            mc.thePlayer.capabilities.allowFlying = false;
        }
        mc.thePlayer.capabilities.isFlying = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (mode.is("Creative")) {
            mc.thePlayer.capabilities.setFlySpeed((float) speed.getValue() / 10.0f);
            mc.thePlayer.capabilities.isFlying = true;
        } else if (mode.is("Vanilla")) {
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.onGround = false;
            mc.thePlayer.fallDistance = 0;

            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.thePlayer.motionY += vertical.getValue();
            }
            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.thePlayer.motionY -= vertical.getValue();
            }

            double forward = mc.thePlayer.movementInput.moveForward;
            double strafe = mc.thePlayer.movementInput.moveStrafe;
            float yaw = mc.thePlayer.rotationYaw;

            if (forward == 0 && strafe == 0) return;

            double speedVal = speed.getValue();
            double rad = Math.toRadians(yaw);

            if (forward != 0) {
                mc.thePlayer.motionX += -Math.sin(rad) * forward * speedVal * 0.1;
                mc.thePlayer.motionZ += Math.cos(rad) * forward * speedVal * 0.1;
            }
            if (strafe != 0) {
                mc.thePlayer.motionX += -Math.sin(rad - Math.PI / 2) * strafe * speedVal * 0.1;
                mc.thePlayer.motionZ += Math.cos(rad - Math.PI / 2) * strafe * speedVal * 0.1;
            }
        }
    }
}
