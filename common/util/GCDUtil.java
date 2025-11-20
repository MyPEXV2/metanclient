package relake.common.util;

import lombok.experimental.UtilityClass;
import relake.common.InstanceAccess;

@UtilityClass
public class GCDUtil implements InstanceAccess {
    public float getSensitivity(float rotation) {
        return getDeltaMouse(rotation) * getGCDValue();
    }

    public float getGCDValue() {
        return (float) (getGCD() * 0.15D);
    }

    public float getGCD() {
        float f1;
        return (f1 = (float) (mc.gameSettings.mouseSensitivity * 0.6 + 0.2)) * f1 * f1 * 8;
    }

    public float getDeltaMouse(float delta) {
        return Math.round(delta / getGCDValue());
    }
}
