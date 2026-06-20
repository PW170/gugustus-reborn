package com.gugustus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gugustus.Gugustus;
import com.gugustus.module.Module;
import com.gugustus.settings.BooleanSetting;
import com.gugustus.settings.ModeSetting;
import com.gugustus.settings.NumberSetting;
import com.gugustus.settings.Setting;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigManager {

    private final File configDir;
    private final File configsDir;
    private final Gson gson;
    private String currentConfigName = "default";

    public ConfigManager() {
        File mcDir = Minecraft.getMinecraft().mcDataDir;
        configDir = new File(mcDir, "Gugustus");
        configsDir = new File(configDir, "configs");
        gson = new GsonBuilder().setPrettyPrinting().create();
        if (!configsDir.exists()) {
            configsDir.mkdirs();
        }
    }

    public File getConfigDir() {
        return configsDir;
    }

    public void loadDefaultConfig() {
        File defaultFile = new File(configsDir, "default.json");
        if (defaultFile.exists()) {
            loadConfig("default");
        } else {
            saveConfig("default");
        }
        currentConfigName = "default";
    }

    public void saveCurrentConfig() {
        saveConfig(currentConfigName);
    }

    public void saveConfig(String name) {
        JsonObject configJson = new JsonObject();
        JsonObject modulesJson = new JsonObject();

        for (Module module : Gugustus.moduleManager.getModules()) {
            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("enabled", module.isEnabled());
            moduleJson.addProperty("keybind", module.getKeybind());

            JsonObject settingsJson = new JsonObject();
            for (Setting setting : module.getSettings()) {
                if (setting instanceof BooleanSetting) {
                    settingsJson.addProperty(setting.getName(), ((BooleanSetting) setting).getValue());
                } else if (setting instanceof NumberSetting) {
                    settingsJson.addProperty(setting.getName(), ((NumberSetting) setting).getValue());
                } else if (setting instanceof ModeSetting) {
                    settingsJson.addProperty(setting.getName(), ((ModeSetting) setting).getValue());
                }
            }
            moduleJson.add("settings", settingsJson);
            modulesJson.add(module.getName(), moduleJson);
        }

        configJson.add("modules", modulesJson);

        File configFile = new File(configsDir, name + ".json");
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(configJson, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean loadConfig(String name) {
        File configFile = new File(configsDir, name + ".json");
        if (!configFile.exists()) {
            return false;
        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject configJson = gson.fromJson(reader, JsonObject.class);
            if (configJson == null) return false;

            JsonObject modulesJson = configJson.getAsJsonObject("modules");
            if (modulesJson == null) return false;

            for (Module module : Gugustus.moduleManager.getModules()) {
                if (modulesJson.has(module.getName())) {
                    JsonObject moduleJson = modulesJson.getAsJsonObject(module.getName());
                    if (moduleJson.has("enabled")) {
                        boolean enabled = moduleJson.get("enabled").getAsBoolean();
                        if (enabled != module.isEnabled()) {
                            module.toggle();
                        }
                    }
                    if (moduleJson.has("keybind")) {
                        module.setKeybind(moduleJson.get("keybind").getAsInt());
                    }
                    if (moduleJson.has("settings")) {
                        JsonObject settingsJson = moduleJson.getAsJsonObject("settings");
                        for (Setting setting : module.getSettings()) {
                            if (settingsJson.has(setting.getName())) {
                                JsonElement value = settingsJson.get(setting.getName());
                                if (setting instanceof BooleanSetting) {
                                    ((BooleanSetting) setting).setValue(value.getAsBoolean());
                                } else if (setting instanceof NumberSetting) {
                                    ((NumberSetting) setting).setValue(value.getAsDouble());
                                } else if (setting instanceof ModeSetting) {
                                    ((ModeSetting) setting).setValue(value.getAsString());
                                }
                            }
                        }
                    }
                }
            }
            currentConfigName = name;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getCurrentConfigName() {
        return currentConfigName;
    }
}
