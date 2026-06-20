package com.gugustus.module;

import com.gugustus.module.combat.Autoclicker;
import com.gugustus.module.combat.Killaura;
import com.gugustus.module.combat.Velocity;
import com.gugustus.module.player.InvManager;
import com.gugustus.module.player.ChestStealer;
import com.gugustus.module.player.InvMove;
import com.gugustus.module.player.Scaffold;
import com.gugustus.module.movement.Flight;
import com.gugustus.module.movement.NoSlow;
import com.gugustus.module.movement.Sprint;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();
    private final Map<Category, List<Module>> categoryMap = new EnumMap<>(Category.class);

    public ModuleManager() {
        System.out.println("[Gugustus] ModuleManager constructor called");
        for (Category c : Category.values()) {
            categoryMap.put(c, new ArrayList<Module>());
        }
        registerModules();
        System.out.println("[Gugustus] ModuleManager initialized with " + modules.size() + " modules");
        dumpModuleRegistration();
    }

    private void registerModules() {
        System.out.println("[Gugustus] Registering modules...");

        Module[] moduleArray = {
            new Autoclicker(),
            new Killaura(),
            new Velocity(),
            new InvManager(),
            new ChestStealer(),
            new InvMove(),
            new Scaffold(),
            new Flight(),
            new NoSlow(),
            new Sprint()
        };

        for (Module m : moduleArray) {
            modules.add(m);
            Category cat = m.getCategory();
            if (cat == null) {
                System.out.println("[Gugustus] ERROR: Module " + m.getName() + " has null category!");
                continue;
            }
            List<Module> catList = categoryMap.get(cat);
            if (catList == null) {
                System.out.println("[Gugustus] ERROR: No list found for category " + cat);
                continue;
            }
            catList.add(m);
            System.out.println("[Gugustus]  -> " + cat.name() + ": " + m.getName());
        }
    }

    private void dumpModuleRegistration() {
        StringBuilder sb = new StringBuilder("[Gugustus] Registered modules -> ");
        boolean first = true;
        for (Category c : Category.values()) {
            List<Module> catList = categoryMap.get(c);
            if (catList == null || catList.isEmpty()) continue;
            if (!first) sb.append(", ");
            first = false;
            sb.append(c.name()).append(": [");
            for (int i = 0; i < catList.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(catList.get(i).getName());
            }
            sb.append("]");
        }
        System.out.println(sb.toString());

        // Also show any categories that were expected but are empty
        for (Category c : Category.values()) {
            List<Module> catList = categoryMap.get(c);
            if (catList == null || catList.isEmpty()) {
                System.out.println("[Gugustus] WARNING: Category " + c.name() + " has ZERO modules registered!");
            }
        }
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(Category category) {
        List<Module> result = categoryMap.get(category);
        if (result == null) {
            System.out.println("[Gugustus] WARNING: getModulesByCategory(" + category + ") returned null, creating new list");
            result = new ArrayList<>();
            categoryMap.put(category, result);
        }
        if (result.isEmpty()) {
            System.out.println("[Gugustus] WARNING: getModulesByCategory(" + category + ") returned empty list");
        }
        return result;
    }

    public Module getModule(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }
        System.out.println("[Gugustus] WARNING: Module '" + name + "' not found");
        return null;
    }

    public void onKey(int keyCode) {
        if (keyCode == Keyboard.KEY_NONE) return;
        for (Module m : modules) {
            if (m.getKeybind() == keyCode) {
                m.toggle();
            }
        }
    }
}
