package com.gugustus.module.player;

import com.gugustus.module.Category;
import com.gugustus.module.Module;
import com.gugustus.settings.BooleanSetting;
import com.gugustus.settings.NumberSetting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class InvManager extends Module {

    private BooleanSetting autoSort;
    private BooleanSetting autoDrop;
    private BooleanSetting refill;
    private NumberSetting refillThreshold;

    private int currentTick = 0;

    public InvManager() {
        super("InvManager", Category.PLAYER, 0);
        autoSort = new BooleanSetting("AutoSort", true);
        autoDrop = new BooleanSetting("AutoDrop", false);
        refill = new BooleanSetting("Refill", true);
        refillThreshold = new NumberSetting("Refill threshold", 1, 1, 64, 1);
        addSetting(autoSort);
        addSetting(autoDrop);
        addSetting(refill);
        addSetting(refillThreshold);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!(mc.currentScreen instanceof GuiInventory)) return;

        currentTick++;
        if (currentTick % 5 != 0) return;

        if (autoDrop.getValue()) {
            dropJunk();
        }

        if (autoSort.getValue()) {
            sortInventory();
        }

        if (refill.getValue()) {
            refillHotbar();
        }
    }

    private void dropJunk() {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null && isJunk(stack)) {
                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, i, 1, 4, mc.thePlayer);
            }
        }
    }

    private boolean isJunk(ItemStack stack) {
        String name = stack.getDisplayName().toLowerCase();
        return name.contains("dirt") || name.contains("cobblestone") || name.contains("gravel")
                || name.contains("sand") || name.contains("rotten") || name.contains("bone");
    }

    private void sortInventory() {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack == null) continue;
            int bestSlot = i;
            for (int j = 9; j < 36; j++) {
                ItemStack other = mc.thePlayer.inventoryContainer.getSlot(j).getStack();
                if (other == null) continue;
                if (getItemValue(other) > getItemValue(stack)) {
                    bestSlot = j;
                }
            }
            if (bestSlot != i) {
                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, i, 0, 2, mc.thePlayer);
                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, bestSlot, 0, 2, mc.thePlayer);
                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, i, 0, 2, mc.thePlayer);
            }
        }
    }

    private int getItemValue(ItemStack stack) {
        int value = 0;
        if (stack.getItem() instanceof ItemSword) {
            value += 100 + EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
        } else if (stack.getItem() instanceof ItemArmor) {
            value += 80 + ((ItemArmor) stack.getItem()).damageReduceAmount;
        } else if (stack.getItem() instanceof ItemTool) {
            value += 60;
        }
        return value;
    }

    private void refillHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack == null || stack.stackSize <= refillThreshold.getValueInt()) {
                for (int j = 9; j < 36; j++) {
                    ItemStack invStack = mc.thePlayer.inventory.mainInventory[j];
                    if (invStack != null && stack != null && invStack.getItem() == stack.getItem()
                            && invStack.getMetadata() == stack.getMetadata()) {
                        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, j, 0, 2, mc.thePlayer);
                        break;
                    }
                }
            }
        }
    }
}
