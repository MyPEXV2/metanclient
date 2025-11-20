package relake.module.implement.render;

import org.lwjgl.system.CallbackI;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.FloatSetting;

public class NotificationsModule extends Module {

    public final Setting<Boolean> sounds = new BooleanSetting("Звуки")
            .setValue(false);

    public final Setting<Float> volume = new FloatSetting("Громкость")
            .range(0.1F, 1.F)
            .setValue(.8F);

    public NotificationsModule() {
        super("Notifications", "Показывает уведомления", "Shows notifications", ModuleCategory.Render);
        registerComponent(sounds, volume);
    }
}
