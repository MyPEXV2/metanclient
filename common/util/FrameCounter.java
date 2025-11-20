package relake.common.util;

import com.google.common.collect.Lists;
import java.util.List;

public class FrameCounter {
    private double lastPassedTime = System.nanoTime() / 1000000.D;
    private double fps, latency;

    private FrameCounter() {
    }

    public static FrameCounter build() {
        return new FrameCounter();
    }

    private final List<Double> framesLatency = Lists.newArrayList();

    public void renderThreadRead(int scaleFramesCheck) {
        if (scaleFramesCheck < 1) return;
        final double ms = System.nanoTime() / 1000000.D;
        final double lastLatency = ms - this.lastPassedTime;
        this.lastPassedTime = ms;
        this.framesLatency.add(lastLatency);
        while (this.framesLatency.size() > scaleFramesCheck) {
            this.framesLatency.remove(0);
        }
        this.fps = 0.F;
        this.latency = 0.F;
        for (final Double latency : framesLatency) {
            this.fps += 1000.D / Math.max(latency, .001D) / (scaleFramesCheck/* + 1.5D*/);
            this.latency += latency / (scaleFramesCheck/* + 1.5D*/);
        }
    }

    public void reset() {
        this.fps = 0.F;
        this.latency = 1.F;
        this.framesLatency.clear();
    }

    public double getFps() {
        return this.fps;
    }

    public String getFpsString(boolean append) {
        String fps = (int) (this.fps + .5F) + "";
        if (append) fps += "fps";
        return fps;
    }

    public double getLatency() {
        return this.latency;
    }

    public String getLatencyString(boolean append) {
        String latency = String.format("%.2f", this.latency);
        if (append) latency += "ms";
        return latency;
    }
}
