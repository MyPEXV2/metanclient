package relake.common.util;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import relake.common.InstanceAccess;

@UtilityClass
public class RayTraceUtil implements InstanceAccess {
    public RayTraceResult rayTrace(double rayTraceDistance,
                                          float yaw,
                                          float pitch,
                                          Entity entity) {

        Vector3d startVec = mc.player.getEyePosition(1.0F);
        Vector3d directionVec = getVectorForRotation(pitch, yaw);

        Vector3d endVec = startVec.add(
                directionVec.x * rayTraceDistance,
                directionVec.y * rayTraceDistance,
                directionVec.z * rayTraceDistance
        );

        return mc.world.rayTraceBlocks(new RayTraceContext(
                startVec,
                endVec,
                RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE,
                entity)
        );
    }

    public boolean isViewEntity(LivingEntity target, float yaw, float pitch, float distance, boolean ignoreWalls) {
        Entity entity = mc.getRenderViewEntity();

        if (entity == null
                || mc.world == null)
            return false;

        double reachDistanceSquared = distance * distance;

        Vector3d startVec = entity.getEyePosition(1F);
        Vector3f directionVec = calculateViewVector(yaw, pitch);
        directionVec.mul(distance, distance, distance);
        Vector3d endVec = startVec.add(directionVec.getX(), directionVec.getY(), directionVec.getZ());
        AxisAlignedBB aabb = target.getBoundingBox();

        EntityRayTraceResult result = ProjectileHelper.rayTraceEntities(
                entity,
                startVec,
                endVec,
                aabb,
                (entityIn) -> !entityIn.isSpectator() && entityIn.isAlive() && entityIn == target,
                reachDistanceSquared
        );

        return result != null;
    }

    public Vector3f calculateViewVector(float yaw, float pitch) {
        float pitchRad = pitch * 0.017453292519943295F;
        float yawRad = -yaw * 0.017453292519943295F;
        float cosYaw = MathHelper.cos(yawRad);
        float sinYaw = MathHelper.sin(yawRad);
        float cosPitch = MathHelper.cos(pitchRad);
        float sinPitch = MathHelper.sin(pitchRad);

        return new Vector3f(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
    }

    public Vector3d getVectorForRotation(float pitch, float yaw) {
        float yawRadians = -yaw * ((float) Math.PI / 180) - (float) Math.PI;
        float pitchRadians = -pitch * ((float) Math.PI / 180);

        float cosYaw = MathHelper.cos(yawRadians);
        float sinYaw = MathHelper.sin(yawRadians);
        float cosPitch = -MathHelper.cos(pitchRadians);
        float sinPitch = MathHelper.sin(pitchRadians);

        return new Vector3d(sinYaw * cosPitch, sinPitch, cosYaw * cosPitch);
    }

    public boolean rayTraceSingleEntity(float yaw, float pitch, double distance, Entity entity) {
        Vector3d eyeVec = mc.player.getEyePosition(1.0F);
        Vector3d lookVec = mc.player.getVectorForRotation(pitch, yaw);
        Vector3d extendedVec = eyeVec.add(lookVec.scale(distance));

        AxisAlignedBB AABB = entity.getBoundingBox();

        return AABB.contains(eyeVec) || AABB.rayTrace(eyeVec, extendedVec).isPresent();
    }
}
