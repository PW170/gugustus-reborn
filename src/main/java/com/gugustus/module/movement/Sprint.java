package com.gugustus.module.movement;

import com.gugustus.module.Category;
import com.gugustus.module.Module;
import com.gugustus.settings.BooleanSetting;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Sprint extends Module {

    private final BooleanSetting fovFix = new BooleanSetting("FOV Fix", true);
    private boolean wasSprinting = false;

    public Sprint() {
        super("Sprint", Category.MOVEMENT, 0);
        addSetting(fovFix);
    }

    @Override
    public void onDisable() {
        wasSprinting = false;
        if (mc.thePlayer != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null) return;

        if (event.phase == TickEvent.Phase.START) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        } else {
            wasSprinting = mc.thePlayer.isSprinting();
        }
    }
}
