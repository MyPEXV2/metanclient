package relake.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.*;
import org.joml.Vector2d;
import relake.Client;
import relake.common.InstanceAccess;

import static relake.common.util.MathUtil.getEntityPos;

public class ProjectionUtil implements InstanceAccess {
    public static Vector4f getEntity2DPosition(Matrix4f matrix4f, Entity entity) {
        float width = entity.getWidth() / 1.5F;
        float height = entity.getHeight() * 1.06F;

        Vector3d pos = getEntityPos(entity, mc.getRenderPartialTicks());

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                pos.x - width,
                pos.y,
                pos.z - width,
                pos.x + width,
                pos.y + height,
                pos.z + width
        );

        Vector3f[] vectors = new Vector3f[]{
                new Vector3f((float) axisAlignedBB.minX, (float) axisAlignedBB.minY, (float) axisAlignedBB.minZ),
                new Vector3f((float) axisAlignedBB.minX, (float) axisAlignedBB.maxY, (float) axisAlignedBB.minZ),
                new Vector3f((float) axisAlignedBB.maxX, (float) axisAlignedBB.minY, (float) axisAlignedBB.minZ),
                new Vector3f((float) axisAlignedBB.maxX, (float) axisAlignedBB.maxY, (float) axisAlignedBB.minZ),
                new Vector3f((float) axisAlignedBB.minX, (float) axisAlignedBB.minY, (float) axisAlignedBB.maxZ),
                new Vector3f((float) axisAlignedBB.minX, (float) axisAlignedBB.maxY, (float) axisAlignedBB.maxZ),
                new Vector3f((float) axisAlignedBB.maxX, (float) axisAlignedBB.minY, (float) axisAlignedBB.maxZ),
                new Vector3f((float) axisAlignedBB.maxX, (float) axisAlignedBB.maxY, (float) axisAlignedBB.maxZ)
        };

        Vector4f position = new Vector4f(Float.MAX_VALUE, Float.MAX_VALUE, -1, -1);
        Vector2f result = Vector2f.ZERO;

