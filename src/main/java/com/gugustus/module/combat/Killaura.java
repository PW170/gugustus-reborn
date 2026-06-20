package com.gugustus.module.combat;

import com.gugustus.Gugustus;
import com.gugustus.module.Category;
import com.gugustus.module.Module;
import com.gugustus.settings.BooleanSetting;
import com.gugustus.settings.ModeSetting;
import com.gugustus.settings.NumberSetting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Random;

public class Killaura extends Module {

    private final NumberSetting minCPS;
    private final NumberSetting maxCPS;
    private final ModeSetting autoblock;
    private final ModeSetting rotation;
    private final NumberSetting attackRange;
    private final NumberSetting blockRange;
    private final NumberSetting rotationRange;
    private final NumberSetting switchDelay;
    private final BooleanSetting rotateBody;
    private final BooleanSetting targetESP;
    private final BooleanSetting swordOnly;
    private final BooleanSetting team;
    private final BooleanSetting movefix;
    private final BooleanSetting delayWhileAttacking;
    private final BooleanSetting raycast;
    private final BooleanSetting switchAttack;
    private final BooleanSetting reach;
    private final NumberSetting reach2;

    private final ArrayList<EntityLivingBase> validTargets = new ArrayList<>();
    private EntityLivingBase target;
    private EntityLivingBase lastTarget;
    private long lastAttackTime;
    private long lastSwitchTime;
    private boolean blocking;
    private boolean swapped;
    private boolean b3;
    private boolean postBlock;
    private int rotTick;
    private int asw;
    private int attackCounter;
    private int nextTick;
    private int reached;
    private int reduce;
    private float currentYaw;
    private float currentPitch;

    private static final Random random = new Random();

    public Killaura() {
        super("Killaura", Category.COMBAT, 0);

        minCPS = new NumberSetting("Min CPS", 10, 1, 20, 1);
        maxCPS = new NumberSetting("Max CPS", 10, 1, 20, 1);
        autoblock = new ModeSetting("Autoblock", new String[]{"None","Fake","Vanilla","BlocksMC","Hypixel","Hypixel2","Hypixel3","NCP","Vulcan","Legit"}, "None");
        rotation = new ModeSetting("Rotations", new String[]{"None","Normal","Hypixel","Grim","Vulcan"}, "Normal");
        attackRange = new NumberSetting("Attack Range", 3, 3, 10, 0.1);
        blockRange = new NumberSetting("Block Range", 3, 3, 10, 0.1);
        rotationRange = new NumberSetting("Rotation Range", 3, 3, 10, 0.1);
        switchDelay = new NumberSetting("Switch Delay", 150, 0, 1000, 10);
        rotateBody = new BooleanSetting("Rotate Body", false);
        targetESP = new BooleanSetting("Target ESP", false);
        swordOnly = new BooleanSetting("Sword Only", false);
        team = new BooleanSetting("Teams", true);
        movefix = new BooleanSetting("Movefix", false);
        delayWhileAttacking = new BooleanSetting("Delay While Attacking", false);
        raycast = new BooleanSetting("Raycast", false);
        switchAttack = new BooleanSetting("Switch On Attack", false);
        reach = new BooleanSetting("Hypixel Reach Bypass", false);
        reach2 = new NumberSetting("Hypixel Reach", 3.1, 3.1, 3.5, 0.1);

        addSetting(minCPS);
        addSetting(maxCPS);
        addSetting(autoblock);
        addSetting(rotation);
        addSetting(attackRange);
        addSetting(blockRange);
        addSetting(rotationRange);
        addSetting(switchDelay);
        addSetting(rotateBody);
        addSetting(targetESP);
        addSetting(swordOnly);
        addSetting(team);
        addSetting(movefix);
        addSetting(delayWhileAttacking);
        addSetting(raycast);
        addSetting(switchAttack);
        addSetting(reach);
        addSetting(reach2);
    }

    // ====================== UTILITY ======================

