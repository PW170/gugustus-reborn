package com.gugustus.gui.components;

import com.gugustus.Gugustus;
import com.gugustus.settings.BooleanSetting;
import com.gugustus.settings.ModeSetting;
import com.gugustus.settings.NumberSetting;
import com.gugustus.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public class SettingWidget {

    private Setting setting;
    private ModuleCard card;
    private boolean dragging = false;
    private boolean modeDropdownOpen = false;
    private boolean listeningForKey = false;
    private GuiTextField textField;
    private String tempText = "";

    public SettingWidget(Setting setting, ModuleCard card) {
        this.setting = setting;
        this.card = card;
    }

    public void draw(int x, int y, int width, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();

        if (setting instanceof BooleanSetting) {
            BooleanSetting bs = (BooleanSetting) setting;
            mc.fontRendererObj.drawString(setting.getName(), x + 8, y + 4, 0xFFB0B0B0);
            int toggleX = x + width - 30;
            Gui.drawRect(toggleX, y + 3, toggleX + 22, y + 15, 0x66333333);
            if (bs.getValue()) {
                Gui.drawRect(toggleX + 10, y + 4, toggleX + 21, y + 14, 0xFFF57C00);
            } else {
                Gui.drawRect(toggleX + 1, y + 4, toggleX + 12, y + 14, 0xFF555555);
            }
        } else if (setting instanceof NumberSetting) {
            NumberSetting ns = (NumberSetting) setting;
            mc.fontRendererObj.drawString(setting.getName() + ": " + String.format("%.1f", ns.getValue()), x + 8, y + 4, 0xFFB0B0B0);
            int sliderX = x + 8;
            int sliderY = y + 16;
            int sliderWidth = width - 16;
            Gui.drawRect(sliderX, sliderY, sliderX + sliderWidth, sliderY + 4, 0x66333333);
            double fill = (ns.getValue() - ns.getMinimum()) / (ns.getMaximum() - ns.getMinimum());
            Gui.drawRect(sliderX, sliderY, (int) (sliderX + sliderWidth * fill), sliderY + 4, 0xFFF57C00);

            if (dragging) {
                double newVal = (double) (mouseX - sliderX) / sliderWidth * (ns.getMaximum() - ns.getMinimum()) + ns.getMinimum();
                newVal = Math.round(newVal / ns.getIncrement()) * ns.getIncrement();
                ns.setValue(newVal);
                Gugustus.configManager.saveCurrentConfig();
            }
        } else if (setting instanceof ModeSetting) {
            ModeSetting ms = (ModeSetting) setting;
            mc.fontRendererObj.drawString(setting.getName(), x + 8, y + 2, 0xFFB0B0B0);
            Gui.drawRect(x + 8, y + 12, x + width - 8, y + 24, 0x66444444);
            mc.fontRendererObj.drawString(ms.getValue(), x + 12, y + 14, 0xFFFFFFFF);

            if (modeDropdownOpen) {
                String[] modes = ms.getModes();
                int ddY = y + 25;
                for (int i = 0; i < modes.length; i++) {
                    Gui.drawRect(x + 8, ddY, x + width - 8, ddY + 14, i == ms.getCurrentIndex() ? 0x88F57C00 : 0x88444444);
                    mc.fontRendererObj.drawString(modes[i], x + 12, ddY + 3, modes[i].equals(ms.getValue()) ? 0xFFF57C00 : 0xFFB0B0B0);
                    ddY += 15;
                }
            }
        }
    }

    public void mouseClicked(int x, int y, int width, int mouseX, int mouseY) {
        if (setting instanceof BooleanSetting) {
            BooleanSetting bs = (BooleanSetting) setting;
            int toggleX = x + width - 30;
            if (mouseX >= toggleX && mouseX <= toggleX + 22 && mouseY >= y + 3 && mouseY <= y + 15) {
                bs.toggle();
                Gugustus.configManager.saveCurrentConfig();
            }
        } else if (setting instanceof NumberSetting) {
            int sliderX = x + 8;
            int sliderY = y + 16;
            int sliderWidth = width - 16;
            if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth && mouseY >= sliderY && mouseY <= sliderY + 4) {
                dragging = true;
            }
        } else if (setting instanceof ModeSetting) {
            ModeSetting ms = (ModeSetting) setting;
            if (mouseX >= x + 8 && mouseX <= x + width - 8 && mouseY >= y + 12 && mouseY <= y + 24) {
                modeDropdownOpen = !modeDropdownOpen;
            } else if (modeDropdownOpen) {
                String[] modes = ms.getModes();
                int ddY = y + 25;
                for (int i = 0; i < modes.length; i++) {
                    if (mouseX >= x + 8 && mouseX <= x + width - 8 && mouseY >= ddY && mouseY <= ddY + 14) {
                        ms.setValue(modes[i]);
                        modeDropdownOpen = false;
                        Gugustus.configManager.saveCurrentConfig();
                        return;
                    }
                    ddY += 15;
                }
                modeDropdownOpen = false;
            }
        }
    }

    public void mouseReleased() {
        if (dragging) {
            dragging = false;
            Gugustus.configManager.saveCurrentConfig();
        }
    }

    public int getHeight() {
        if (setting instanceof NumberSetting) return 24;
        if (setting instanceof BooleanSetting) return 18;
        if (setting instanceof ModeSetting) return 24;
        return 18;
    }
}
