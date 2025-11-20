package relake.render.display.bqrender.framelimiter;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

public abstract class AbstractFrameLimiter {
    protected long lastHookTime;
    protected int accumulatedCalls;
    protected long hookIntervalNS;
    protected int fps;

    public AbstractFrameLimiter(int fps) {
        if (fps <= 0) {
            throw new IllegalArgumentException("FPS must be greater than 0.");
        }
        this.fps = fps;
        this.lastHookTime = Util.nanoTime();
        this.hookIntervalNS = 1_000_000_000L / fps;
        this.accumulatedCalls = 0;
    }

    protected abstract void performRender(IFrameCall... calls);

    public void render(IFrameCall... calls) {
        long nanoTime = Util.nanoTime();
        long elapsed = nanoTime - lastHookTime;

        accumulatedCalls += (int) (elapsed / hookIntervalNS);
        lastHookTime += (accumulatedCalls * hookIntervalNS);

        accumulatedCalls = Math.min(accumulatedCalls, Math.min(fps, Minecraft.debugFPS));

        while (accumulatedCalls > 0) {
            performRender(calls);
            accumulatedCalls--;
        }
    }

    public void setFps(int fps) {
        if (fps <= 0) {
            throw new IllegalArgumentException("FPS must be greater than 0.");
        }
        if (this.fps == fps) return;
        this.fps = fps;
        this.lastHookTime = Util.nanoTime();
        this.hookIntervalNS = 1_000_000_000L / fps;
        this.accumulatedCalls = 0;
    }
}
