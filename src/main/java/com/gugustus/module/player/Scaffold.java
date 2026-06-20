package com.gugustus.module.player;

import com.gugustus.module.Category;
import com.gugustus.module.Module;
import com.gugustus.settings.BooleanSetting;
import com.gugustus.settings.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Scaffold extends Module {

    private BooleanSetting eagle;
    private NumberSetting sneakDelay;
    private BooleanSetting autoPick;
    private BooleanSetting tower;
    private BooleanSetting sameY;
    private NumberSetting placeDelay;

    private BlockPos targetBlock;
    private EnumFacing targetFacing;
    private boolean wasSneaking = false;
    private long lastPlaceTime = 0;
    private boolean shouldSneak = false;
    private double startY = -1;

    public Scaffold() {
        super("Scaffold", Category.PLAYER, 0);
        eagle = new BooleanSetting("Eagle", true);
        sneakDelay = new NumberSetting("Sneak delay", 50, 0, 500, 5);
        autoPick = new BooleanSetting("AutoPick", true);
        tower = new BooleanSetting("Tower", false);
        sameY = new BooleanSetting("SameY", true);
        placeDelay = new NumberSetting("Place delay", 0, 0, 500, 5);
        addSetting(eagle);
        addSetting(sneakDelay);
        addSetting(autoPick);
        addSetting(tower);
        addSetting(sameY);
        addSetting(placeDelay);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (sameY.getValue() && startY == -1) {
            startY = mc.thePlayer.posY;
        }

        if (sameY.getValue() && mc.thePlayer.posY > startY + 0.5) {
            return;
        }

        if (tower.getValue() && mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.thePlayer.motionY = 0.42;
            mc.thePlayer.onGround = false;
        }

        int slot = -1;
        if (autoPick.getValue()) {
            slot = findBlockSlot();
            if (slot != -1 && slot != mc.thePlayer.inventory.currentItem) {
                mc.thePlayer.inventory.currentItem = slot;
                mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(slot));
            }
        }

        shouldSneak = false;
        if (eagle.getValue() && isAtEdge()) {
            shouldSneak = true;
            if (!wasSneaking) {
                long now = System.currentTimeMillis();
                if (lastPlaceTime == 0 || now - lastPlaceTime >= sneakDelay.getValue()) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                    wasSneaking = true;
                }
            }
        } else if (wasSneaking) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            wasSneaking = false;
        }

        if (findPlacementPosition()) {
            long now = System.currentTimeMillis();
            if (now - lastPlaceTime >= placeDelay.getValue()) {
                placeBlock();
                lastPlaceTime = now;
            }
        }
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) stack.getItem()).getBlock();
                if (block.isFullBlock()) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean isAtEdge() {
        double x = mc.thePlayer.posX;
        double z = mc.thePlayer.posZ;
        BlockPos below = new BlockPos(MathHelper.floor_double(x), MathHelper.floor_double(mc.thePlayer.posY) - 1, MathHelper.floor_double(z));
        return mc.theWorld.isAirBlock(below);
    }

    private boolean findPlacementPosition() {
        BlockPos below = new BlockPos(MathHelper.floor_double(mc.thePlayer.posX),
                MathHelper.floor_double(mc.thePlayer.posY) - 1,
                MathHelper.floor_double(mc.thePlayer.posZ));

        for (BlockPos pos : new BlockPos[]{below, below.east(), below.west(), below.north(), below.south(),
                below.east().north(), below.east().south(), below.west().north(), below.west().south()}) {
            if (mc.theWorld.isAirBlock(pos)) {
                for (EnumFacing facing : EnumFacing.values()) {
                    BlockPos neighbor = pos.offset(facing);
                    if (!mc.theWorld.isAirBlock(neighbor) && mc.theWorld.getBlockState(neighbor).getBlock().isFullBlock()) {
                        targetBlock = pos;
                        targetFacing = facing.getOpposite();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void placeBlock() {
        if (targetBlock == null) return;
        if (findBlockSlot() == -1) return;

        mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(),
                targetBlock, targetFacing, new Vec3(0.5, 0.5, 0.5));
        mc.thePlayer.swingItem();
    }

    @Override
    public void onDisable() {
        if (wasSneaking) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            wasSneaking = false;
        }
        startY = -1;
    }
}
