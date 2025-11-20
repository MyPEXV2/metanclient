package relake.common.component.rtxsoundengine.filters;

import org.lwjgl.openal.EXTEfx;

public class FilterLowPass extends BaseFilter {
    public float gain = 1.F, gainHF = 1.F;

    public void loadFilter() {
        if (!this.isLoaded) {
            this.isLoaded = true;
            this.id = EXTEfx.alGenFilters();
            this.slot = this.id;
            EXTEfx.alFilteri(this.id, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
        }

    }

    public void checkParameters() {
        if (this.gain < 0.0F) {
            this.gain = 0.0F;
        }

        if (this.gain > 1.0F) {
            this.gain = 1.0F;
        }

        if (this.gainHF < 0.0F) {
            this.gainHF = 0.0F;
        }

        if (this.gainHF > 1.0F) {
            this.gainHF = 1.0F;
        }

    }

    public void loadParameters() {
        this.checkParameters();
        this.loadFilter();
        EXTEfx.alFilterf(this.id, EXTEfx.AL_LOWPASS_GAIN, this.gain);
        EXTEfx.alFilterf(this.id, EXTEfx.AL_LOWPASS_GAINHF, this.gainHF);
    }
}
