package com.gugustus.module.visual;

import com.gugustus.module.Category;
import com.gugustus.module.Module;

public class ClickGUI extends Module {

    public ClickGUI() {
        super("ClickGUI", Category.VISUAL, 54);
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen(new com.gugustus.gui.ClickGUI());
        toggle();
    }
}
