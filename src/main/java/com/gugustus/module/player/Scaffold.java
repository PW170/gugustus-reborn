package com.gugustus.module.player;

import com.gugustus.module.Category;
import com.gugustus.module.Module;
import com.gugustus.settings.BooleanSetting;
import com.gugustus.settings.ModeSetting;
import com.gugustus.settings.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Scaffold extends Module {

    private static final double[] placeOffsets = new double[]{
            0.03125, 0.09375, 0.15625, 0.21875,
            0.28125, 0.34375, 0.40625, 0.46875,
            0.53125, 0.59375, 0.65625, 0.71875,
            0.78125, 0.84375, 0.90625, 0.96875
    };

    private int rotationTick = 0;
    private int lastSlot = -1;
    private int blockCount = -1;
    private float yaw = -180.0F;
    private float pitch = 0.0F;
    private boolean canRotate = false;
    private int towerTick = 0;
    private int towerDelay = 0;
    private int stage = 0;
    private int startY = 256;
    private boolean shouldKeepY = false;
    private boolean towering = false;
    private EnumFacing targetFacing = null;
    private int safeStuckTicks = 0;
    private int safeStuckDelayTicks = 0;
    private double safePrevMotionY = 0.0;
    private double savedMotionX;
    private double savedMotionY;
    private double savedMotionZ;
    private boolean safeStuckActive = false;
    private boolean snapRotating = false;
    private boolean placedThisTick = false;
    private float lastSnapPlaceYaw = Float.NaN;
    private float lastSnapPlacePitch = Float.NaN;

    public final ModeSetting rotationMode = new ModeSetting("Rotations", new String[]{"NONE","DEFAULT","BACKWARDS","SIDEWAYS","GODBRIDGE","SMOOTH","HYPIXEL","SNAP"}, "SIDEWAYS");
    public final NumberSetting tellyStartRotMin = new NumberSetting("Telly Start Rot Min", 90, 1, 180, 1);
    public final NumberSetting tellyStartRotMax = new NumberSetting("Telly Start Rot Max", 95, 1, 180, 1);
    public final NumberSetting tellyNormalRotMin = new NumberSetting("Telly Normal Rot Min", 30, 1, 180, 1);
    public final NumberSetting tellyNormalRotMax = new NumberSetting("Telly Normal Rot Max", 35, 1, 180, 1);
    public final ModeSetting moveFix = new ModeSetting("MoveFix", new String[]{"NONE","SILENT"}, "SILENT");
    public final ModeSetting sprintMode = new ModeSetting("Sprint", new String[]{"NONE","VANILLA"}, "NONE");
    public final NumberSetting groundMotion = new NumberSetting("Ground Motion", 100, 0, 100, 1);
    public final NumberSetting airMotion = new NumberSetting("Air Motion", 100, 0, 100, 1);
    public final NumberSetting speedMotion = new NumberSetting("Speed Motion", 100, 0, 100, 1);
    public final ModeSetting tower = new ModeSetting("Tower", new String[]{"NONE","VANILLA","EXTRA","TELLY"}, "NONE");
    public final BooleanSetting hypixelTower = new BooleanSetting("Hypixel Tower", false);
    public final BooleanSetting safe = new BooleanSetting("Safe", false);
    public final NumberSetting safeStuckDelayTicksProperty = new NumberSetting("Safe Delay Ticks", 1, 1, 3, 1);
    public final ModeSetting keepY = new ModeSetting("KeepY", new String[]{"NONE","VANILLA","EXTRA","TELLY","EXTRATELLY"}, "NONE");
    public final BooleanSetting keepYonPress = new BooleanSetting("KeepY OnPress", false);
    public final BooleanSetting disableWhileJumpActive = new BooleanSetting("Disable While Jump", false);
    public final BooleanSetting multiplace = new BooleanSetting("Multiplace", true);
    public final BooleanSetting safeWalk = new BooleanSetting("Safe Walk", true);
    public final BooleanSetting swing = new BooleanSetting("Swing", true);
    public final BooleanSetting itemSpoof = new BooleanSetting("Item Spoof", false);
    public final BooleanSetting blockCounter = new BooleanSetting("Block Counter", true);
    public final BooleanSetting eagle = new BooleanSetting("Eagle", false);
    public final NumberSetting edgeDistance = new NumberSetting("Edge Distance", 0.13, 0, 0.5, 0.01);
    public final NumberSetting sneakDelay = new NumberSetting("Sneak Delay", 80, 0, 500, 5);
    public final NumberSetting blocksPerSneak = new NumberSetting("Blocks Per Sneak", 1, 1, 5, 1);

    private boolean eagleSneaking = false;
    private int eagleSneakTicks = 0;
    private long eagleLastSneakTime = 0L;
    private int eagleBlocksPlaced = 0;

    public Scaffold() {
        super("Scaffold", Category.PLAYER, 0);
        addSetting(rotationMode);
        addSetting(tellyStartRotMin);
        addSetting(tellyStartRotMax);
        addSetting(tellyNormalRotMin);
        addSetting(tellyNormalRotMax);
        addSetting(moveFix);
        addSetting(sprintMode);
        addSetting(groundMotion);
        addSetting(airMotion);
        addSetting(speedMotion);
        addSetting(tower);
        addSetting(hypixelTower);
        addSetting(safe);
        addSetting(safeStuckDelayTicksProperty);
        addSetting(keepY);
        addSetting(keepYonPress);
        addSetting(disableWhileJumpActive);
        addSetting(multiplace);
        addSetting(safeWalk);
        addSetting(swing);
        addSetting(itemSpoof);
        addSetting(blockCounter);
        addSetting(eagle);
        addSetting(edgeDistance);
        addSetting(sneakDelay);
        addSetting(blocksPerSneak);
    }

    // ====================== UTILITY HELPERS ======================

    private float quantizeAngle(float angle) {
        return (float) Math.round(angle * 1000.0F) / 1000.0F;
    }

    private float wrapAngleDiff(float a, float b) {
        return MathHelper.wrapAngleTo180_float(a - b);
    }

    private float clampAngle(float diff, float max) {
        return MathHelper.clamp_float(diff, -max, max);
    }

    private float[] getRotationsTo(double dx, double dy, double dz, float yaw, float pitch) {
        double dist = MathHelper.sqrt_double(dx * dx + dz * dz);
        float targetYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        float targetPitch = (float) (-Math.atan2(dy, dist) * 180.0 / Math.PI);
        return new float[]{targetYaw, targetPitch};
    }

    private MovingObjectPosition rayTrace(float yaw, float pitch, double dist, float partialTicks) {
        Vec3 eyes = mc.thePlayer.getPositionEyes(partialTicks);
        float radPitch = pitch * 0.017453292F;
        float radYaw = yaw * 0.017453292F;
        Vec3 lookVec = new Vec3(
                -MathHelper.sin(radYaw) * MathHelper.cos(radPitch),
                -MathHelper.sin(radPitch),
                MathHelper.cos(radYaw) * MathHelper.cos(radPitch)
        );
        Vec3 end = eyes.addVector(lookVec.xCoord * dist, lookVec.yCoord * dist, lookVec.zCoord * dist);
        return mc.theWorld.rayTraceBlocks(eyes, end, false, false, true);
    }

    private float getCurrentYaw() {
        float forward = mc.thePlayer.movementInput.moveForward;
        float strafe = mc.thePlayer.movementInput.moveStrafe;
        float yaw = mc.thePlayer.rotationYaw;
        if (forward == 0 && strafe == 0) return yaw;
        float diff = (float) Math.toDegrees(Math.atan2(strafe, forward));
        if (forward < 0) diff += 180;
        return yaw + diff;
    }

    private float getForwardValue() {
        return mc.thePlayer.movementInput.moveForward;
    }

    private float getLeftValue() {
        return mc.thePlayer.movementInput.moveStrafe;
    }

    private boolean isForwardPressed() {
        return Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
    }

    private double getSpeed() {
        return Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
    }

    private float getSpeedLevel() {
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            return mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1;
        }
        return 0;
    }

    private boolean isHoldingBlock() {
        ItemStack held = mc.thePlayer.getHeldItem();
        return held != null && held.getItem() instanceof ItemBlock;
    }

    private boolean isBlock(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemBlock;
    }

    private boolean isReplaceable(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        return block.isAir(mc.theWorld, pos) || block.isReplaceable(mc.theWorld, pos);
    }

    private boolean isInteractable(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        return block instanceof net.minecraft.block.BlockChest
                || block instanceof net.minecraft.block.BlockFurnace
                || block instanceof net.minecraft.block.BlockAnvil
                || block instanceof net.minecraft.block.BlockWorkbench
                || block instanceof net.minecraft.block.BlockDoor
                || block instanceof net.minecraft.block.BlockTrapDoor;
    }

    private boolean isSolid(Block block) {
        return block.isFullBlock() && block.getMaterial().isSolid();
    }

    private boolean isAirAbove() {
        BlockPos above = new BlockPos(MathHelper.floor_double(mc.thePlayer.posX), MathHelper.floor_double(mc.thePlayer.posY) + 2, MathHelper.floor_double(mc.thePlayer.posZ));
        return mc.theWorld.isAirBlock(above);
    }

    private boolean isAirBelow() {
        BlockPos below = new BlockPos(MathHelper.floor_double(mc.thePlayer.posX), MathHelper.floor_double(mc.thePlayer.posY) - 1, MathHelper.floor_double(mc.thePlayer.posZ));
        return mc.theWorld.isAirBlock(below);
    }

    private boolean canMove(double mx, double mz, double y) {
        AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(mx, y, mz);
        return mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty();
    }

    private Vec3 getClickVec(BlockPos pos, EnumFacing facing) {
        return new Vec3(pos.getX() + 0.5 + facing.getFrontOffsetX() * 0.5,
                pos.getY() + 0.5 + facing.getFrontOffsetY() * 0.5,
                pos.getZ() + 0.5 + facing.getFrontOffsetZ() * 0.5);
    }

    private Vec3 getHitVec(BlockPos pos, EnumFacing facing, float yaw, float pitch) {
        double x = pos.getX() + 0.5 + facing.getFrontOffsetX() * 0.5;
        double y = pos.getY() + 0.5 + facing.getFrontOffsetY() * 0.5;
        double z = pos.getZ() + 0.5 + facing.getFrontOffsetZ() * 0.5;
        return new Vec3(x, y, z);
    }

    private void setSpeed(double speed, float yaw) {
        double rad = Math.toRadians(yaw);
        mc.thePlayer.motionX = -Math.sin(rad) * speed;
        mc.thePlayer.motionZ = Math.cos(rad) * speed;
    }

    private void fixStrafe(float rotationYaw) {
        float forward = getForwardValue();
        float strafe = getLeftValue();
        if (forward == 0 && strafe == 0) return;
        float diff = (float) Math.toDegrees(Math.atan2(strafe, forward));
        if (forward < 0) diff += 180;
        float targetYaw = rotationYaw + diff;
        speedStrafe(targetYaw);
    }

    private void speedStrafe(float targetYaw) {
        double speed = getSpeed();
        double rad = Math.toRadians(targetYaw);
        mc.thePlayer.motionX = -Math.sin(rad) * speed;
        mc.thePlayer.motionZ = Math.cos(rad) * speed;
    }

    private double randomDouble(double min, double max) {
        return min + Math.random() * (max - min);
    }

    private float randomFloat(float min, float max) {
        return min + (float) Math.random() * (max - min);
    }

    // ====================== CORE LOGIC ======================

    private boolean shouldStopSprint() {
        if (isTowering()) return false;
        boolean stageActive = keepY.getValue().equals("VANILLA") || keepY.getValue().equals("EXTRA") || keepY.getValue().equals("EXTRATELLY");
        return (!stageActive || stage <= 0) && sprintMode.getValue().equals("NONE");
    }

    private boolean canPlaceCheck() {
        return true;
    }

    private EnumFacing getBestFacing(BlockPos blockPos1, BlockPos blockPos3) {
        double offset = 0;
        EnumFacing enumFacing = null;
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing != EnumFacing.DOWN) {
                BlockPos pos = blockPos1.offset(facing);
                if (pos.getY() <= blockPos3.getY()) {
                    double distance = pos.distanceSqToCenter(blockPos3.getX() + 0.5, blockPos3.getY() + 0.5, blockPos3.getZ() + 0.5);
                    if (enumFacing == null || distance < offset || distance == offset && facing == EnumFacing.UP) {
                        offset = distance;
                        enumFacing = facing;
                    }
                }
            }
        }
        return enumFacing;
    }

    private BlockData getBlockData() {
        int startY = MathHelper.floor_double(mc.thePlayer.posY);
        BlockPos targetPos = new BlockPos(
                MathHelper.floor_double(mc.thePlayer.posX),
                (stage != 0 && !shouldKeepY ? Math.min(startY, this.startY) : startY) - 1,
                MathHelper.floor_double(mc.thePlayer.posZ)
        );
        if (!isReplaceable(targetPos)) return null;

        ArrayList<BlockPos> positions = new ArrayList<>();
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 0; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos pos = targetPos.add(x, y, z);
                    if (!isReplaceable(pos)
                            && !isInteractable(pos)
                            && mc.thePlayer.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                            <= mc.playerController.getBlockReachDistance()
                            && (stage == 0 || shouldKeepY || pos.getY() < this.startY)) {
                        for (EnumFacing facing : EnumFacing.VALUES) {
                            if (facing != EnumFacing.DOWN) {
                                BlockPos neighbor = pos.offset(facing);
                                if (isReplaceable(neighbor)) {
                                    positions.add(pos);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (positions.isEmpty()) return null;

        positions.sort(Comparator.comparingDouble(o ->
                o.distanceSqToCenter(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5)));
        BlockPos blockPos = positions.get(0);
        EnumFacing facing = getBestFacing(blockPos, targetPos);
        return facing == null ? null : new BlockData(blockPos, facing);
    }

    private void place(BlockPos blockPos, EnumFacing enumFacing, Vec3 vec3) {
        if (isHoldingBlock() && blockCount > 0) {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), blockPos, enumFacing, vec3)) {
                if (mc.playerController.getCurrentGameType() != WorldSettings.GameType.CREATIVE) {
                    blockCount--;
                }
                placedThisTick = true;
                eagleBlocksPlaced++;
                if (swing.getValue()) {
                    mc.thePlayer.swingItem();
                } else {
                    mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                }
            }
        }
    }

    private MovingObjectPosition getPlacementMop(BlockData blockData, float yaw, float pitch) {
        MovingObjectPosition mop = rayTrace(yaw, pitch, mc.playerController.getBlockReachDistance(), 1.0F);
        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                || !mop.getBlockPos().equals(blockData.blockPos())
                || mop.sideHit != blockData.facing()) {
            return null;
        }
        return mop;
    }

    private boolean isDuplicateSnapRotation(float yaw, float pitch) {
        return !Float.isNaN(lastSnapPlaceYaw)
                && Math.abs(wrapAngleDiff(yaw, lastSnapPlaceYaw)) < 0.35F;
    }

    private float[] getSnapRotation(BlockData blockData, float yaw, float pitch) {
        float baseYaw = quantizeAngle(yaw);
        float basePitch = quantizeAngle(MathHelper.clamp_float(pitch, -90, 90));

        if (!isDuplicateSnapRotation(baseYaw, basePitch)) {
            return new float[]{baseYaw, basePitch};
        }

        for (int i = 0; i < 24; i++) {
            float yawStep = 0.35F + 0.075F * (i / 2);
            float pitchStep = 0.025F + 0.01F * (i / 3);
            float testYaw = quantizeAngle(baseYaw + (i % 2 == 0 ? yawStep : -yawStep));
            float testPitch = quantizeAngle(MathHelper.clamp_float(basePitch + (i % 4 < 2 ? pitchStep : -pitchStep), -90, 90));

            if (!isDuplicateSnapRotation(testYaw, testPitch) && getPlacementMop(blockData, testYaw, testPitch) != null) {
                return new float[]{testYaw, testPitch};
            }
        }
        return null;
    }

    private void rememberSnapRotation() {
        lastSnapPlaceYaw = yaw;
        lastSnapPlacePitch = pitch;
    }

    private EnumFacing yawToFacing(float yaw) {
        if (yaw < -135 || yaw > 135) return EnumFacing.NORTH;
        if (yaw < -45) return EnumFacing.EAST;
        if (yaw < 45) return EnumFacing.SOUTH;
        return EnumFacing.WEST;
    }

    private double distanceToEdge(EnumFacing facing) {
        switch (facing) {
            case NORTH: return mc.thePlayer.posZ - Math.floor(mc.thePlayer.posZ);
            case EAST:  return Math.ceil(mc.thePlayer.posX) - mc.thePlayer.posX;
            case SOUTH: return Math.ceil(mc.thePlayer.posZ) - mc.thePlayer.posZ;
            case WEST:
            default:    return mc.thePlayer.posX - Math.floor(mc.thePlayer.posX);
        }
    }

    private boolean isNearEdge() {
        if (!mc.thePlayer.onGround) return false;
        double fracX = mc.thePlayer.posX - Math.floor(mc.thePlayer.posX);
        double fracZ = mc.thePlayer.posZ - Math.floor(mc.thePlayer.posZ);
        double threshold = edgeDistance.getValue();
        double minDist = Math.min(Math.min(fracX, 1 - fracX), Math.min(fracZ, 1 - fracZ));
        return minDist <= threshold;
    }

    private boolean isDiagonal(float yaw) {
        float absYaw = Math.abs(yaw % 90);
        return absYaw > 20 && absYaw < 70;
    }

    private boolean isTowering() {
        if (mc.thePlayer.onGround && isForwardPressed() && !isAirAbove()) {
            boolean keepYActive = keepY.getValue().equals("TELLY") || keepY.getValue().equals("EXTRATELLY");
            boolean towerActive = tower.getValue().equals("TELLY");
            return keepYActive && stage > 0 || towerActive && mc.gameSettings.keyBindJump.isKeyDown();
        }
        return false;
    }

    private boolean shouldSneakEagle() {
        if (!eagle.getValue() || !mc.thePlayer.onGround) return false;
        if (eagleBlocksPlaced < blocksPerSneak.getValueInt()) return false;
        if (System.currentTimeMillis() - eagleLastSneakTime < (long) sneakDelay.getValueInt()) return false;
        return isNearEdge();
    }

    private void updateEagle() {
        if (!eagle.getValue()) {
            eagleSneaking = false;
            eagleSneakTicks = 0;
            return;
        }
        if (eagleSneakTicks > 0) {
            eagleSneakTicks--;
            if (eagleSneakTicks == 0) eagleSneaking = false;
            return;
        }
        if (shouldSneakEagle()) {
            eagleSneaking = true;
            eagleSneakTicks = 2;
            eagleLastSneakTime = System.currentTimeMillis();
            eagleBlocksPlaced = 0;
        }
    }

    private float getSpeedFactor() {
        if (!mc.thePlayer.onGround) return (float) airMotion.getValue() / 100;
        return getSpeedLevel() > 0 ? (float) speedMotion.getValue() / 100 : (float) groundMotion.getValue() / 100;
    }

    private double getRandomOffset() {
        return 0.2155 - randomDouble(0.0001, 0.0009);
    }

    // ====================== MAIN TICK (UpdateEvent PRE) ======================

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!isEnabled()) return;

        placedThisTick = false;

        // Safe stuck delay countdown
        if (safeStuckDelayTicks > 0) {
            safeStuckDelayTicks--;
            if (safeStuckDelayTicks <= 0) safeStuckTicks = 1;
        }

        // Safe stuck freeze
        if (safeStuckTicks > 0) {
            if (!safeStuckActive) {
                savedMotionX = mc.thePlayer.motionX;
                savedMotionY = mc.thePlayer.motionY;
                savedMotionZ = mc.thePlayer.motionZ;
                safeStuckActive = true;
            }
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionZ = 0;
        } else if (safeStuckActive) {
            mc.thePlayer.motionX = savedMotionX;
            mc.thePlayer.motionY = savedMotionY;
            mc.thePlayer.motionZ = savedMotionZ;
            safeStuckActive = false;
        }

        if (rotationTick > 0) rotationTick--;

        updateEagle();

        // Hypixel tower
        if (hypixelTower.getValue() && mc.thePlayer.motionY <= 0
                && Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ) <= 0.02
                && mc.thePlayer.motionY >= -0.09
                && !(Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())
                  || Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())
                  || Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode())
                  || Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()))
                && Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            mc.thePlayer.motionY = -0.38;
        }

        // Stage management
        if (mc.thePlayer.onGround) {
            if (stage > 0) stage--;
            if (stage < 0) stage++;
            if (stage == 0 && !keepY.getValue().equals("NONE")
                    && (!keepYonPress.getValue() || mc.thePlayer.isUsingItem())
                    && (!disableWhileJumpActive.getValue() || !mc.thePlayer.isPotionActive(Potion.jump))
                    && !mc.gameSettings.keyBindJump.isKeyDown()) {
                stage = 1;
            }
            startY = shouldKeepY ? startY : MathHelper.floor_double(mc.thePlayer.posY);
            shouldKeepY = false;
            towering = false;
        }

        if (!canPlaceCheck()) return;

        // Item management
        ItemStack stack = mc.thePlayer.getHeldItem();
        int count = isBlock(stack) ? stack.stackSize : 0;
        blockCount = Math.min(blockCount, count);
        if (blockCount <= 0) {
            int slot = mc.thePlayer.inventory.currentItem;
            if (blockCount == 0) slot--;
            for (int i = slot; i > slot - 9; i--) {
                int hotbarSlot = ((i % 9) + 9) % 9;
                ItemStack candidate = mc.thePlayer.inventory.getStackInSlot(hotbarSlot);
                if (isBlock(candidate)) {
                    mc.thePlayer.inventory.currentItem = hotbarSlot;
                    blockCount = candidate.stackSize;
                    break;
                }
            }
        }

        // Rotation calculation
        float currentYaw = getCurrentYaw();
        float yawDiffTo180 = wrapAngleDiff(currentYaw - 180, mc.thePlayer.rotationYaw);
        float diagonalYaw = isDiagonal(currentYaw)
                ? yawDiffTo180
                : wrapAngleDiff(currentYaw - 135 * ((currentYaw + 180) % 90 < 45 ? 1 : -1), mc.thePlayer.rotationYaw);

        boolean snapMode = rotationMode.getValue().equals("SNAP");
        snapRotating = false;

        if (!canRotate) {
            switch (rotationMode.getValue()) {
                case "DEFAULT":
                case "SIDEWAYS":
                case "HYPIXEL":
                    if (yaw == -180 && pitch == 0) {
                        yaw = quantizeAngle(diagonalYaw);
                        pitch = quantizeAngle(85);
                    } else {
                        yaw = quantizeAngle(diagonalYaw);
                    }
                    break;
                case "BACKWARDS":
                    if (yaw == -180 && pitch == 0) {
                        yaw = quantizeAngle(yawDiffTo180);
                        pitch = quantizeAngle(85);
                    } else {
                        yaw = quantizeAngle(yawDiffTo180);
                    }
                    break;
                case "GODBRIDGE": {
                    float roundedYaw = Math.round(currentYaw / 45) * 45;
                    yaw = quantizeAngle(roundedYaw);
                    if (pitch == 0 || !canRotate) {
                        pitch = quantizeAngle(79.3F);
                    }
                    break;
                }
                case "SMOOTH":
                    if (yaw == -180 && pitch == 0) {
                        yaw = quantizeAngle(diagonalYaw);
                        pitch = quantizeAngle(85);
                    } else {
                        float targetYawRot = isDiagonal(currentYaw) ? diagonalYaw : yawDiffTo180;
                        float yawDiff = wrapAngleDiff(targetYawRot, yaw);
                        float pitchDiff = wrapAngleDiff(85, pitch);
                        float yawTol = rotationTick >= 2
                                ? randomFloat((float) tellyStartRotMin.getValue(), (float) tellyStartRotMax.getValue())
                                : randomFloat((float) tellyNormalRotMin.getValue(), (float) tellyNormalRotMax.getValue());
                        float pitchTol = rotationTick >= 2
                                ? randomFloat((float) tellyStartRotMin.getValue(), (float) tellyStartRotMax.getValue())
                                : randomFloat((float) tellyNormalRotMin.getValue(), (float) tellyNormalRotMax.getValue());
                        yaw = quantizeAngle(yaw + clampAngle(yawDiff, yawTol));
                        pitch = quantizeAngle(pitch + clampAngle(pitchDiff, pitchTol));
                    }
                    break;
                case "SNAP":
                    yaw = quantizeAngle(yawDiffTo180);
                    pitch = quantizeAngle(85);
                    break;
            }
        }

        // Block data scan & hit vector calculation
        BlockData blockData = getBlockData();
        Vec3 hitVec = null;

        if (blockData != null) {
            double[] x = placeOffsets, y = placeOffsets, z = placeOffsets;
            switch (blockData.facing()) {
                case NORTH: z = new double[]{0}; break;
                case EAST:  x = new double[]{1}; break;
                case SOUTH: z = new double[]{1}; break;
                case WEST:  x = new double[]{0}; break;
                case DOWN:  y = new double[]{0}; break;
                case UP:    y = new double[]{1}; break;
            }

            float bestYaw = -180, bestPitch = 0, bestDiff = 0;
            for (double dx : x) {
                for (double dy : y) {
                    for (double dz : z) {
                        double relX = blockData.blockPos().getX() + dx - mc.thePlayer.posX;
                        double relY = blockData.blockPos().getY() + dy - mc.thePlayer.posY - mc.thePlayer.getEyeHeight();
                        double relZ = blockData.blockPos().getZ() + dz - mc.thePlayer.posZ;
                        float baseYaw = wrapAngleDiff(yaw, mc.thePlayer.rotationYaw);
                        float[] rotations = getRotationsTo(relX, relY, relZ, baseYaw, pitch);
                        MovingObjectPosition mop = rayTrace(rotations[0], rotations[1], mc.playerController.getBlockReachDistance(), 1);
                        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                                && mop.getBlockPos().equals(blockData.blockPos())
                                && mop.sideHit == blockData.facing()) {
                            float totalDiff = Math.abs(rotations[0] - baseYaw) + Math.abs(rotations[1] - pitch);
                            if (bestYaw == -180 && bestPitch == 0 || totalDiff < bestDiff) {
                                bestYaw = rotations[0];
                                bestPitch = rotations[1];
                                bestDiff = totalDiff;
                                hitVec = mop.hitVec;
                            }
                        }
                    }
                }
            }
            if (bestYaw != -180 || bestPitch != 0) {
                yaw = bestYaw;
                pitch = bestPitch;
                canRotate = true;
            }
        }

        // Snap mode rotation
        boolean towerRotating = towering || isTowering();
        boolean snapAlreadyLooking = false;
        boolean snapCanPlace = true;

        if (snapMode && !towerRotating && blockData != null) {
            MovingObjectPosition currentMop = getPlacementMop(blockData, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
            if (currentMop != null) {
                float[] snapRotation = getSnapRotation(blockData, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
                if (snapRotation == null) {
                    snapCanPlace = false;
                    hitVec = null;
                } else {
                    yaw = snapRotation[0];
                    pitch = snapRotation[1];
                    canRotate = true;
                    MovingObjectPosition snapMop = getPlacementMop(blockData, yaw, pitch);
                    hitVec = snapMop != null ? snapMop.hitVec : currentMop.hitVec;
                    snapRotating = true;
                    if (rotationTick > 1) rotationTick = 1;
                }
            } else if (hitVec != null && canRotate) {
                float[] snapRotation = getSnapRotation(blockData, yaw, pitch);
                if (snapRotation == null) {
                    snapCanPlace = false;
                    hitVec = null;
                } else {
                    yaw = snapRotation[0];
                    pitch = snapRotation[1];
                    MovingObjectPosition snapMop = getPlacementMop(blockData, yaw, pitch);
                    if (snapMop != null) hitVec = snapMop.hitVec;
                    snapRotating = true;
                    if (rotationTick > 1) rotationTick = 1;
                }
            }
        }

        // Rotation re-alignment
        if (canRotate && isForwardPressed() && Math.abs(wrapAngleDiff(yawDiffTo180, yaw)) < 90) {
            switch (rotationMode.getValue()) {
                case "BACKWARDS": yaw = quantizeAngle(yawDiffTo180); break;
                case "SIDEWAYS":
                case "HYPIXEL":   yaw = quantizeAngle(diagonalYaw); break;
            }
        }

        // Internal yaw/pitch used for ray-tracing only — player camera stays free

        // Block placement
        if (blockData != null && hitVec != null && snapCanPlace && (rotationTick <= 0 || snapAlreadyLooking)) {
            place(blockData.blockPos(), blockData.facing(), hitVec);
            if (snapMode) rememberSnapRotation();

            if (multiplace.getValue() && !snapMode) {
                for (int i = 0; i < 3; i++) {
                    blockData = getBlockData();
                    if (blockData == null) break;
                    MovingObjectPosition mop = rayTrace(yaw, pitch, mc.playerController.getBlockReachDistance(), 1);
                    if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                            && mop.getBlockPos().equals(blockData.blockPos())
                            && mop.sideHit == blockData.facing()) {
                        place(blockData.blockPos(), blockData.facing(), mop.hitVec);
                    } else {
                        hitVec = getClickVec(blockData.blockPos(), blockData.facing());
                        double dx = hitVec.xCoord - mc.thePlayer.posX;
                        double dy = hitVec.yCoord - mc.thePlayer.posY - mc.thePlayer.getEyeHeight();
                        double dz = hitVec.zCoord - mc.thePlayer.posZ;
                        float[] rotations = getRotationsTo(dx, dy, dz, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
                        if (Math.abs(rotations[0] - yaw) >= 120 || Math.abs(rotations[1] - pitch) >= 60) break;
                        mop = rayTrace(rotations[0], rotations[1], mc.playerController.getBlockReachDistance(), 1);
                        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                                || !mop.getBlockPos().equals(blockData.blockPos())
                                || mop.sideHit != blockData.facing()) break;
                        place(blockData.blockPos(), blockData.facing(), mop.hitVec);
                    }
                }
            }
        }

        // KeepY extra / target facing placement
        if (targetFacing != null) {
            if (rotationTick <= 0 && !placedThisTick) {
                int px = MathHelper.floor_double(mc.thePlayer.posX);
                int py = MathHelper.floor_double(mc.thePlayer.posY);
                int pz = MathHelper.floor_double(mc.thePlayer.posZ);
                BlockPos belowPlayer = new BlockPos(px, py - 1, pz);
                hitVec = getHitVec(belowPlayer, targetFacing, yaw, pitch);
                place(belowPlayer, targetFacing, hitVec);
            }
            targetFacing = null;
        } else if ((keepY.getValue().equals("EXTRA") || keepY.getValue().equals("EXTRATELLY"))
                && stage > 0 && !mc.thePlayer.onGround) {
            int nextBlockY = MathHelper.floor_double(mc.thePlayer.posY + mc.thePlayer.motionY);
            if (nextBlockY <= startY && mc.thePlayer.posY > startY + 1) {
                shouldKeepY = true;
                blockData = getBlockData();
                if (blockData != null && rotationTick <= 0 && !placedThisTick) {
                    MovingObjectPosition mop = getPlacementMop(blockData, yaw, pitch);
                    if (mop != null) place(blockData.blockPos(), blockData.facing(), mop.hitVec);
                }
            }
        }
    }

    // ====================== STRAFE (tower motion) ======================

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.entity != mc.thePlayer) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!isEnabled()) return;

        // Safe stuck freezing
        if (safeStuckTicks > 0) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionZ = 0;
            safeStuckTicks--;
            return;
        }

        // Tower strafe
        if (!mc.thePlayer.isCollidedHorizontally && mc.thePlayer.hurtTime <= 5
                && !mc.thePlayer.isPotionActive(Potion.jump)
                && mc.gameSettings.keyBindJump.isKeyDown()
                && isHoldingBlock()) {

            int yState = (int) (mc.thePlayer.posY % 1.0 * 100);
            switch (tower.getValue()) {
                case "VANILLA":
                    switch (towerTick) {
                        case 0:
                            if (mc.thePlayer.onGround) { towerTick = 1; mc.thePlayer.motionY = -0.0784000015258789; }
                            return;
                        case 1:
                            if (yState == 0 && isAirBelow()) {
                                startY = MathHelper.floor_double(mc.thePlayer.posY);
                                towerTick = 2;
                                mc.thePlayer.motionY = 0.42F;
                                if (isForwardPressed()) setSpeed(getSpeed(), getCurrentYaw());
                                else { setSpeed(0, 0); mc.thePlayer.motionX = 0; mc.thePlayer.motionZ = 0; }
                                return;
                            } else { towerTick = 0; return; }
                        case 2:
                            towerTick = 3;
                            mc.thePlayer.motionY = 0.75 - mc.thePlayer.posY % 1.0;
                            return;
                        case 3:
                            towerTick = 1;
                            mc.thePlayer.motionY = 1.0 - mc.thePlayer.posY % 1.0;
                            return;
                        default: towerTick = 0; return;
                    }
                case "EXTRA":
                    switch (towerTick) {
                        case 0:
                            if (mc.thePlayer.onGround) { towerTick = 1; mc.thePlayer.motionY = -0.0784000015258789; }
                            return;
                        case 1:
                            if (yState == 0 && isAirBelow()) {
                                startY = MathHelper.floor_double(mc.thePlayer.posY);
                                if (!isForwardPressed()) {
                                    towerDelay = 2;
                                    setSpeed(0, 0);
                                    mc.thePlayer.motionX = 0;
                                    mc.thePlayer.motionZ = 0;
                                    EnumFacing facing = yawToFacing(wrapAngleDiff(yaw - 180, 0));
                                    double distance = distanceToEdge(facing);
                                    if (distance > 0.1) {
                                        if (mc.thePlayer.onGround) {
                                            Vec3i dir = facing.getDirectionVec();
                                            double offset = Math.min(getRandomOffset(), distance - 0.05);
                                            double jitter = randomDouble(0.02, 0.03);
                                            AxisAlignedBB nextBox = mc.thePlayer.getEntityBoundingBox()
                                                    .offset(dir.getX() * (offset - jitter), 0, dir.getZ() * (offset - jitter));
                                            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, nextBox).isEmpty()) {
                                                mc.thePlayer.motionY = -0.0784000015258789;
                                                mc.thePlayer.setPosition(
                                                        nextBox.minX + (nextBox.maxX - nextBox.minX) / 2,
                                                        nextBox.minY,
                                                        nextBox.minZ + (nextBox.maxZ - nextBox.minZ) / 2);
                                            }
                                            return;
                                        }
                                    } else {
                                        towerTick = 2;
                                        targetFacing = facing;
                                        mc.thePlayer.motionY = 0.42F;
                                    }
                                    return;
                                } else {
                                    towerTick = 2;
                                    towerDelay++;
                                    mc.thePlayer.motionY = 0.42F;
                                    setSpeed(getSpeed(), getCurrentYaw());
                                    return;
                                }
                            } else { towerTick = 0; towerDelay = 0; return; }
                        case 2:
                            towerTick = 3;
                            mc.thePlayer.motionY -= randomDouble(0.00101, 0.00109);
                            return;
                        case 3:
                            if (towerDelay >= 4) { towerTick = 4; towerDelay = 0; }
                            else { towerTick = 1; mc.thePlayer.motionY = 1.0 - mc.thePlayer.posY % 1.0; }
                            return;
                        case 4:
                            towerTick = 5;
                            return;
                        case 5:
                            if (!isAirBelow()) towerTick = 0;
                            else {
                                towerTick = 1;
                                mc.thePlayer.motionY -= 0.08;
                                mc.thePlayer.motionY *= 0.98;
                                mc.thePlayer.motionY -= 0.08;
                                mc.thePlayer.motionY *= 0.98;
                            }
                            return;
                        default: towerTick = 0; towerDelay = 0; return;
                    }
                default:
                    towerTick = 0;
                    towerDelay = 0;
            }
        } else {
            towerTick = 0;
            towerDelay = 0;
        }

        // Speed modification
        float speed = getSpeedFactor();
        if (speed != 1) {
            if (mc.thePlayer.movementInput.moveForward != 0 && mc.thePlayer.movementInput.moveStrafe != 0) {
                mc.thePlayer.movementInput.moveForward *= (1 / (float) Math.sqrt(2));
                mc.thePlayer.movementInput.moveStrafe *= (1 / (float) Math.sqrt(2));
            }
            mc.thePlayer.movementInput.moveForward *= speed;
            mc.thePlayer.movementInput.moveStrafe *= speed;
        }

        // Sprint stop
        if (shouldStopSprint()) mc.thePlayer.setSprinting(false);

        // Safe stuck detection (TELLY)
        if (safe.getValue() && tower.getValue().equals("TELLY") && mc.gameSettings.keyBindJump.isKeyDown()) {
            float moveYaw = getCurrentYaw();
            if (isDiagonal(moveYaw) && !mc.thePlayer.onGround) {
                double motionY = mc.thePlayer.motionY;
                if (safePrevMotionY > 0 && motionY <= 0) {
                    double motionXZ = Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
                    double bps = motionXZ * 20;
                    if (safeStuckDelayTicks <= 0 && safeStuckTicks <= 0 && bps >= 4.67) {
                        safeStuckDelayTicks = safeStuckDelayTicksProperty.getValueInt();
                    }
                }
                safePrevMotionY = motionY;
            } else {
                safePrevMotionY = mc.thePlayer.motionY;
            }
        } else {
            safePrevMotionY = mc.thePlayer.motionY;
        }

        // Move fix handling
        if (moveFix.getValue().equals("SILENT")) {
            if (isForwardPressed()) {
                fixStrafe(mc.thePlayer.rotationYaw);
            }
        }

        // KeepY auto-jump
        if (mc.thePlayer.onGround && stage > 0 && isForwardPressed()) {
            mc.thePlayer.movementInput.jump = true;
        }

        // Eagle sneak
        if (eagleSneaking && !mc.thePlayer.movementInput.sneak) {
            mc.thePlayer.movementInput.sneak = true;
            mc.thePlayer.movementInput.moveForward *= 0.3F;
            mc.thePlayer.movementInput.moveStrafe *= 0.3F;
        }

        // Safe walk
        if (safeWalk.getValue() && mc.thePlayer.onGround && mc.thePlayer.motionY <= 0
                && canMove(mc.thePlayer.motionX, mc.thePlayer.motionZ, -1)) {
            mc.thePlayer.movementInput.sneak = true;
        }

        // Cancel left/right click on blocks
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            mc.objectMouseOver = null;
        }

        // Item spoof slot tracking
        if (itemSpoof.getValue()) {
            int currentSlot = mc.thePlayer.inventory.currentItem;
            if (lastSlot != -1 && currentSlot != lastSlot) {
                mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(lastSlot));
            }
        }
    }

    // ====================== BLOCK COUNTER HUD ======================

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (!isEnabled() || !blockCounter.getValue() || mc.thePlayer == null) return;

        int count = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.stackSize > 0) {
                Item item = stack.getItem();
                if (item instanceof ItemBlock) {
                    Block block = ((ItemBlock) item).getBlock();
                    if (!isInteractable(new BlockPos(0, 0, 0)) && isSolid(block)) {
                        count += stack.stackSize;
                    }
                }
            }
        }

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ScaledResolution sr = new ScaledResolution(mc);
        String text = String.format("%d block%s left", count, count != 1 ? "s" : "");
        int color = (count > 0 ? Color.WHITE.getRGB() : new Color(255, 85, 85).getRGB()) | -1090519040;
        mc.fontRendererObj.drawString(
                text,
                sr.getScaledWidth() / 2 + mc.fontRendererObj.FONT_HEIGHT * 2,
                sr.getScaledHeight() / 2 - mc.fontRendererObj.FONT_HEIGHT / 2 + 1,
                color, true);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    // ====================== ENABLE / DISABLE ======================

    @Override
    public void onEnable() {
        if (mc.thePlayer != null) lastSlot = mc.thePlayer.inventory.currentItem;
        else lastSlot = -1;
        blockCount = -1;
        rotationTick = 3;
        yaw = -180;
        pitch = 0;
        canRotate = false;
        towerTick = 0;
        towerDelay = 0;
        towering = false;
        safeStuckTicks = 0;
        safeStuckDelayTicks = 0;
        safePrevMotionY = 0;
        safeStuckActive = false;
        eagleSneaking = false;
        eagleSneakTicks = 0;
        eagleBlocksPlaced = 0;
        eagleLastSneakTime = 0;
        snapRotating = false;
        lastSnapPlaceYaw = Float.NaN;
        lastSnapPlacePitch = Float.NaN;
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null && lastSlot != -1) {
            mc.thePlayer.inventory.currentItem = lastSlot;
        }
        if (safeStuckActive && mc.thePlayer != null) {
            mc.thePlayer.motionX = savedMotionX;
            mc.thePlayer.motionY = savedMotionY;
            mc.thePlayer.motionZ = savedMotionZ;
        }
        safeStuckTicks = 0;
        safeStuckDelayTicks = 0;
        safePrevMotionY = 0;
        safeStuckActive = false;
        eagleSneaking = false;
        eagleSneakTicks = 0;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public int getSlot() {
        return lastSlot;
    }

    public static class BlockData {
        private final BlockPos blockPos;
        private final EnumFacing facing;
        public BlockData(BlockPos blockPos, EnumFacing enumFacing) {
            this.blockPos = blockPos;
            this.facing = enumFacing;
        }
        public BlockPos blockPos() { return blockPos; }
        public EnumFacing facing() { return facing; }
    }
}
