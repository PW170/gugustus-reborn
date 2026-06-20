package com.gugustus.client;

import com.gugustus.gui.ClickGUI;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

public class KeyInputHandler {

    private boolean wasRShiftDown = false;
    private boolean[] wasKeyDown = new boolean[Keyboard.KEYBOARD_SIZE];

    public KeyInputHandler() {
        Arrays.fill(wasKeyDown, false);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        int key = Keyboard.getEventKey();
        boolean pressed = Keyboard.getEventKeyState();

        if (key == Keyboard.KEY_NONE) return;

        // RShift -> ClickGUI
        if (key == Keyboard.KEY_RSHIFT) {
            if (pressed && !wasRShiftDown) {
                wasRShiftDown = true;
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.currentScreen == null) {
                    mc.displayGuiScreen(new ClickGUI());
                }
            }
            if (!pressed) {
                wasRShiftDown = false;
            }
            return;
        }

        // Module keybinds: toggle only on press edge (not release, not auto-repeat)
        if (pressed && !wasKeyDown[key] && Minecraft.getMinecraft().theWorld != null) {
            com.gugustus.Gugustus.moduleManager.onKey(key);
        }
        wasKeyDown[key] = pressed;
    }
}
