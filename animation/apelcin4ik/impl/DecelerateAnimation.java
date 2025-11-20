package relake.animation.apelcin4ik.impl;

import relake.animation.apelcin4ik.Animation;

public class DecelerateAnimation extends Animation {
    public DecelerateAnimation(int speed) {
        super(speed, 1);
    }

    public DecelerateAnimation(int speed, float endValue) {
        super(speed, endValue);
    }

    @Override
    public float calculateValue(float x) {
        return 1 - ((x - 1) * (x - 1));
    }

    @Override
    public float get() {
        if (isForward()) {
            if (isFinished()) {
                return this.endValue;
            }

            return calculateValue(timerHandler.elapsedTime() / (float) this.timeMs) * this.endValue;
        } else {
            if (isFinished()) {
                return 0F;
            }

            return (1 - calculateValue(timerHandler.elapsedTime() / (float) this.timeMs)) * this.endValue;
        }
    }
}
