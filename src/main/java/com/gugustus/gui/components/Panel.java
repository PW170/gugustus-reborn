package com.gugustus.gui.components;

import com.gugustus.module.Category;
import com.gugustus.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class Panel {

    private Category category;
    private int x, y;
    private int width, height;
    private List<ModuleCard> cards = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int HEADER_HEIGHT = 25;

    public Panel(Category category, List<Module> modules, int x, int y, int width, int height) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        System.out.println("[Gugustus] Panel(" + category + ") creating cards from " + modules.size() + " modules");

        for (Module m : modules) {
            ModuleCard card = new ModuleCard(m, this);
            cards.add(card);
            System.out.println("[Gugustus]  -> Card added for " + m.getName());
        }

        System.out.println("[Gugustus] Panel(" + category + ") total cards: " + cards.size());
    }

    public void draw(int mouseX, int mouseY, float partialTicks) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

        Gui.drawRect(x, y, x + width, y + height, 0xD92C2C2E);
        drawRoundedBorder(x, y, width, height, 0x33FFFFFF);

        fr.drawString(category.getName(), x + 10, y + 8, 0xFFFFFFFF);

        if (cards.isEmpty()) {
            fr.drawString("No modules", x + 10, y + HEADER_HEIGHT + 10, 0xFF666666);
            return;
        }

        // Scissor region: content area below header
        int contentTop = y + HEADER_HEIGHT;
        int contentBottom = y + height - 5;
        int scissorX = x;
        int scissorY = Minecraft.getMinecraft().displayHeight - contentBottom;
        int scissorW = width;
        int scissorH = contentBottom - contentTop;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);

        int currentY = contentTop + scrollOffset;
        for (ModuleCard card : cards) {
            int cardH = ModuleCard.CARD_HEIGHT;
            int totalH = card.getTotalHeight();

            // Check if card is at least partially visible
            if (currentY + cardH > contentTop && currentY < contentBottom) {
                card.draw(x + 5, currentY, width - 10, cardH, mouseX, mouseY);
            }

            // Advance by TOTAL height (card + dropdown if expanded)
            currentY += totalH;
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void drawRoundedBorder(int x, int y, int width, int height, int borderColor) {
        Gui.drawRect(x, y, x + width, y + 1, borderColor);
        Gui.drawRect(x, y + height - 1, x + width, y + height, borderColor);
        Gui.drawRect(x, y, x + 1, y + height, borderColor);
        Gui.drawRect(x + width - 1, y, x + width, y + height, borderColor);
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isHeaderHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + HEADER_HEIGHT;
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        int contentTop = y + HEADER_HEIGHT;
        int currentY = contentTop + scrollOffset;

        for (ModuleCard card : cards) {
            int cardH = ModuleCard.CARD_HEIGHT;
            int totalH = card.getTotalHeight();

            // Check if the click is within this card's vertical span
            if (mouseY >= currentY && mouseY < currentY + totalH) {
                card.mouseClicked(x + 5, currentY, width - 10, cardH, mouseX, mouseY, mouseButton);
                return;
            }

            currentY += totalH;
        }
    }

    public void mouseReleased(int mouseX, int mouseY) {
        for (ModuleCard card : cards) {
            card.mouseReleased();
        }
    }

    public void handleScroll(int scroll) {
        int contentHeight = computeContentHeight();
        int maxScroll = -(contentHeight - (height - HEADER_HEIGHT - 10));

        scrollOffset += scroll > 0 ? 15 : -15;

        if (scrollOffset > 0) scrollOffset = 0;
        if (maxScroll > 0) maxScroll = 0;
        if (scrollOffset < maxScroll) scrollOffset = maxScroll;
    }

    private int computeContentHeight() {
        int total = 0;
        for (ModuleCard card : cards) {
            total += card.getTotalHeight();
        }
        return total;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
