package com.gugustus.gui.components;

import com.gugustus.Gugustus;
import com.gugustus.module.Module;
import com.gugustus.settings.BooleanSetting;
import com.gugustus.settings.ModeSetting;
import com.gugustus.settings.NumberSetting;
import com.gugustus.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class ModuleCard {

    private Module module;
    private Panel panel;
    private boolean settingsExpanded = false;
    private List<SettingWidget> settingWidgets = new ArrayList<>();

    public static final int CARD_HEIGHT = 28;
    public static final int CARD_MARGIN = 3;

    public ModuleCard(Module module, Panel panel) {
        this.module = module;
        this.panel = panel;
        for (Setting s : module.getSettings()) {
            settingWidgets.add(new SettingWidget(s, this));
        }
    }

    public void draw(int x, int y, int width, int height, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;

        Gui.drawRect(x, y, x + width, y + height, module.isEnabled() ? 0x442C2C2E : 0x33151517);
        if (hovered) {
            Gui.drawRect(x, y, x + width, y + height, 0x22FFFFFF);
        }

        Minecraft.getMinecraft().fontRendererObj.drawString(module.getName(), x + 8, y + 8, 0xFFFFFFFF);

        // Toggle switch
        int toggleX = x + width - 40;
        int toggleY = y + 6;
        int toggleWidth = 30;
        int toggleHeight = 14;

        Gui.drawRect(toggleX, toggleY, toggleX + toggleWidth, toggleY + toggleHeight, 0x66333333);
        if (module.isEnabled()) {
            Gui.drawRect(toggleX + toggleWidth - 14, toggleY + 1, toggleX + toggleWidth - 1, toggleY + toggleHeight - 1, 0xFFF57C00);
        } else {
            Gui.drawRect(toggleX + 1, toggleY + 1, toggleX + 14, toggleY + toggleHeight - 1, 0xFF555555);
        }

        // Gear icon
        String gear = "\u2699";
        int gearColor = settingsExpanded ? 0xFFF57C00 : 0xFFB0B0B0;
        Minecraft.getMinecraft().fontRendererObj.drawString(gear, x + width - 12, y + 7, gearColor);

        // Settings dropdown
        if (settingsExpanded) {
            int settingsY = y + height + 2;
            int settingsWidth = width + 10;
            int dropdownHeight = calculateDropdownHeight();
            Gui.drawRect(x - 5, settingsY, x - 5 + settingsWidth, settingsY + dropdownHeight, 0xE02C2C2E);

            // Accent line at top of dropdown
            Gui.drawRect(x - 5, settingsY, x - 5 + settingsWidth, settingsY + 1, 0x44F57C00);

            int settingY = settingsY + 5;
            for (SettingWidget widget : settingWidgets) {
                widget.draw(x, settingY, settingsWidth - 10, mouseX, mouseY);
                settingY += widget.getHeight() + 4;
            }
        }
    }

    public void mouseClicked(int x, int y, int width, int height, int mouseX, int mouseY, int mouseButton) {
        int toggleX = x + width - 40;
        int toggleY = y + 6;
        int toggleWidth = 30;
        int toggleHeight = 14;

        // Toggle switch (any button)
        if (mouseX >= toggleX && mouseX <= toggleX + toggleWidth && mouseY >= toggleY && mouseY <= toggleY + toggleHeight) {
            module.toggle();
            Gugustus.configManager.saveCurrentConfig();
            return;
        }

        // Gear icon (any button)
        int gearX = x + width - 16;
        int gearY = y + 4;
        if (mouseX >= gearX && mouseX <= gearX + 12 && mouseY >= gearY && mouseY <= gearY + 16) {
            settingsExpanded = !settingsExpanded;
            return;
        }

        // Right-click on card body - toggle settings
        if (mouseButton == 1 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            settingsExpanded = !settingsExpanded;
            return;
        }

        // Left-click on card body (not toggle, not gear) - toggle module
        if (mouseButton == 0 && mouseX >= x && mouseX <= x + width - 40 && mouseY >= y && mouseY <= y + height) {
            module.toggle();
            Gugustus.configManager.saveCurrentConfig();
            return;
        }

        // Handle clicks inside the settings dropdown
        if (settingsExpanded) {
            int settingsY = y + height + 2;
            int settingsWidth = width + 10;
            int settingY = settingsY + 5;
            for (SettingWidget widget : settingWidgets) {
                if (mouseY >= settingY && mouseY <= settingY + widget.getHeight() + 4) {
                    widget.mouseClicked(x, settingY, settingsWidth - 10, mouseX, mouseY);
                    return;
                }
                settingY += widget.getHeight() + 4;
            }
        }
    }

    public void mouseReleased() {
        for (SettingWidget widget : settingWidgets) {
            widget.mouseReleased();
        }
    }

    public int getTotalHeight() {
        int total = CARD_HEIGHT + CARD_MARGIN;
        if (settingsExpanded) {
            total += 2 + calculateDropdownHeight();
        }
        return total;
    }

    private int calculateDropdownHeight() {
        int h = 5;
        for (SettingWidget w : settingWidgets) {
            h += w.getHeight() + 4;
        }
        return h + 5;
    }

    public boolean isSettingsExpanded() { return settingsExpanded; }
    public Module getModule() { return module; }
}
