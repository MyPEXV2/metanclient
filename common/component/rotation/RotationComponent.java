package relake.common.component.rotation;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import relake.Client;
import relake.common.InstanceAccess;
import relake.common.util.ChatUtil;
import relake.common.util.GCDUtil;
import relake.common.util.MoveUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.event.impl.player.MovementInputEvent;

import static java.lang.Math.toRadians;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

public class RotationComponent implements InstanceAccess {
    public static RotationTask currentTask = RotationTask.IDLE;
    public static float currentTurnSpeed;
    public static float currentReturnSpeed;
    public static int currentPriority;
    public static int currentTimeout;
    public static void resetParentTimeout() {
        currentTimeout = 0;
        currentTask = RotationTask.IDLE;
        currentPriority = 0;
        FreeLookComponent.setActive(false);
    }
    public static int idleTicks;

    public RotationComponent() {
        Client.instance.eventManager.register(this);
    }

    @EventHandler
    public void tick(TickEvent event) {
        idleTicks++;

        if (currentTask == RotationTask.AIM && idleTicks > currentTimeout) {
            currentTask = RotationTask.RESET;
        }

        if (currentTask == RotationTask.RESET) {
            Rotation rotation = new Rotation(mc.gameRenderer.getActiveRenderInfo().getYaw(), mc.gameRenderer.getActiveRenderInfo().getPitch());

            if (updateRotation(rotation, currentReturnSpeed)) {
                currentTask = RotationTask.IDLE;
                currentPriority = 0;
                FreeLookComponent.setActive(false);
            }
        }
    }

    @EventHandler
    public void onMoveInput(MovementInputEvent movementInputEvent) {
        if (!Client.instance.moduleManager.attackAuraModule.moveCorrection.isSelected("Свободный") || Client.instance.moduleManager.strafeModule.callStopRotates) return;

        final float forward = movementInputEvent.getForward();
        final float strafe = movementInputEvent.getStrafe();

        final double angle = MathHelper.wrapDegrees(Math.toDegrees(direction(mc.player.isElytraFlying() ? mc.player.rotationYaw : FreeLookComponent.getFreeYaw(), forward, strafe)));

        if (forward == 0 && strafe == 0) return;

        float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;

        for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
            for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                if (predictedStrafe == 0 && predictedForward == 0)
                    continue;

                final double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(mc.player.rotationYaw, predictedForward, predictedStrafe)));
                final double difference = Math.abs(angle - predictedAngle);

                if (difference < closestDifference) {
                    closestDifference = (float) difference;
                    closestForward = predictedForward;
                    closestStrafe = predictedStrafe;
                }
            }
        }
        movementInputEvent.setForward(closestForward);
        movementInputEvent.setStrafe(closestStrafe);
    }

    public static double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        float forward = moveForward < 0F ? -.5F : moveForward > 0F ? .5F : 1F;
        rotationYaw += ((90F * forward) * (moveStrafing > 0F ? -1 : moveStrafing < 0F ? 1 : 0)) + (moveForward < 0F ? 180F : 0);

        return toRadians(rotationYaw);
    }

    public static void update(Rotation rotation, float turnSpeed, float returnSpeed, int timeout, int priority) {
        if (currentPriority > priority) {
            return;
        }

        if (currentTask == RotationTask.IDLE) {
            FreeLookComponent.setActive(true);
        }

        currentTurnSpeed = turnSpeed;
        currentReturnSpeed = returnSpeed;
        currentTimeout = timeout;
        currentPriority = priority;

        currentTask = RotationTask.AIM;

        updateRotation(rotation, turnSpeed);
    }

    public static Vector2f applySensitivityPatch(Vector2f rotation, Vector2f previousRotation) {
        double sens = mc.gameSettings.mouseSensitivity;
        double gcd = Math.pow(sens * (double) 0.6F + (double) 0.2F, 3.0D) * 8.0D;

        double prevYaw = previousRotation.x;
        double prevPitch = previousRotation.y;

        double currentYaw = rotation.x;
        double currentPitch = rotation.y;

        double yaw = (Math.ceil(((currentYaw - prevYaw) / gcd) / 0.15F) * gcd) * 0.15F;
        double pitch = (Math.ceil(((currentPitch - prevPitch) / gcd) / 0.15F) * gcd) * 0.15F;

        return new Vector2f((float) (prevYaw + yaw), (float) (prevPitch + pitch));
    }

    private static boolean updateRotation(Rotation rotation, float turnSpeed) {
        Rotation currentRotation = new Rotation(mc.player);

        float yawDelta = wrapDegrees(rotation.getYaw() - currentRotation.getYaw());
        float pitchDelta = rotation.getPitch() - currentRotation.getPitch();

        float totalDelta = Math.abs(yawDelta) + Math.abs(pitchDelta);

        float yawSpeed = (totalDelta == 0) ? 0 : Math.abs(yawDelta / totalDelta) * turnSpeed;
        float pitchSpeed = (totalDelta == 0) ? 0 : Math.abs(pitchDelta / totalDelta) * turnSpeed;

        Vector2f rot = applySensitivityPatch(
                new Vector2f(
                        mc.player.rotationYaw + clamp(yawDelta, -yawSpeed, yawSpeed),
                        MathHelper.clamp(mc.player.rotationPitch + clamp(pitchDelta, -pitchSpeed, pitchSpeed), -90, 90)
                ),
                new Vector2f(
                        mc.player.rotationYaw,
                        mc.player.rotationPitch
                )
        );

        mc.player.rotationYaw = rot.x;
        mc.player.rotationPitch = rot.y;

//        mc.player.rotationYaw += GCDUtil.getSensitivity(clamp(yawDelta, -yawSpeed, yawSpeed));
//        mc.player.rotationPitch = MathHelper.clamp(mc.player.rotationPitch + GCDUtil.getSensitivity(clamp(pitchDelta, -pitchSpeed, pitchSpeed)), -90, 90);

        Rotation finalRotation = new Rotation(mc.player);

        idleTicks = 0;

        return finalRotation.getDelta(rotation) < (currentTask.equals(RotationTask.RESET) ? currentReturnSpeed : currentTurnSpeed);
    }

    public enum RotationTask {
        AIM,
        RESET,
        IDLE
    }
}
