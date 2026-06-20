package com.gugustus.module.player;

import com.gugustus.module.Category;
import com.gugustus.module.Module;
import com.gugustus.settings.BooleanSetting;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class InvMove extends Module {

    private BooleanSetting sprint;

    public InvMove() {
        super("InvMove", Category.PLAYER, 0);
        sprint = new BooleanSetting("Sprint", true);
        addSetting(sprint);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (mc.currentScreen == null) return;
        if (mc.currentScreen instanceof GuiChat) return;
        if (mc.currentScreen instanceof GuiEditSign) return;

        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(),
                org.lwjgl.input.Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(),
                org.lwjgl.input.Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()));
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(),
                org.lwjgl.input.Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()));
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(),
                org.lwjgl.input.Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()));
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(),
                org.lwjgl.input.Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()));

        if (sprint.getValue()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(),
                    org.lwjgl.input.Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode()));
        }
    }
}
