package relake.render.display.bqrender.framelimiter;

public class FrameLimiterSimple extends AbstractFrameLimiter {
    public FrameLimiterSimple(int fps) {
        super(fps);
    }

    @Override
    protected void performRender(IFrameCall... calls) {
        for (IFrameCall call : calls) {
            call.execute();
        }
    }
}
