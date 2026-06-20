package com.gugustus.module;

import com.gugustus.Gugustus;
import com.gugustus.settings.BooleanSetting;
import com.gugustus.settings.KeybindSetting;
import com.gugustus.settings.ModeSetting;
import com.gugustus.settings.NumberSetting;
import com.gugustus.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

public class Module {

    private String name;
    private Category category;
    private int keybind;
    private boolean enabled;
    private List<Setting> settings;
    protected Minecraft mc = Minecraft.getMinecraft();

    public Module(String name, Category category, int keybind) {
        this.name = name;
        this.category = category;
        this.keybind = keybind;
        this.enabled = false;
        this.settings = new ArrayList<>();
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void toggle() {
        setEnabled(!isEnabled());
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            MinecraftForge.EVENT_BUS.register(this);
            onEnable();
        } else {
            MinecraftForge.EVENT_BUS.unregister(this);
            onDisable();
        }
        if (Gugustus.configManager != null) {
            Gugustus.configManager.saveCurrentConfig();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public int getKeybind() {
        return keybind;
    }

    public void setKeybind(int keybind) {
        this.keybind = keybind;
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public void addSetting(Setting setting) {
        settings.add(setting);
    }

    public BooleanSetting getBooleanSetting(String name) {
        for (Setting s : settings) {
            if (s instanceof BooleanSetting && s.getName().equalsIgnoreCase(name)) {
                return (BooleanSetting) s;
            }
        }
        return null;
    }

    public NumberSetting getNumberSetting(String name) {
        for (Setting s : settings) {
            if (s instanceof NumberSetting && s.getName().equalsIgnoreCase(name)) {
                return (NumberSetting) s;
            }
        }
        return null;
    }

    public ModeSetting getModeSetting(String name) {
        for (Setting s : settings) {
            if (s instanceof ModeSetting && s.getName().equalsIgnoreCase(name)) {
                return (ModeSetting) s;
            }
        }
        return null;
    }

    public KeybindSetting getKeybindSetting(String name) {
        for (Setting s : settings) {
            if (s instanceof KeybindSetting && s.getName().equalsIgnoreCase(name)) {
                return (KeybindSetting) s;
            }
        }
        return null;
    }
}
