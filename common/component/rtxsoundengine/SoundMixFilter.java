package relake.common.component.rtxsoundengine;

import net.minecraft.client.Minecraft;
import relake.Client;
import relake.common.component.rtxsoundengine.filters.FilterLowPass;
import relake.common.component.rtxsoundengine.filters.FilterReverb;
import relake.common.util.MathUtil;

public class SoundMixFilter {
    private final SoundMixerBase mixer;
    public SoundMixerBase getMixer() {
        return mixer;
    }
    private final SoundSurroundTool surround;
    public SoundSurroundTool getSurround() {
        return surround;
    }
    private boolean state = false;
    public boolean isState() {
        return state;
    }
    public void setState(boolean state) {
        this.state = state;
    }
    private SoundMixFilter() {
        this.mixer = SoundMixerBase.loadMixer();
        this.surround = SoundSurroundTool.build();
    }

    public static SoundMixFilter makeDistorterMixer() {
        return new SoundMixFilter();
    }

    public void updateMixer() {
        //params
        float[] args = new float[]{0.F, 0.F, 1.F, 1.F};
        //update allow
        this.setState(Client.instance.moduleManager.rtxSoundsModule.isEnabled());//ClientTune.get.getRTSoundSurround());
        //while allowed
        if (this.state) {
            //processing filter parameters
            this.surround.setRtxDebug(false);
            this.surround.setPlayer(Minecraft.getInstance().player);
            this.surround.setTooPerfomance(Client.instance.moduleManager.rtxSoundsModule.perfomancePriority.isSelected("Производительность"));
            args = this.surround.getGainArgsFromWorld();
        } else if (!this.surround.getListOfTestVecs().isEmpty()) {
            int sz = this.surround.getListOfTestVecs().size();
            for (int i = 0; i < sz / 2.F; i++)
                if (!this.surround.getListOfTestVecs().isEmpty())
                    this.surround.getListOfTestVecs().remove(0);
        } else this.surround.setRtxDebug(false);
        this.mixer.setEchoEffect(args[0] * (this.surround.isTooPerfomance() ? .75F : 1.F), args[1] * (this.surround.isTooPerfomance() ? .4F : .8F));
        this.mixer.setLowPass(args[2], args[3]);
        //apply
        this.updateFiltersData();
    }

    private void updateFiltersData() {
        final SoundMixerBase mixer = this.getMixer();
        final FilterReverb reverbFilter = mixer.getReverbFilter();
        final FilterLowPass lowPassFilter = mixer.getLowPassFilter();
        final float echoDelay = mixer.getEchoPercent(), echoRev = echoDelay + mixer.getReflectPercent() * 8.000011459172877F;
        reverbFilter.decayTime = echoDelay;
        reverbFilter.reflectionsGain = echoRev * (0.05F + 0.05F * echoDelay);
        reverbFilter.reflectionsDelay = 0.125F * echoDelay;
        reverbFilter.lateReverbGain = echoRev * (1.26F + 0.2F * echoDelay);
        reverbFilter.lateReverbDelay = 0.01F * echoDelay;
        reverbFilter.checkParameters();
        final float lLevelGain = mixer.getLowPassGain(), lLevelGainHF = mixer.getLowPassGainHF();
        lowPassFilter.gain = MathUtil.lerp(lowPassFilter.gain, lLevelGain, lowPassFilter.gainHF > lLevelGainHF ? .05F : .1F);
        lowPassFilter.gainHF = MathUtil.lerp(lowPassFilter.gainHF, lLevelGainHF, lowPassFilter.gainHF > lLevelGainHF ? .3F : .12F);
        lowPassFilter.checkParameters();
    }

    public boolean getHasMixerLoaded() {
        return mixer != null;
    }

    public void init() {
        /*
        //testing part 1
        assert this != null : -1;
        //testing part 2
        mixer.setEchoEffect(2.F, 1);
        mixer.setLowPass(1.F, .6F);
        */
    }

    public void unload() {
        mixer.cleanupEffects();
    }
}
