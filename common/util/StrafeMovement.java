package relake.common.util;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import relake.common.InstanceAccess;
import relake.event.impl.player.MoveEvent;

@Data @Getter @Setter
public class StrafeMovement implements InstanceAccess {
    private double oldSpeed, contextFriction;
    private boolean needSwap;
    private boolean needSprintState;
    private int counter;
    private int noSlowTicks;

    public double calcSpeed(MoveEvent move, boolean damageBoost, boolean hasTime, boolean autoJump, float damageSpeed) {
        boolean isOnGround = mc.player.isOnGround();
        boolean isLanding = move.isToGround();
        boolean isJumping = move.getMotion().y > 0;
        float moveSpeed = getAIMoveSpeed(mc.player);
        float friction = getFrictionFactor(mc.player, move);

        float adjustedFriction = mc.player.isPotionActive(Effects.JUMP_BOOST) && mc.player.isHandActive() ? 0.88f : 0.9103f;
        if (isOnGround) {
            adjustedFriction = friction;
        }

        float frictionAdjustment = 0.16277136f / (adjustedFriction * adjustedFriction * adjustedFriction);
        float calculatedSpeed;

        if (isOnGround) {
            calculatedSpeed = moveSpeed * frictionAdjustment;
            if (isJumping) {
                calculatedSpeed += 0.2f;
            }
        } else {
            calculatedSpeed = (damageBoost && hasTime && (autoJump || mc.gameSettings.keyBindJump.isKeyDown())) ? damageSpeed : 0.0255f;
        }

        boolean isSlowed = false;
        double maxSpeed = oldSpeed + calculatedSpeed;
        double currentSpeed = 0.0;

        if (mc.player.isHandActive() && !isJumping) {
            double adjustedSpeed = oldSpeed + calculatedSpeed * 0.25;
            double verticalMotion = move.getMotion().y;
            if (verticalMotion != 0.0 && Math.abs(verticalMotion) < 0.08) {
                adjustedSpeed += 0.055;
            }
            if (maxSpeed > (currentSpeed = Math.max(0.043, adjustedSpeed))) {
                isSlowed = true;
                ++noSlowTicks;
            } else {
                noSlowTicks = Math.max(noSlowTicks - 1, 0);
            }
        } else {
            noSlowTicks = 0;
        }

        if (noSlowTicks > 3) {
            maxSpeed = currentSpeed - (mc.player.isPotionActive(Effects.JUMP_BOOST) && mc.player.isHandActive() ? 0.3 : 0.019);
        } else {
            maxSpeed = Math.max(isSlowed ? 0 : 0.249984 - (++counter % 2) * 0.0001D, maxSpeed);
        }

        contextFriction = adjustedFriction;

        if (!isLanding && !isOnGround) {
            needSwap = true;
        }
        if (!isOnGround && !isLanding) {
            needSprintState = !mc.player.isServerSprintState();
        }
        if (isLanding && isOnGround) {
            needSprintState = false;
        }

        return maxSpeed;
    }


    public void postMove(final double horizontal) {
        oldSpeed = horizontal * contextFriction;
    }

    private static float getAIMoveSpeed(final ClientPlayerEntity contextPlayer) {
        boolean prevSprinting = contextPlayer.isSprinting();
        contextPlayer.setSprinting(false);
        float speed = contextPlayer.getAIMoveSpeed() * 1.3f;
        contextPlayer.setSprinting(prevSprinting);
        return speed;
    }

    private static float getFrictionFactor(final ClientPlayerEntity contextPlayer, final MoveEvent move) {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        pos.setPos(move.getFrom().x, move.getAabbFrom().minY - 1.0D, move.getFrom().z);
        return contextPlayer.world.getBlockState(pos).getBlock().getSlipperiness() * 0.91F;
    }
}