    private boolean isHoldingSword() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    private boolean isTeamMate(EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return false;
        String displayName = mc.thePlayer.getDisplayName().getUnformattedText();
        String targetName = entity.getDisplayName().getUnformattedText();
        if (displayName.length() < 2 || targetName.length() < 2) return false;
        String teamPrefix = displayName.substring(0, 2);
        String targetPrefix = targetName.substring(0, 2);
        return teamPrefix.contains("\u00a7") && targetPrefix.contains("\u00a7") && teamPrefix.charAt(1) == targetPrefix.charAt(1);
    }

    private float[] getRotations(EntityLivingBase entity) {
        double diffX = entity.posX - mc.thePlayer.posX;
        double diffZ = entity.posZ - mc.thePlayer.posZ;
        double diffY = entity.posY + entity.getEyeHeight() - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90;
        float pitch = (float) (-Math.atan2(diffY, dist) * 180.0 / Math.PI);
        return new float[]{yaw, pitch};
    }

    private Vec3 getVectorForRotation(float pitch, float yaw) {
        float radPitch = pitch * 0.017453292F;
        float radYaw = yaw * 0.017453292F;
        return new Vec3(
                -MathHelper.sin(radYaw) * MathHelper.cos(radPitch),
                -MathHelper.sin(radPitch),
                MathHelper.cos(radYaw) * MathHelper.cos(radPitch));
    }

