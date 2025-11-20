/*
 * Было сделано FakeSystem(Апельсин)
 */

package relake.animation.apelcin4ik;


import relake.common.util.StopWatch;

public abstract class Animation implements IAnimation {
    protected AnimationDirection animationDirection = AnimationDirection.FORWARD;
    protected final StopWatch timerHandler = new StopWatch();
    protected float endValue;
    protected int timeMs;

    protected Animation(int timeMs, float endValue) {
        this.timeMs = timeMs;
        this.endValue = endValue;
    }

    public float get() {
        boolean isForward = isForward();

        if (isForward)
            return isFinished() ? endValue : calculateValue(timerHandler.elapsedTime()) * endValue;

        float revTime = (float) Math.min(Math.max(timeMs - timerHandler.elapsedTime(), 0), timeMs);
        return isFinished() ? 0 : calculateValue(revTime) * endValue;
    }

    public void setEndValue(float endValue) {
        this.endValue = endValue;
    }

    public void reset() {
        timerHandler.reset();
    }

    public boolean isFinished() {
        return timerHandler.finished(timeMs);
    }

    public boolean isFinished(AnimationDirection direction) {
        return this.animationDirection == direction && timerHandler.finished(timeMs);
    }

    public boolean isForward() {
        return animationDirection == AnimationDirection.FORWARD;
    }

    public boolean isBackward() {
        return animationDirection == AnimationDirection.BACKWARD;
    }

    public Animation setDirection(boolean direction) {
        return this.setDirection(direction ? AnimationDirection.FORWARD : AnimationDirection.BACKWARD);
    }

    public void finish() {
        this.timerHandler.setTime(System.currentTimeMillis());
    }

    public Animation setDirection(AnimationDirection newDirection) {
        if (this.animationDirection != newDirection)
            this.timerHandler.setTime(System.currentTimeMillis() - (this.timeMs - Math.min(this.timeMs, this.timerHandler.elapsedTime())));

        this.animationDirection = newDirection;

        return this;
    }

    public void setTimeMs(int timeMs) {
        if (this.timeMs != timeMs)
            reset();

        this.timeMs = timeMs;
    }
}