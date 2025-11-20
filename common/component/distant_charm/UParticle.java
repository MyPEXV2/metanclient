package relake.common.component.distant_charm;

import net.minecraft.util.math.vector.Vector3d;
import relake.animation.excellent.util.Easings;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.common.util.StopWatch;

import java.awt.*;
import java.util.Random;

public class UParticle {
    private static Random RAND = new Random();
    private Vector3d pos, prevPos, motion;
    private StopWatch stopWatch;
    private final int maxTime;
    private final float gravity;
    private int color;
    public UParticle(Vector3d spawnPos, int maxTime, float motionXZSpeedMax, float motionYSpeedMax, float gravity) {
        this.pos = new Vector3d(spawnPos.x, spawnPos.y, spawnPos.z);
        this.prevPos = new Vector3d(spawnPos.x, spawnPos.y, spawnPos.z);
        this.maxTime = maxTime;
        this.stopWatch = new StopWatch();
        this.motion = new Vector3d(
             MathUtil.lerp(-motionXZSpeedMax, motionXZSpeedMax, RAND.nextFloat()),
             MathUtil.lerp(-motionYSpeedMax / 4.F, motionYSpeedMax, RAND.nextFloat()),
             MathUtil.lerp(-motionXZSpeedMax, motionXZSpeedMax, RAND.nextFloat())
        );
        this.gravity = gravity;
        this.color = Color.HSBtoRGB(0.F, 0.F, (float) Easings.EXPO_IN_OUT.ease(RAND.nextFloat()));
    }
    public void updateMotion() {
        this.prevPos.x = this.pos.x;
        this.prevPos.y = this.pos.y;
        this.prevPos.z = this.pos.z;
        this.pos = this.pos.add(motion);
        this.motion.x *= .99F;
        this.motion.z *= .99F;
        this.motion.y -= gravity;
        this.motion.y *= .994F;
    }

    public Vector3d getRenderPos(float partialTicks) {
        return this.prevPos.add((this.pos.x - this.prevPos.x) * partialTicks, (this.pos.y - this.prevPos.y) * partialTicks, (this.pos.z - this.prevPos.z) * partialTicks);
    }

    public boolean removeIf() {
        return this.stopWatch.finished(maxTime);
    }

    public int getColor() {
        return ColorUtil.multAlpha(this.color, 1.F - Math.min(this.stopWatch.elapsedTime() / (float)maxTime, 1.F));
    }

    public int uniqueIntCode() {
        return this.hashCode();
    }

    public int getScaledBeginInt(int maxInt) {
        return this.hashCode() % maxInt;
    }
}
