package relake.common.util;

import java.time.Duration;

public class StopWatch {
    private long time = System.currentTimeMillis();

    public void reset() {
        this.time = System.currentTimeMillis();
    }

    public boolean finished(double time) {
        return (System.currentTimeMillis() - this.time) > time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean finished(Duration time) {
        return (System.currentTimeMillis() - this.time) > time.toMillis();
    }

    public long elapsedTime() {
        return System.currentTimeMillis() - this.time;
    }
}
