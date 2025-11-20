package relake.common.util;

import lombok.experimental.UtilityClass;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import relake.common.InstanceAccess;
import relake.common.component.rotation.FreeLookComponent;
import relake.event.impl.player.MoveEvent;
import relake.event.impl.player.MovementInputEvent;

@UtilityClass
public class MoveUtil implements InstanceAccess {

    public boolean isBlockAboveHead() {
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(mc.player.getPosX() - 0.3, mc.player.getPosY() + mc.player.getEyeHeight(),
                mc.player.getPosZ() + 0.3, mc.player.getPosX() + 0.3, mc.player.getPosY() + (!mc.player.isOnGround() ? 1.5 : 2.5),
                mc.player.getPosZ() - 0.3);
        return mc.world.getCollisionShapes(mc.player, axisAlignedBB).findAny().isPresent();
    }

    public boolean isMoving() {
        return mc.player.movementInput.moveForward != 0 || mc.player.movementInput.moveStrafe != 0;
    }

    public double getSpeed() {
        return Math.sqrt(mc.player.motion.x * mc.player.motion.x + mc.player.motion.z * mc.player.motion.z);
    }

    public void setSpeed(MoveEvent move, double motion) {
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;
        setSpeed(move, motion, yaw, strafe, forward);
    }

    public void setSpeedSilent(MoveEvent move, double motion) {
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        setSpeed(move, motion, moveYaw(FreeLookComponent.getFreeYaw()), strafe, forward);
    }

    public static void setSpeed(MoveEvent move, double motion, float yaw, double strafe, double forward) {
        if (forward == 0 && strafe == 0) {
            move.getMotion().x = 0;
            move.getMotion().z = 0;
        } else {
            if (forward != 0) {
                if (strafe > 0) {
                    yaw += (float) (forward > 0 ? -45 : 45);
                } else if (strafe < 0) {
                    yaw += (float) (forward > 0 ? 45 : -45);
                }
                strafe = 0;
                if (forward > 0) {
                    forward = 1;
                } else if (forward < 0) {
                    forward = -1;
                }
            }
            move.getMotion().x = forward * motion * MathHelper.cos((float) Math.toRadians(yaw + 90.0f))
                    + strafe * motion * MathHelper.sin((float) Math.toRadians(yaw + 90.0f));
            move.getMotion().z = forward * motion * MathHelper.sin((float) Math.toRadians(yaw + 90.0f))
                    - strafe * motion * MathHelper.cos((float) Math.toRadians(yaw + 90.0f));
        }
    }

    public void setSpeed(double speed) {
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;

        if (isMoving()) {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (float) (forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += (float) (forward > 0.0 ? 45 : -45);
                }

                strafe = 0;

                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }

            double cos = Math.cos(Math.toRadians(yaw + 89.5f));
            double sin = Math.sin(Math.toRadians(yaw + 89.5f));
            mc.player.motion.x = forward * speed * cos + strafe * speed * sin;
            mc.player.motion.z = forward * speed * sin - strafe * speed * cos;
        } else {
            mc.player.motion.x = 0.0D;
            mc.player.motion.z = 0.0D;
        }
    }

    public void setSpeed(double speed, float yaw, boolean onlyMove) {
        if (isMoving()) {
            double cos = Math.cos(Math.toRadians(yaw));
            double sin = Math.sin(Math.toRadians(yaw));
            mc.player.motion.x = -sin * speed;
            mc.player.motion.z = cos * speed;
        } else if (!onlyMove) {
            mc.player.motion.x = 0.0D;
            mc.player.motion.z = 0.0D;
        }
    }

    public static void strafe() {
        strafe(getSpeed());
    }

    public void strafe(final double speed) {
        if (!isMoving()) {
            return;
        }

        final double yaw = direction();
        mc.player.motion.x = -MathHelper.sin((float) yaw) * speed;
        mc.player.motion.z = MathHelper.cos((float) yaw) * speed;
    }

    public void strafe(final double speed, float yaw) {
        if (!isMoving()) {
            return;
        }

        yaw = (float) Math.toRadians(yaw);
        mc.player.motion.x = -MathHelper.sin(yaw) * speed;
        mc.player.motion.z = MathHelper.cos(yaw) * speed;
    }

    public void fixMovement(final MovementInputEvent event, final float yaw) {
        final float forward = event.getForward();
        final float strafe = event.getStrafe();

        final double angle = MathHelper.wrapDegrees(Math.toDegrees(direction(FreeLookComponent.getFreeYaw(), forward, strafe)));

        if (forward == 0 && strafe == 0) {
            return;
        }

        float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;

        for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
            for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                if (predictedStrafe == 0 && predictedForward == 0) continue;

                final double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(yaw, predictedForward, predictedStrafe)));
                final double difference = Math.abs(angle - predictedAngle);

                if (difference < closestDifference) {
                    closestDifference = (float) difference;
                    closestForward = predictedForward;
                    closestStrafe = predictedStrafe;
                }
            }
        }

        event.setForward(closestForward);
        event.setStrafe(closestStrafe);
    }


    public double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    public double direction() {
        float rotationYaw = mc.player.rotationYaw;

        if (mc.player.moveForward < 0) {
            rotationYaw += 180;
        }

        float forward = 1;

        if (mc.player.moveForward < 0) {
            forward = -0.5F;
        } else if (mc.player.moveForward > 0) {
            forward = 0.5F;
        }

        if (mc.player.moveStrafing > 0) {
            rotationYaw -= 90 * forward;
        }

        if (mc.player.moveStrafing < 0) {
            rotationYaw += 90 * forward;
        }

        return Math.toRadians(rotationYaw);
    }

    public static boolean moveKeyPressed(int keyNumber) {
        final boolean w = mc.gameSettings.keyBindForward.isKeyDown(),
                a = mc.gameSettings.keyBindLeft.isKeyDown(),
                s = mc.gameSettings.keyBindBack.isKeyDown(),
                d = mc.gameSettings.keyBindRight.isKeyDown();
        return keyNumber == 0 ? w : keyNumber == 1 ? a : keyNumber == 2 ? s : keyNumber == 3 ? d : false;
    }

    public static boolean w() {
        return moveKeyPressed(0);
    }

    public static boolean a() {
        return moveKeyPressed(1);
    }

    public static boolean s() {
        return moveKeyPressed(2);
    }

    public static boolean d() {
        return moveKeyPressed(3);
    }

    public static float moveYaw(float append) {
        return append + (a() && d() && !(w() && s()) && (w() || s()) ? w() ? 0 : s() ? 180 : 0 : w() && s() && !(a() && d()) && (a() || d()) ? a() ? -90 : d() ? 90 : 0 : (a() && d() && !(w() && s()) || w() && s() && !(a() && d())) ? 0 : a() || d() || s() ? (w() && !s() ? 45 : s() && !w() ? a() || d() ? 45 * 3 : 45 * 4 : !w() && !s() || w() && s() ? 45 * 2 : 0) * (a() ? -1 : 1) : 0);
    }
}
