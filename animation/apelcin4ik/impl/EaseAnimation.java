package relake.animation.apelcin4ik.impl;

import relake.animation.apelcin4ik.Animation;
import relake.animation.apelcin4ik.AnimationDirection;

public class EaseAnimation extends Animation {
    private final float easeAmount;

    public EaseAnimation(int speed, float spring) {
        super(speed, 1);
        this.easeAmount = spring;
    }

    public EaseAnimation(int speed, float spring, float endValue) {
        super(speed, endValue);
        this.easeAmount = spring;
    }

    @Override
    public float calculateValue(float revTime) {
        float normalizedRevTime = revTime / timeMs;
        float shrink = easeAmount + 1;

        return (float) Math.max(0F, 1F + shrink * Math.pow(normalizedRevTime - 1F, 3F) + easeAmount * Math.pow(normalizedRevTime - 1F, 2F));
    }
}