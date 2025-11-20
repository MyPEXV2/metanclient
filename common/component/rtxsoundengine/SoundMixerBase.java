package relake.common.component.rtxsoundengine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import relake.Client;
import relake.common.component.rtxsoundengine.filters.BaseFilter;
import relake.common.component.rtxsoundengine.filters.FilterException;
import relake.common.component.rtxsoundengine.filters.FilterLowPass;
import relake.common.component.rtxsoundengine.filters.FilterReverb;
import relake.common.util.MathUtil;

import javax.annotation.Nullable;

public class SoundMixerBase {
    private float echoPercent, reflectPercent;
    public float getEchoPercent() {
        return echoPercent;
    }
    public float getReflectPercent() {
        return reflectPercent;
    }
    private float lowPassGain, lowPassGainHF;
    public float getLowPassGain() {
        return lowPassGain;
    }
    public float getLowPassGainHF() {
        return lowPassGainHF;
    }
    private final FilterLowPass lowPassFilter = new FilterLowPass();
    public FilterLowPass getLowPassFilter() {
        return lowPassFilter;
    }
    private final FilterReverb reverbFilter = new FilterReverb();
    public FilterReverb getReverbFilter() {
        return reverbFilter;
    }
    private boolean locked;

    public SoundMixerBase() {
    }

    public static SoundMixerBase loadMixer() {
        return new SoundMixerBase();
    }


    public void setEchoEffect(float longest, float reflect) {
        if (locked) return;
        echoPercent = MathUtil.lerp(echoPercent, longest, echoPercent > longest ? .7F : .5F);
        reflectPercent = MathUtil.lerp(reflectPercent, reflect, reflectPercent > reflect ? .7F : .5F);
    }

    public void setLowPass(float gain, float gainHF) {
        if (locked && !(lowPassGain == 0.F && lowPassGainHF == 0.F)) return;
        lowPassGain = gain;
        lowPassGainHF = gainHF;
    }

    public void cleanupEffects() {
        if (locked) return;
        echoPercent = 0;
        reflectPercent = 0;
        lowPassGain = 1;
        lowPassGainHF = 1;
    }

    public void lockup() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }

    public void injectFiltersToChannel(@Nullable ISound sound, int sourceId) {
        final SoundMixFilter mixFilter = Client.instance.getRtxEngine();
        if (mixFilter == null) return;
        final SoundMixerBase mixer = mixFilter.getMixer();
        boolean canUseFilters = !Minecraft.getInstance().isGamePaused() && Minecraft.getInstance().world != null && mixFilter.isState();
        final FilterReverb reverbFilter = mixer.getReverbFilter();
        final FilterLowPass lowPassFilter = mixer.getLowPassFilter();
        if (canUseFilters) {
            if (reverbFilter.reflectionsDelay > 0 && reverbFilter.lateReverbDelay > 0) {
                reverbFilter.enable();
                reverbFilter.loadParameters();
            } else {
                reverbFilter.disable();
            }
            if (lowPassFilter.gain != 1 || lowPassFilter.gainHF != 1) {
                lowPassFilter.loadParameters();
                lowPassFilter.enable();
                lowPassFilter.loadParameters();
            } else lowPassFilter.disable();
            try {
                BaseFilter.load3SourceFilters(sourceId, 131078, reverbFilter, lowPassFilter, null);
                BaseFilter.loadSourceFilter(sourceId, 131077, lowPassFilter);
            } catch (FilterException var9) {
                CrashReport crashreport = CrashReport.makeCrashReport(var9, "Updating Sound Filters");
                throw new ReportedException(crashreport);
            }
        } else {
            try {
                reverbFilter.loadParameters();
                lowPassFilter.loadParameters();
                BaseFilter.load3SourceFilters(sourceId, 131078, null, null, null);
                BaseFilter.loadSourceFilter(sourceId, 131077, null);
            } catch (FilterException e) {
                CrashReport crashreport = CrashReport.makeCrashReport(e, "Updating Sound Filters");
                throw new ReportedException(crashreport);
            } finally {
                lowPassFilter.disable();
                reverbFilter.disable();
            }
        }
    }
}
