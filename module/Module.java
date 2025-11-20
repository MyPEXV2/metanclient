package relake.module;

import com.mojang.text2speech.Narrator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.lwjgl.glfw.GLFW;
import relake.Client;
import relake.animation.tenacity.Animation;
import relake.animation.tenacity.Direction;
import relake.common.InstanceAccess;
import relake.common.util.SoundUtil;
import relake.menu.ui.components.Component;
import relake.menu.ui.components.module.setting.*;
import relake.module.implement.render.NotificationsModule;
import relake.notification.NotificationManager;
import relake.notification.NotificationType;
import relake.settings.Setting;
import relake.settings.implement.*;


import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class Module implements InstanceAccess {
    public final List<Setting<?>> settings = new ArrayList<>();
    private final List<Component> components = new ArrayList<>();
    private final Animation animation = new Animation(1, Duration.ofMillis(150))
            .setDirection(Direction.BACKWARD);

    private final String name, descRU, descENG;

    public String getDesc(boolean russianLanguage, boolean moduleNamePrefix) {
        String descPrefix = moduleNamePrefix ? (russianLanguage ? "Модуль " + this.getName() + " " : "Module " + this.getName() + " ") : "";
        String moduleDesc = russianLanguage ? descRU : descENG;
        return descPrefix + moduleDesc;
    }

    public void sayDesc(boolean russianLanguage, boolean moduleNamePrefix) {
        String desc = this.getDesc(russianLanguage, moduleNamePrefix);
        SoundUtil.playSound("saydescaction.wav", .35F);
        Narrator.getNarrator().say(desc, true);
    }

    private final ModuleCategory moduleCategory;

    private int key = GLFW.GLFW_KEY_UNKNOWN;
    private boolean enabled;

    public void updateComponent() {
    }

    public void enable() {
        Client.instance.eventManager.register(this);
        animation.switchDirection(true);
        onEnable();
    }

    public void disable() {
        Client.instance.eventManager.unregister(this);
        animation.switchDirection(false);
        onDisable();
    }

    public void onDisable() {
    }

    public void onEnable() {
    }

    public void switchState(boolean enabled, boolean notification) {
        if (this.enabled == enabled)
            return;

        this.enabled = enabled;

        NotificationsModule notificationsModule = Client.instance.moduleManager.notificationsModule;

        if (notificationsModule.isEnabled())
            NotificationManager.send("Module", getName() + " " + (enabled ? "включён" : "выключен"), false, enabled ? Color.GREEN.getRGB() : Color.RED.getRGB(), NotificationType.INFO, 1000);
        if (enabled) enable();
        else disable();

        if (notification && notificationsModule.isEnabled() && notificationsModule.sounds.getValue()) {
            SoundUtil.playSound(enabled ? "enable.wav" : "disable.wav", notificationsModule.volume.getValue());
        }
    }

    public void switchState() {
        switchState(!enabled, true);
    }

    public void registerComponent(Object... object) {
        for (Object obj : object) {
            if (obj instanceof Setting<?> setting) {
                if (setting instanceof BooleanSetting booleanSetting) {
                    components.add(new CheckboxComponent(booleanSetting));
                    settings.add(booleanSetting);
                }

                if (setting instanceof SelectSetting selectSetting) {
                    components.add(new SelectComponent(selectSetting));
                    settings.add(selectSetting);
                }

                if (setting instanceof MultiSelectSetting multiSelectSetting) {
                    components.add(new MultiSelectComponent(multiSelectSetting));
                    settings.add(multiSelectSetting);
                }

                if (setting instanceof FloatSetting floatSetting) {
                    components.add(new SliderComponent(floatSetting));
                    settings.add(floatSetting);
                }

                if (setting instanceof StringSetting stringSetting) {
                    components.add(new TextFieldComponent(stringSetting));
                    settings.add(stringSetting);
                }

                if (setting instanceof DelimiterSetting delimiterSetting) {
                    components.add(new DelimiterComponent(delimiterSetting));
                    settings.add(delimiterSetting);
                }

                if (setting instanceof KeySetting keySetting) {
                    components.add(new BindComponent(keySetting));
                    settings.add(keySetting);
                }

                if (setting instanceof ColorSetting colorSetting) {
                    components.add(new ColorPickerComponent(colorSetting));
                    settings.add(colorSetting);
                }

            } else if (obj instanceof Component component) {
                components.add(component);
            }
        }
    }
}
