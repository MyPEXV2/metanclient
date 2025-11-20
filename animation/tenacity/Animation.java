package relake.animation.tenacity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import relake.common.util.StopWatch;

import java.time.Duration;

@Setter
@Getter
@Accessors(chain = true)
public class Animation {
    private float start;
    private float target;
    private Duration duration;

    private StopWatch stopWatch = new StopWatch();
    private Direction direction = Direction.FORWARD;

    public Animation(float start, Duration duration) {
        this.start = start;
        this.duration = duration;
        reset();
    }

    public void reset() {
        stopWatch.reset();
    }

    public void update(float newTarget) {
        if (this.target != newTarget) {
            this.start = get();
            this.target = newTarget;
            reset();
        }
    }

    public void switchDirection(Direction direction) {
        if (this.direction != direction) {
            this.direction = direction;
            reset();
        }
    }

    public void switchDirection(boolean state) {
        switchDirection(
                state ? Direction.FORWARD : Direction.BACKWARD
        );
    }

    public boolean isForward() {
        return direction == Direction.FORWARD;
    }

    public boolean isBackward() {
        return direction == Direction.BACKWARD;
    }

    public float get() {
        float progress = Math.min(1f, (float) stopWatch.elapsedTime() / duration.toMillis());
        return start * (direction == Direction.FORWARD ? progress : (1 - progress));
    }



    public boolean isDone() {
        return stopWatch.finished(duration.toMillis());
    }

    public boolean isDone(Direction direction) {
        return isDone() && this.direction == direction;
    }
}
