package relake.settings.implement;

import lombok.Getter;
import lombok.Setter;
import relake.settings.Setting;

import java.awt.*;

import static net.minecraft.util.ColorHelper.PackedColor.*;

@Getter
@Setter
public class ColorSetting extends Setting<Color> {
    private float hue = 0,
            saturation = 1,
            brightness = 1;

    public ColorSetting(String name) {
        super(name);
    }

    public Setting<Color> setValue(int value) {

        float[] hsb = Color.RGBtoHSB(
                getRed(value),
                getGreen(value),
                getBlue(value),
                null
        );

        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];

        return super.setValue(Color.getHSBColor(hue, saturation, brightness));
    }
}
