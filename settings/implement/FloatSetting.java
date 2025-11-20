package relake.settings.implement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import relake.common.util.MathUtil;
import relake.settings.Setting;

@Getter
@Setter
@Accessors(chain = true)
public class FloatSetting extends Setting<Float> {

    private float min = 0;
    private float max = 1;
    private float increment;

    public FloatSetting(String name) {
        super(name);
    }

    public FloatSetting range(float min, float max) {
        this.min = min;
        this.max = max;
        this.increment = 0.1F;
        return this;
    }

    public FloatSetting range(float min, float max, float increment) {
        this.min = min;
        this.max = max;
        this.increment = increment;
        return this;
    }

    @Override
    public Setting<Float> setValue(Float value) {
        return super.setValue(MathUtil.clamp(value, min, max));
    }
}
