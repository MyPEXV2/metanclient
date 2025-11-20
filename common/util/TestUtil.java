package relake.common.util;

import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector3d;
import relake.common.InstanceAccess;

public class TestUtil implements InstanceAccess {
    public static float getFallDistance(int nextTicks) {
        final Vector3d deltaMove = new Vector3d(0, mc.player.getMotion().y, 0);

        if (deltaMove.y == 0
                || mc.player.isOnGround())
            return 0;

        float fallDistance = 0;

        double d0 = 0.08D;
        boolean flag = deltaMove.y <= 0.0D;

        if (flag && mc.player.isPotionActive(Effects.SLOW_FALLING)) {
            d0 = 0.01D;
        }

        for (int i = 0; i < nextTicks + 1; i++) {
            double d2 = deltaMove.y;
            d2 -= d0;

            deltaMove.y = d2 * 0.98F;

            if (deltaMove.y > 0) {
                fallDistance = 0;
            } else {
                fallDistance -= (float) deltaMove.y;
            }
        }

        return fallDistance;
    }
}