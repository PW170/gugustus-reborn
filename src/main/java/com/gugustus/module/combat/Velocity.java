package com.gugustus.module.combat;

import com.gugustus.module.Category;
import com.gugustus.module.Module;
import com.gugustus.settings.BooleanSetting;
import com.gugustus.settings.ModeSetting;
import com.gugustus.settings.NumberSetting;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Velocity extends Module {

    private ModeSetting mode;
    private NumberSetting horizontal;
    private NumberSetting vertical;
    private BooleanSetting onlyGrounded;

    public Velocity() {
        super("Velocity", Category.COMBAT, 0);
        mode = new ModeSetting("Mode", new String[]{"Simple", "JumpReset", "Glide"}, "Simple");
        horizontal = new NumberSetting("Horizontal", 80, 0, 100, 1);
        vertical = new NumberSetting("Vertical", 80, 0, 100, 1);
        onlyGrounded = new BooleanSetting("Only Grounded", true);
        addSetting(mode);
        addSetting(horizontal);
        addSetting(vertical);
        addSetting(onlyGrounded);
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.entity != mc.thePlayer) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

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
}
