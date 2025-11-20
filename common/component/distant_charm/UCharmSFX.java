package relake.common.component.distant_charm;

import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import relake.common.util.SoundUtil;
import relake.common.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class UCharmSFX {
    private final Random RANDOM;
    private final String atFoderSFX, sfxFormat;
    private final List<TimedRunnableRunner> timedRuns = new ArrayList<>();
    private class TimedRunnableRunner {
        private final StopWatch timer;
        private Runnable runnable;
        private final long removePost;
        public TimedRunnableRunner(Runnable runnable, long removePost) {
            this.timer = new StopWatch();
            this.timer.reset();
            this.runnable = runnable;
            this.removePost = removePost;
        }
        public void update() {
            if (this.timer.finished(this.removePost) && this.runnable != null)  {
                this.runnable.run();
                this.runnable = null;
            }
        }
        public boolean removeIf() {
            return this.runnable == null || this.timer.finished(this.removePost);
        }
    }
    private UCharmSFX() {
        RANDOM = new Random();
        atFoderSFX = "charmsfxpack/";
        sfxFormat = ".wav";
    }
    public static UCharmSFX create() {
        return new UCharmSFX();
    }
    private float getMinecraftMasterPC() {
        return Minecraft.getInstance().gameSettings.getSoundLevel(SoundCategory.MASTER) / 1.3F;
    }
    private float volPulseDistantRAND() {return 1.F;}
    public void sfxPulseDistantRAND(long timeWait) {
        silentPlay(atFoderSFX + "pulsecharmdistant" + RANDOM.nextInt(5) + sfxFormat, volPulseDistantRAND(), timeWait);
    }
    private float volKnockMain() {return 1.F;}
    public void sfxKnockMain(long timeWait) {
        silentPlay(atFoderSFX + "knockcharmmain" + sfxFormat, volKnockMain(), timeWait);
    }
    private float volEchoMain() {return .6F;}
    public void sfxEchoMain(long timeWait) {
        silentPlay(atFoderSFX + "echocharmmain" + sfxFormat, volEchoMain(), timeWait);
    }
    private float volSparksBlockHit() {return .2F;}
    public void sfxSparksBlockHit(long timeWait) {
        silentPlay(atFoderSFX + "sparkscollisionneared" + sfxFormat, volSparksBlockHit(), timeWait);
    }
    private void silentPlay(final String filePostLoc, final float volPC, long timeWait) {
        this.timedRuns.add(new TimedRunnableRunner(() -> SoundUtil.playSoundInstant(filePostLoc, volPC * this.getMinecraftMasterPC()), timeWait));
    }
    public void inRenderThreadUpdate() {
        if (this.timedRuns.isEmpty()) return;
        this.timedRuns.forEach(TimedRunnableRunner::update);
        this.timedRuns.removeIf(TimedRunnableRunner::removeIf);
    }
}