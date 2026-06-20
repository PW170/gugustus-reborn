package com.gugustus.settings;

public class KeybindSetting extends Setting {

    private int keyCode;

    public KeybindSetting(String name, int defaultKeyCode) {
        super(name);
        this.keyCode = defaultKeyCode;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }
}
