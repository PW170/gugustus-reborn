package com.gugustus.gui;

import com.gugustus.Gugustus;
import com.gugustus.gui.components.Panel;
import com.gugustus.module.Category;
import com.gugustus.module.Module;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends GuiScreen {

    private List<Panel> panels = new ArrayList<>();
    private Panel selectedPanel = null;
    private int dragStartX, dragStartY;
    private boolean dragging = false;

    private static final int PANEL_WIDTH = 140;
    private static final int PANEL_HEIGHT = 280;

    public ClickGUI() {
        System.out.println("[Gugustus] Opening ClickGUI...");
        if (Gugustus.moduleManager == null) {
            System.out.println("[Gugustus] ERROR: moduleManager is null!");
            return;
        }

        int xStart = 20;
        for (Category category : Category.values()) {
            List<Module> modules = Gugustus.moduleManager.getModulesByCategory(category);
            System.out.println("[Gugustus] Category " + category + " has " + modules.size() + " modules");
            if (modules != null && !modules.isEmpty()) {
                Panel panel = new Panel(category, modules, xStart, 30, PANEL_WIDTH, PANEL_HEIGHT);
                panels.add(panel);
                xStart += PANEL_WIDTH + 10;
            }
        }
        System.out.println("[Gugustus] ClickGUI panels created: " + panels.size());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        for (Panel panel : panels) {
            panel.draw(mouseX, mouseY, partialTicks);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (Panel panel : panels) {
            if (panel.isHovered(mouseX, mouseY)) {
                if (panel.isHeaderHovered(mouseX, mouseY)) {
                    if (mouseButton == 0) {
                        dragging = true;
                        selectedPanel = panel;
                        dragStartX = mouseX - panel.getX();
                        dragStartY = mouseY - panel.getY();
                        return;
                    }
                } else {
                    panel.mouseClicked(mouseX, mouseY, mouseButton);
                    return;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
        selectedPanel = null;
        for (Panel panel : panels) {
            panel.mouseReleased(mouseX, mouseY);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            int mx = Mouse.getEventX() * this.width / mc.displayWidth;
            int my = this.height - Mouse.getEventY() * this.height / mc.displayHeight;
            for (Panel panel : panels) {
                if (panel.isHovered(mx, my)) {
                    panel.handleScroll(scroll);
                }
            }
        }
    }

    @Override
    public void updateScreen() {
        if (dragging && selectedPanel != null) {
            int mx = Mouse.getX() * this.width / mc.displayWidth;
            int my = this.height - Mouse.getY() * this.height / mc.displayHeight;
            selectedPanel.setX(mx - dragStartX);
            selectedPanel.setY(my - dragStartY);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
