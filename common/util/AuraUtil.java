package relake.common.util;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import relake.common.InstanceAccess;

@UtilityClass
public class AuraUtil implements InstanceAccess {
    public Vector3d getClosestVec(Vector3d vec, AxisAlignedBB AABB) {
        return new Vector3d(
                MathHelper.clamp(vec.getX(), AABB.minX, AABB.maxX),
                MathHelper.clamp(vec.getY(), AABB.minY, AABB.maxY),
                MathHelper.clamp(vec.getZ(), AABB.minZ, AABB.maxZ)
        );
    }

    public Vector3d getClosestVec(Vector3d vec, Entity entity) {
        return getClosestVec(vec, entity.getBoundingBox());
    }

    public Vector3d getClosestVec(Entity entity) {
        Vector3d eyePosVec = mc.player.getEyePosition(mc.getRenderPartialTicks());

        return getClosestVec(eyePosVec, entity).subtract(eyePosVec);
    }

    public double getStrictDistance(Entity entity) {
        return getClosestVec(entity).length();
    }

    private Vector3d calculateVector(LivingEntity target) {
        double yOffset = MathHelper.clamp(mc.player.getPosYEye() - target.getPosYEye(), 0.2, target.getEyeHeight());
        return target.getPositionVec().add(0, yOffset, 0);
    }

    public Vector3d getBestVec(final Vector3d pos, final AxisAlignedBB AABB) {
        double lastDistance = Double.MAX_VALUE;
        Vector3d vec = null;

        final double xWidth = AABB.maxX - AABB.minX;
        final double zWidth = AABB.maxZ - AABB.minZ;
        final double height = AABB.maxY - AABB.minY;

        for (float x = 0F; x < 1F; x += 0.1F) {
            for (float y = 0F; y < 1F; y += 0.1F) {
                for (float z = 0F; z < 1F; z += 0.1F) {

                    final Vector3d hitVec = new Vector3d(
                            AABB.minX + xWidth * x,
                            AABB.minY + height * y,
                            AABB.minZ + zWidth * z
                    );

                    final double distance = pos.distanceTo(hitVec);

                    if (isHitBoxNotVisible(hitVec) && distance < lastDistance) {
                        vec = hitVec;
                        lastDistance = distance;
                    }
                }
            }
        }

        return vec;
    }

    public boolean isHitBoxNotVisible(final Vector3d vec) {
        final RayTraceContext rayTraceContext = new RayTraceContext(
                mc.player.getEyePosition(mc.getRenderPartialTicks()),
                vec,
                RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE,
                mc.player
        );
        final BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);
        return blockHitResult.getType() == RayTraceResult.Type.MISS;
    }

    public Vector3d getVector(LivingEntity target) {
        double wHalf = target.getWidth() / 3D;
        double yExpand = MathHelper.clamp(target.getPosYEye() - target.getPosY(), 0, target.getHeight());
        double xExpand = MathHelper.clamp(mc.player.getPosX() - target.getPosX(), -wHalf, wHalf);
        double zExpand = MathHelper.clamp(mc.player.getPosZ() - target.getPosZ(), -wHalf, wHalf);
        return new Vector3d(
                target.getPosX() - mc.player.getPosX() + xExpand,
                target.getPosY() - mc.player.getPosYEye() + yExpand,
                target.getPosZ() - mc.player.getPosZ() + zExpand
        );
    }
}
