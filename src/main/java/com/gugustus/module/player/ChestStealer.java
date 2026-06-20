package com.gugustus.module.player;

import com.gugustus.module.Category;
import com.gugustus.module.Module;
import com.gugustus.settings.BooleanSetting;
import com.gugustus.settings.NumberSetting;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ChestStealer extends Module {

    private BooleanSetting autoSteal;
    private NumberSetting delay;
    private BooleanSetting onlyValuable;
    private BooleanSetting closeOnEmpty;

    private int currentTick = 0;

    public ChestStealer() {
        super("ChestStealer", Category.PLAYER, 0);
        autoSteal = new BooleanSetting("AutoSteal", true);
        delay = new NumberSetting("Delay", 50, 0, 500, 5);
        onlyValuable = new BooleanSetting("Only valuable", false);
        closeOnEmpty = new BooleanSetting("Close on empty", true);
        addSetting(autoSteal);
        addSetting(delay);
        addSetting(onlyValuable);
        addSetting(closeOnEmpty);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (!(mc.currentScreen instanceof GuiChest)) {
            currentTick = 0;
            return;
        }

        if (!autoSteal.getValue()) return;

        GuiChest chest = (GuiChest) mc.currentScreen;
        ContainerChest container = (ContainerChest) chest.inventorySlots;

        currentTick++;
        int delayTicks = (int) Math.max(1, delay.getValue() / 50);

        if (currentTick % delayTicks != 0) return;

        boolean hasItems = false;
        for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); i++) {
            if (container.getSlot(i).getHasStack()) {
                hasItems = true;
                mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                break;
            }
        }

        if (!hasItems && closeOnEmpty.getValue()) {
            mc.thePlayer.closeScreen();
        }
    }
}
