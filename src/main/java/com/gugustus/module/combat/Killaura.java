package com.gugustus.module.combat;

import com.gugustus.module.Category;
import com.gugustus.module.Module;
import com.gugustus.settings.BooleanSetting;
import com.gugustus.settings.ModeSetting;
import com.gugustus.settings.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Killaura extends Module {

    private NumberSetting range;
    private NumberSetting minCPS;
    private NumberSetting maxCPS;
    private ModeSetting autoblock;
    private BooleanSetting rotations;
    private BooleanSetting silentRotations;
    private NumberSetting smoothness;
    private BooleanSetting raytrace;
    private BooleanSetting keepSprint;
    private BooleanSetting teamCheck;
    private BooleanSetting invisibleCheck;
    private ModeSetting targetMode;
    private NumberSetting switchDelay;

    private EntityLivingBase target;
    private EntityLivingBase lastTarget;
    private long lastAttackTime = 0;
    private long lastSwitchTime = 0;
    private boolean blocking = false;
    private Random random = new Random();
    private int asw = 0;
    private int attack = 0;

    public Killaura() {
        super("Killaura", Category.COMBAT, 0);
        range = new NumberSetting("Range", 3.5, 1, 6, 0.1);
        minCPS = new NumberSetting("Min CPS", 10, 1, 20, 1);
        maxCPS = new NumberSetting("Max CPS", 15, 1, 20, 1);
        autoblock = new ModeSetting("Autoblock", new String[]{"None", "Fake", "Real", "Switch"}, "None");
        rotations = new BooleanSetting("Rotations", true);
        silentRotations = new BooleanSetting("Silent Rotations", true);
        smoothness = new NumberSetting("Smoothness", 0.3, 0, 1, 0.05);
        raytrace = new BooleanSetting("Raytrace", true);
        keepSprint = new BooleanSetting("KeepSprint", true);
        teamCheck = new BooleanSetting("Team check", true);
        invisibleCheck = new BooleanSetting("Invisible check", true);
        targetMode = new ModeSetting("Target Mode", new String[]{"Closest", "LowestHP", "HighestHP", "Single"}, "Closest");
        switchDelay = new NumberSetting("Switch Delay", 200, 0, 1000, 10);
        addSetting(range);
        addSetting(minCPS);
        addSetting(maxCPS);
        addSetting(autoblock);
        addSetting(rotations);
        addSetting(silentRotations);
        addSetting(smoothness);
        addSetting(raytrace);
        addSetting(keepSprint);
        addSetting(teamCheck);
        addSetting(invisibleCheck);
        addSetting(targetMode);
        addSetting(switchDelay);
    }

    private List<EntityLivingBase> getValidTargets() {
        List<EntityLivingBase> valid = new ArrayList<>();
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == mc.thePlayer) continue;
            if (!(entity instanceof EntityLivingBase)) continue;
            EntityLivingBase living = (EntityLivingBase) entity;
            if (living.isDead) continue;
            if (living.getHealth() <= 0) continue;
            if (invisibleCheck.getValue() && living.isInvisible()) continue;
            if (teamCheck.getValue() && isTeamMate(living)) continue;
            if (living instanceof EntityPlayer || living instanceof EntityMob || living instanceof EntityAnimal) {
                double dist = mc.thePlayer.getDistanceToEntity(living);
                if (dist <= range.getValue()) {
                    valid.add(living);
                }
            }
        }
        return valid;
    }

    private boolean isTeamMate(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            String displayName = mc.thePlayer.getDisplayName().getUnformattedText();
            String targetName = entity.getDisplayName().getUnformattedText();
            if (displayName.length() >= 2 && targetName.length() >= 2) {
                String teamPrefix = displayName.substring(0, 2);
                String targetPrefix = targetName.substring(0, 2);
                if (teamPrefix.contains("§") && targetPrefix.contains("§")) {
                    return teamPrefix.charAt(1) == targetPrefix.charAt(1);
                }
            }
        }
        return false;
    }

    private EntityLivingBase findTarget() {
        List<EntityLivingBase> valid = getValidTargets();
        if (valid.isEmpty()) return null;

        if (targetMode.is("Single")) {
            return valid.get(0);
        }

        long now = System.currentTimeMillis();
        if (lastTarget != null && valid.contains(lastTarget)) {
            if (now - lastSwitchTime < switchDelay.getValue()) {
                return lastTarget;
            }
        }

        EntityLivingBase best = null;
        double bestValue = targetMode.is("Closest") ? Double.MAX_VALUE : 0;

        for (EntityLivingBase e : valid) {
            double value;
            switch (targetMode.getValue().toLowerCase()) {
                case "closest":
                    value = mc.thePlayer.getDistanceToEntity(e);
                    if (value < bestValue) { bestValue = value; best = e; }
                    break;
                case "lowesthp":
                    value = e.getHealth();
                    if (value < bestValue) { bestValue = value; best = e; }
                    break;
                case "highesthp":
                    value = e.getHealth();
                    if (value > bestValue) { bestValue = value; best = e; }
                    break;
            }
        }

        if (best != lastTarget) {
            lastSwitchTime = now;
        }
        return best;
    }

    private boolean isHoldingSword() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    private boolean canRaytrace(EntityLivingBase entity) {
        if (!raytrace.getValue()) return true;
        return mc.thePlayer.canEntityBeSeen(entity);
    }

    private float[] getRotations(EntityLivingBase entity) {
        double diffX = entity.posX - mc.thePlayer.posX;
        double diffZ = entity.posZ - mc.thePlayer.posZ;
        double diffY = entity.posY + entity.getEyeHeight() - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-Math.atan2(diffY, dist) * 180.0 / Math.PI);
        return new float[]{yaw, pitch};
    }

    private void block() {
        if (!blocking && isHoldingSword()) {
            mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
            blocking = true;
        }
    }

    private void unblock() {
        if (blocking) {
            mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(
                    C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            blocking = false;
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.entity != mc.thePlayer) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        target = findTarget();
        com.gugustus.gui.hud.TargetHUD.currentTarget = target;

        if (target == null) {
            if (blocking) unblock();
            asw = 0;
            attack = 0;
            return;
        }

        if (!canRaytrace(target)) {
            if (blocking) unblock();
            return;
        }

        if (rotations.getValue()) {
            float[] rots = getRotations(target);
            if (silentRotations.getValue()) {
                mc.thePlayer.rotationYawHead = rots[0];
                mc.thePlayer.rotationPitch = rots[1];
            } else {
                float yawDiff = MathHelper.wrapAngleTo180_float(rots[0] - mc.thePlayer.rotationYaw);
                float pitchDiff = rots[1] - mc.thePlayer.rotationPitch;
                float smooth = (float) smoothness.getValue();
                mc.thePlayer.rotationYaw += yawDiff * smooth;
                mc.thePlayer.rotationPitch += pitchDiff * smooth;
            }
        }

        if (!keepSprint.getValue()) {
            mc.thePlayer.setSprinting(false);
        }

        double cps = minCPS.getValue() + random.nextDouble() * (maxCPS.getValue() - minCPS.getValue());
        long attackDelay = (long) (1000.0 / cps);
        long now = System.currentTimeMillis();

        if (now - lastAttackTime < attackDelay) return;

        String abMode = autoblock.getValue();

        if (abMode.equals("Fake")) {
            if (isHoldingSword()) {
                mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
            }
            attackTarget();
        } else if (abMode.equals("Real")) {
            block();
            attackTarget();
        } else if (abMode.equals("Switch")) {
            block();
            attackTarget();
            unblock();
        } else {
            attackTarget();
        }

        lastAttackTime = now;
    }

    private void attackTarget() {
        if (target == null) return;
        mc.thePlayer.swingItem();
        mc.playerController.attackEntity(mc.thePlayer, target);
    }

    @Override
    public void onDisable() {
        target = null;
        lastTarget = null;
        blocking = false;
        asw = 0;
        attack = 0;
    }
}
