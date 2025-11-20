package relake.common.util;

import lombok.experimental.UtilityClass;
import relake.menu.ui.components.Component;
import relake.menu.ui.components.module.setting.*;
import relake.settings.Setting;
import relake.settings.implement.*;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ComponentUtil {
    public List<Component> registerComponent(Object... objects) {
        List<Component> components = new ArrayList<>();

        for (Object obj : objects) {
            if (obj instanceof Setting<?> setting) {

                if (setting instanceof BooleanSetting booleanSetting) {
                    components.add(new CheckboxComponent(booleanSetting));
                }

                if (setting instanceof FloatSetting floatSetting) {
                    components.add(new SliderComponent(floatSetting));
                }

                if (setting instanceof StringSetting stringSetting) {
                    components.add(new TextFieldComponent(stringSetting));
                }
                if (setting instanceof DelimiterSetting delimiterComponent) {
                    components.add(new DelimiterComponent(delimiterComponent));
                }
                if (setting instanceof KeySetting keySetting) {
                    components.add(new BindComponent(keySetting));
                }

                if (setting instanceof ColorSetting colorSetting) {
                    components.add(new ColorPickerComponent(colorSetting));
                }

            } else if (obj instanceof Component component) {
                components.add(component);
            }
        }


        return components;
    }
}
