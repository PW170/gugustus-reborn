package com.gugustus.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

public class RotationUtil {

    public static float[] getRotations(EntityLivingBase entity) {
        double diffX = entity.posX - mc.thePlayer.posX;
        double diffZ = entity.posZ - mc.thePlayer.posZ;
        double diffY;

        if (entity instanceof EntityLivingBase) {
            EntityLivingBase livingBase = entity;
            diffY = livingBase.posY + livingBase.getEyeHeight() - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        } else {
            diffY = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        }

        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-Math.atan2(diffY, dist) * 180.0 / Math.PI);
        return new float[]{yaw, pitch};
    }

    public static float smoothRotation(float current, float target, float smoothness) {
        float delta = MathHelper.wrapAngleTo180_float(target - current);
        if (delta > smoothness) delta = smoothness;
        if (delta < -smoothness) delta = -smoothness;
        return current + delta;
    }

    public static float getAngleDifference(float current, float target) {
        return MathHelper.wrapAngleTo180_float(target - current);
    }

    private static final net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
}
