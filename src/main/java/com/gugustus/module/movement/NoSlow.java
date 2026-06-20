package com.gugustus.module.movement;

import com.gugustus.module.Category;
import com.gugustus.module.Module;
import com.gugustus.settings.BooleanSetting;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class NoSlow extends Module {

    private BooleanSetting items;
    private BooleanSetting soulsand;
    private BooleanSetting web;

    public NoSlow() {
        super("NoSlow", Category.MOVEMENT, 0);
        items = new BooleanSetting("Items", true);
        soulsand = new BooleanSetting("Soulsand", true);
        web = new BooleanSetting("Web", true);
        addSetting(items);
        addSetting(soulsand);
        addSetting(web);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (items.getValue() && mc.thePlayer.isUsingItem()) {
            mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(
                    C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
        }

        if (soulsand.getValue() && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down()).getBlock()
                .getUnlocalizedName().contains("soulSand")) {
            mc.thePlayer.motionX *= 1.4;
            mc.thePlayer.motionZ *= 1.4;
        }

        if (web.getValue() && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer)).getBlock()
                .getUnlocalizedName().contains("web")) {
            mc.thePlayer.motionX *= 1.4;
            mc.thePlayer.motionZ *= 1.4;
            mc.thePlayer.motionY *= 1.4;
        }
    }
}