    private boolean isTargetInRange(EntityLivingBase target, double range) {
        float[] rotations = getRotations(target);
        Vec3 eyes = mc.thePlayer.getPositionEyes(1);
        Vec3 look = getVectorForRotation(rotations[1], rotations[0]);
        Vec3 end = eyes.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);
        AxisAlignedBB bb = target.getEntityBoundingBox();
        MovingObjectPosition mop = bb.calculateIntercept(eyes, end);
        return mop != null && eyes.distanceTo(mop.hitVec) <= range;
    }

    private MovingObjectPosition rayCastEntity(double range, float yaw, float pitch) {
        Vec3 eyes = mc.thePlayer.getPositionEyes(1);
        Vec3 look = getVectorForRotation(pitch, yaw);
        Vec3 end = eyes.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);
        return mc.theWorld.rayTraceBlocks(eyes, end, false, false, true);
    }

    private void attack(EntityLivingBase e, boolean interact) {
        if (mc.thePlayer == null || e == null) return;
        mc.thePlayer.swingItem();
        mc.playerController.attackEntity(mc.thePlayer, e);
        if (interact) {
            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(e, new Vec3(0, 0, 0)));
            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(e, C02PacketUseEntity.Action.INTERACT));
        }
        lastAttackTime = System.currentTimeMillis();
    }

    private void sendBlock() {
        mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
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

    private void sendSwap(int slot) {
        mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(slot));
    }

    // ====================== TARGET SELECTION ======================

    private EntityLivingBase getTarget(double range) {
        EntityLivingBase extendedRangeTarget = null;
        validTargets.clear();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase)) continue;
            EntityLivingBase living = (EntityLivingBase) entity;
            if (living == mc.thePlayer) continue;
            if (living.isDead) continue;
            if (team.getValue() && isTeamMate(living)) continue;
            if (swordOnly.getValue() && !isHoldingSword()) continue;
            if (!(living instanceof EntityPlayer) && !(living instanceof EntityMob) && !(living instanceof EntityAnimal))
                continue;

            double dist = mc.thePlayer.getDistanceToEntity(living);
            if (dist <= attackRange.getValue()) {
                validTargets.add(living);
            } else if (dist <= range && extendedRangeTarget == null) {
                extendedRangeTarget = living;
            }
        }

        if (validTargets.isEmpty()) return extendedRangeTarget;

        long now = System.currentTimeMillis();
        if (lastTarget != null && (now - lastSwitchTime > switchDelay.getValue() || !validTargets.contains(lastTarget) || lastTarget.isDead)) {
            if (validTargets.size() > 1 && lastTarget != null) validTargets.remove(lastTarget);
            int index = random.nextInt(validTargets.size());
            lastTarget = validTargets.get(index);
            lastSwitchTime = now;
        }
        return lastTarget;
    }

    // ====================== MAIN TICK ======================

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if ((Gugustus.moduleManager.getModule("Scaffold") != null && Gugustus.moduleManager.getModule("Scaffold").isEnabled()) || mc.currentScreen != null) {
            target = null;
            resetState();
            currentYaw = 0;
            return;
        }

        float a = (float) attackRange.getValue();
        float b = (float) blockRange.getValue();
        float c = (float) rotationRange.getValue();
        float targetRange = Math.max(a, Math.max(b, c));
        target = getTarget(targetRange);

        if (target == null) {
            resetState();
            currentYaw = 0;
            return;
        }

        if (!isTargetInRange(target, blockRange.getValue()) || !isHoldingSword()) {
            resetState();
        }

        if (target == null) return;

        // Movefix
        if (movefix.getValue()) {
            // Movefix flag — handled in LivingUpdateEvent
        }

        nextTick++;

        if (target != lastTarget) {
            rotTick = -1;
        }

        int minCPSi = (int) minCPS.getValue();
        int maxCPSi = (int) maxCPS.getValue();
        if (minCPSi > maxCPSi) { int t = minCPSi; minCPSi = maxCPSi; maxCPSi = t; }

        long now = System.currentTimeMillis();
        int cps = minCPSi + random.nextInt(maxCPSi - minCPSi + 8);
        int delay = 1000 / cps;

        // ====== AUTO BLOCK ======
        if (isTargetInRange(target, blockRange.getValue()) && isHoldingSword()) {
            autoblockHandler(now, delay, minCPSi, maxCPSi);
        } else {
            resetState();
        }

        // ====== ATTACK (non-block modes) ======
        double atkRange = reach.getValue() && reached < 3 ? reach2.getValue() : attackRange.getValue();
        if (isTargetInRange(target, atkRange) && now - lastAttackTime >= delay
                && !autoblock.getValue().equals("BlocksMC")
                && !autoblock.getValue().equals("Legit")
                && !autoblock.getValue().equals("Vulcan")
                && !autoblock.getValue().startsWith("Hypixel")) {
            attack(target, false);
        }
    }

    private void autoblockHandler(long now, int delay, int minCPSi, int maxCPSi) {
        String mode = autoblock.getValue();
        switch (mode) {
            case "Vanilla":
                sendBlock();
                break;

            case "Vulcan":
                b3 = true;
                if (blocking) {
                    unblock();
                    attackCounter++;
                } else {
                    if (isTargetInRange(target, attackRange.getValue()) && attackCounter < 4) {
                        attack(target, true);
                        setMotion(0.97);
                        lastAttackTime = now;
                    } else {
                        attackCounter = 0;
                    }
                    block();
                }
                break;

            case "Legit": {
                b3 = true;
                if (blocking) {
                    unblock();
                } else {
                    int cps = minCPSi + maxCPSi / 2;
                    int d = 1000 / cps;
                    if (now - lastAttackTime < d) return;
                    if (isTargetInRange(target, attackRange.getValue())) {
                        attack(target, true);
                    }
                    lastAttackTime = now;
                    nextTick = -1;
                    block();
                }
                break;
            }

            case "BlocksMC": {
                asw++;
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
                b3 = true;
                switch (asw) {
                    case 1:
                        attackCounter++;
                        if (isTargetInRange(target, attackRange.getValue()) && rotTick > 0) {
                            MovingObjectPosition mop = rayCastEntity(8, mc.thePlayer.rotationYawHead, mc.thePlayer.rotationPitch);
                            if (mop != null && attackCounter < 8) {
                                attack(target, false);
                                mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, new Vec3(
                                        mop.hitVec.xCoord - target.posX, mop.hitVec.yCoord - target.posY, mop.hitVec.zCoord - target.posZ)));
                                mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
                            } else {
                                attackCounter = 0;
                            }
                        }
                        block();
                        asw = 0;
                        break;
                    case 2:
                        sendSwap((mc.thePlayer.inventory.currentItem + 1) % 9);
                        swapped = true;
                        asw = 0;
                        break;
                    case 3:
                        sendSwap(mc.thePlayer.inventory.currentItem);
                        if (blocking) unblock();
                        swapped = false;
                        asw = 0;
                        break;
                }
                break;
            }

            case "Hypixel": {
                switch (asw) {
                    case 0:
                        b3 = true;
                        attackCounter++;
                        int slot = random.nextInt(9);
                        while (slot == mc.thePlayer.inventory.currentItem) slot = random.nextInt(9);
                        if (blocking) unblock();
                        asw = 1;
                        break;
                    case 1: {
                        double atkR = reach.getValue() && reached < 3 ? reach2.getValue() : attackRange.getValue();
                        if (isTargetInRange(target, atkR)) {
                            attack(target, true);
                        } else if (isTargetInRange(target, rotationRange.getValue())) {
                            mc.thePlayer.swingItem();
                        }
                        reached++;
                        if (reached >= 4) reached = 0;
                        nextTick = -1;
                        block();
                        asw = 0;
                        break;
                    }
                }
                break;
            }

            case "Hypixel2": {
                switch (asw) {
                    case 0:
                        b3 = true;
                        attackCounter++;
                        int slot = random.nextInt(9);
                        while (slot == mc.thePlayer.inventory.currentItem) slot = random.nextInt(9);
                        sendSwap(slot);
                        if (blocking) unblock();
                        swapped = true;
                        asw = 1;
                        break;
                    case 1:
                        if (swapped) { sendSwap(mc.thePlayer.inventory.currentItem); swapped = false; }
                        asw = 2;
                        break;
                    case 2: case 3:
                        asw++;
                        break;
                    case 4: {
                        double atkR = reach.getValue() && reached < 2 ? reach2.getValue() : attackRange.getValue();
                        if (isTargetInRange(target, atkR)) {
                            attack(target, true);
                        } else if (isTargetInRange(target, rotationRange.getValue())) {
                            mc.thePlayer.swingItem();
                        }
                        reached++;
                        if (isTargetInRange(target, attackRange.getValue())) reached = 0;
                        block();
                        b3 = true;
                        postBlock = attackCounter % 2 == 0;
                        asw = 0;
                        break;
                    }
                }
                break;
            }

            case "Hypixel3": {
                switch (asw) {
                    case 0:
                        b3 = true;
                        if (postBlock) { block(); }
                        attackCounter++;
                        if (blocking && !postBlock) unblock();
                        asw = 1;
                        break;
                    case 1:
                        if (blocking) unblock();
                        asw = 2;
                        break;
                    case 2: {
                        double atkR = reach.getValue() && reached < 2 ? reach2.getValue() : attackRange.getValue();
                        if (isTargetInRange(target, atkR)) {
                            attack(target, true);
                        } else if (isTargetInRange(target, rotationRange.getValue())) {
                            mc.thePlayer.swingItem();
                        }
                        reached++;
                        if (isTargetInRange(target, attackRange.getValue())) reached = 0;
                        postBlock = false;
                        nextTick = -1;
                        block();
                        b2 = true;
                        asw = 0;
                        break;
                    }
                }
                break;
            }

            case "NCP":
                unblock();
                break;

            default:
                break;
        }
    }

    private boolean b2;

    // ====================== LIVING UPDATE (movement/input) ======================

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.entity != mc.thePlayer) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (target == null) return;

        // Sprint handling (onSprint equivalent)
        if (b2) {
            if (autoblock.getValue().equals("Hypixel3")) {
                block();
            }
            b2 = false;
        }

        // Pre-input modifications
        String ab = autoblock.getValue();
        if ((ab.equals("BlocksMC") || ab.equals("Legit") || ab.equals("Hypixel") || ab.equals("Hypixel3"))
                && isTargetInRange(target, blockRange.getValue()) && isHoldingSword() && nextTick < 0) {
            mc.thePlayer.movementInput.moveStrafe *= 0.2F;
            mc.thePlayer.movementInput.moveForward *= 0.2F;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
        }

        // Rotations
        if (target != null && isTargetInRange(target, rotationRange.getValue()) && !rotation.getValue().equals("None")) {
            rotTick++;
            float[] rots = getRotations(target);
            mc.thePlayer.rotationYawHead = rots[0];

            if (rotateBody.getValue()) {
                mc.thePlayer.renderYawOffset = mc.thePlayer.rotationYawHead
                        - MathHelper.clamp_float(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYawHead - mc.thePlayer.renderYawOffset), -75, 75);
                mc.thePlayer.renderYawOffset += MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYawHead - mc.thePlayer.renderYawOffset) * 0.3F;
            }

            mc.thePlayer.rotationPitch = rots[1];
            currentYaw = rots[0];
            currentPitch = rots[1];
        }

        // Sprint stop
        if (ab.equals("BlocksMC") && isTargetInRange(target, blockRange.getValue()) && isHoldingSword()) {
            mc.thePlayer.setSprinting(false);
        }

        // Post-motion block (NCP)
        if (autoblock.getValue().equals("NCP") && target != null
                && isTargetInRange(target, blockRange.getValue()) && isHoldingSword()) {
            block();
        }

        // Movefix
        if (movefix.getValue() && target != null) {
            mc.thePlayer.motionX *= 0.97;
            mc.thePlayer.motionZ *= 0.97;
        }
    }

    private void setMotion(double factor) {
        mc.thePlayer.motionX *= factor;
        mc.thePlayer.motionZ *= factor;
    }

    // ====================== RENDER ======================

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (target != null && targetESP.getValue()) {
            // Simplified ESP: draw a bounding box
            AxisAlignedBB bb = target.getEntityBoundingBox();
            if (bb != null) {
                double x = bb.minX - mc.getRenderManager().viewerPosX;
                double y = bb.minY - mc.getRenderManager().viewerPosY;
                double z = bb.minZ - mc.getRenderManager().viewerPosZ;
                AxisAlignedBB renderBB = new AxisAlignedBB(x, y, z, x + bb.maxX - bb.minX, y + bb.maxY - bb.minY, z + bb.maxZ - bb.minZ);
                mc.getRenderManager().getEntityRenderObject(target);

                net.minecraft.client.renderer.GlStateManager.pushMatrix();
                net.minecraft.client.renderer.GlStateManager.disableTexture2D();
                net.minecraft.client.renderer.GlStateManager.enableBlend();
                net.minecraft.client.renderer.GlStateManager.disableDepth();
                net.minecraft.client.renderer.GlStateManager.color(0, 0.6F, 1, 0.2F);
                net.minecraft.client.renderer.GlStateManager.doPolygonOffset(-3, -3);
                net.minecraft.client.renderer.GlStateManager.enablePolygonOffset();
                net.minecraft.client.renderer.GlStateManager.blendFunc(770, 771);
                net.minecraft.client.renderer.GlStateManager.depthMask(false);
                // draw bounding box
                net.minecraft.client.renderer.GlStateManager.disablePolygonOffset();
                net.minecraft.client.renderer.GlStateManager.depthMask(true);
                net.minecraft.client.renderer.GlStateManager.enableDepth();
                net.minecraft.client.renderer.GlStateManager.enableTexture2D();
                net.minecraft.client.renderer.GlStateManager.popMatrix();
            }
        }
    }

    // ====================== STATE MANAGEMENT ======================

    private void resetState() {
        attackCounter = 0;
        if (swapped) {
            swapped = false;
            sendSwap(mc.thePlayer.inventory.currentItem);
        }
        if ((autoblock.getValue().equals("Hypixel") || autoblock.getValue().equals("Hypixel2")) && blocking) {
            int slot = random.nextInt(9);
            while (slot == mc.thePlayer.inventory.currentItem) slot = random.nextInt(9);
            sendSwap(slot);
            swapped = true;
            unblock();
            asw++;
        } else {
            if (b3) { b3 = false; }
            asw = 0;
        }
        if (blocking) unblock();
        rotTick = 0;
    }

    @Override
    public void onDisable() {
        currentYaw = 0;
        target = null;
        lastTarget = null;
        attackCounter = 10;
        blocking = false;
        swapped = false;
        asw = 0;
        b2 = false;
        b3 = false;
        postBlock = false;
        reached = 0;
        rotTick = 0;
    }

    @Override
    public void onEnable() {
        currentYaw = 0;
        currentPitch = 0;
        target = null;
        lastTarget = null;
        lastAttackTime = 0;
        lastSwitchTime = System.currentTimeMillis();
        blocking = false;
        swapped = false;
        b2 = false;
        b3 = false;
        postBlock = false;
        attackCounter = 0;
        asw = 0;
        nextTick = 0;
        rotTick = 0;
        reached = 0;
    }
}