        for (Vector3f vector : vectors) {
            if (toScreen(matrix4f, vector, result)) {
                position.set(
                        Math.min(result.x, position.getX()),
                        Math.min(result.y, position.getY()),
                        Math.max(result.x, position.getZ()),
                        Math.max(result.y, position.getW())
                );
            }
        }
        return position;
    }

    public static Vector4f getEntity2DPosition(Matrix4f matrix4f, ItemEntity itemEntity) {
        float width = itemEntity.getWidth() / 1.45F;
        float height = itemEntity.getHeight() * 1.3F;

        float f1 = MathHelper.sin(((float) itemEntity.getAge() + mc.getRenderPartialTicks()) / 10.0F + itemEntity.hoverStart) * 0.1F + 0.1F;
        Vector3d pos = getEntityPos(itemEntity, mc.getRenderPartialTicks());

        pos.y += 0.1F + f1;

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                pos.x - width,
                pos.y,
                pos.z - width,
                pos.x + width,
                pos.y + height,
                pos.z + width
        );

        Vector3f[] vectors = new Vector3f[]{
                new Vector3f((float) axisAlignedBB.minX, (float) axisAlignedBB.minY, (float) axisAlignedBB.minZ),
                new Vector3f((float) axisAlignedBB.minX, (float) axisAlignedBB.maxY, (float) axisAlignedBB.minZ),
                new Vector3f((float) axisAlignedBB.maxX, (float) axisAlignedBB.minY, (float) axisAlignedBB.minZ),
                new Vector3f((float) axisAlignedBB.maxX, (float) axisAlignedBB.maxY, (float) axisAlignedBB.minZ),
                new Vector3f((float) axisAlignedBB.minX, (float) axisAlignedBB.minY, (float) axisAlignedBB.maxZ),
                new Vector3f((float) axisAlignedBB.minX, (float) axisAlignedBB.maxY, (float) axisAlignedBB.maxZ),
                new Vector3f((float) axisAlignedBB.maxX, (float) axisAlignedBB.minY, (float) axisAlignedBB.maxZ),
                new Vector3f((float) axisAlignedBB.maxX, (float) axisAlignedBB.maxY, (float) axisAlignedBB.maxZ)
        };

        Vector4f position = new Vector4f(Float.MAX_VALUE, Float.MAX_VALUE, -1, -1);
        Vector2f result = Vector2f.ZERO;

        for (Vector3f vector : vectors) {
            if (toScreen(matrix4f, vector, result)) {
                position.set(
                        Math.min(result.x, position.getX()),
                        Math.min(result.y, position.getY()),
                        Math.max(result.x, position.getZ()),
                        Math.max(result.y, position.getW())
                );
            }
        }
        return position;
    }


    public static boolean toScreen(Matrix4f matrix, Vector3f position, Vector2f result) {
        float x = position.getX(),
                y = position.getY(),
                z = position.getZ();

        int screenWidth = mw.getScaledWidth(),
                screenHeight = mw.getScaledHeight();

        float w = matrix.m30 * x + matrix.m31 * y + matrix.m32 * z + matrix.m33;

        if (w < 0F)
            return false;

        float x2 = matrix.m00 * x + matrix.m01 * y + matrix.m02 * z + matrix.m03;
        float y2 = matrix.m10 * x + matrix.m11 * y + matrix.m12 * z + matrix.m13;

        result.set(
                (float) ((screenWidth * 0.5F + 0.5F * (x2 / w * screenWidth)) * mw.getGuiScaleFactor()),
                (float) ((screenHeight * 0.5F - 0.5F * (y2 / w * screenHeight)) * mw.getGuiScaleFactor())
        );

        return true;
    }

    public static Vector2f toScreen(final Vector3d position, Matrix4f projectionViewMatrix) {
        int screenWidth = mw.getScaledWidth(),
                screenHeight = mw.getScaledHeight();

        Vector3d camera = mc.gameRenderer.getActiveRenderInfo().getProjectedView(),
                dir = camera.subtract(position);

        Vector4f pos = new Vector4f((float) dir.x, (float) dir.y, (float) dir.z, 1.f);

        pos.transform(projectionViewMatrix);

        float w = pos.getW();

        if (w < .05f && w != 0) {
            pos.perspectiveDivide();
        } else {
            final float scale = (float) Math.max(screenWidth, screenHeight);
            pos.setX(pos.getX() * -1 * scale);
            pos.setY(pos.getY() * -1 * scale);
        }

        double centerX = screenWidth / 2.,
                centerY = screenHeight / 2.;

        return new Vector2f((float) ((centerX * pos.getX()) + (pos.getX() + centerX)), (float) (-(centerY * pos.getY()) + (pos.getY() + centerY)));
    }

    public static Vector2d worldToScreen(double x, double y, double z) {
        if (mc.getRenderManager().info == null)
            return new Vector2d();

        net.minecraft.util.math.vector.Vector3d cameraPosition = mc.getRenderManager().info.getProjectedView();
        Quaternion cameraRotation = mc.getRenderManager().getCameraOrientation().copy();
        cameraRotation.conjugate();

        Vector3f relativePosition = new Vector3f((float) (cameraPosition.x - x), (float) (cameraPosition.y - y), (float) (cameraPosition.z - z));
        relativePosition.transform(cameraRotation);

        if (mc.gameSettings.viewBobbing) {
            Entity renderViewEntity = mc.getRenderViewEntity();
            if (renderViewEntity instanceof PlayerEntity playerEntity) {
                float walkedDistance = playerEntity.distanceWalkedModified;

                float deltaDistance = walkedDistance - playerEntity.prevDistanceWalkedModified;
                float interpolatedDistance = -(walkedDistance + deltaDistance * mc.getRenderPartialTicks());
                float cameraYaw = MathHelper.lerp(mc.getRenderPartialTicks(), playerEntity.prevCameraYaw, playerEntity.cameraYaw);

                Quaternion bobQuaternionX = new Quaternion(Vector3f.XP, Math.abs(MathHelper.cos(interpolatedDistance * (float) Math.PI - 0.2F) * cameraYaw) * 5.0F, true);
                bobQuaternionX.conjugate();
                relativePosition.transform(bobQuaternionX);

                Quaternion bobQuaternionZ = new Quaternion(Vector3f.ZP, MathHelper.sin(interpolatedDistance * (float) Math.PI) * cameraYaw * 3.0F, true);
                bobQuaternionZ.conjugate();
                relativePosition.transform(bobQuaternionZ);

                Vector3f bobTranslation = new Vector3f((MathHelper.sin(interpolatedDistance * (float) Math.PI) * cameraYaw * 0.5F), (-Math.abs(MathHelper.cos(interpolatedDistance * (float) Math.PI) * cameraYaw)), 0.0f);
                bobTranslation.setY(-bobTranslation.getY());
                relativePosition.add(bobTranslation);
            }
        }

        double fieldOfView = (float) mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);

        float halfHeight = (float) mc.getMainWindow().getScaledHeight() / 2.0F;
        float scaleFactor = halfHeight / (relativePosition.getZ() * (float) Math.tan(Math.toRadians(fieldOfView / 2.0F)));

        if (relativePosition.getZ() < 0.0F) {
            return new Vector2d(-relativePosition.getX() * scaleFactor / (Client.instance.moduleManager.aspectRatioModule.isEnabled() ? 1 / Client.instance.moduleManager.aspectRatioModule.width.getValue() : 1) + (float) (mc.getMainWindow().getScaledWidth() / 2), (float) (mc.getMainWindow().getScaledHeight() / 2) - relativePosition.getY() * scaleFactor);
        }
        return null;
    }

}
