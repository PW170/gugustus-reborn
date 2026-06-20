package com.gugustus.settings;

public class ModeSetting extends Setting {

    private String[] modes;
    private int currentIndex;

    public ModeSetting(String name, String[] modes, String defaultMode) {
        super(name);
        this.modes = modes;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equalsIgnoreCase(defaultMode)) {
                this.currentIndex = i;
                return;
            }
        }
        this.currentIndex = 0;
    }

    public String getValue() {
        return modes[currentIndex];
    }

    public void setValue(String value) {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equalsIgnoreCase(value)) {
                this.currentIndex = i;
                return;
            }
        }
    }

    public void cycle() {
        currentIndex = (currentIndex + 1) % modes.length;
    }

    public void cycleReverse() {
        currentIndex = (currentIndex - 1 + modes.length) % modes.length;
    }

    public String[] getModes() {
        return modes;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean is(String mode) {
        return getValue().equalsIgnoreCase(mode);
    }
}
