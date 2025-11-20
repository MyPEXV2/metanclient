package relake.module.implement.render;

import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.FloatSetting;
import relake.settings.implement.SelectSetting;

public class TargetHUDModule extends Module {


    public final Setting<Boolean> particlesBool = new BooleanSetting("Частички").setValue(true);

    public final SelectSetting mode = new SelectSetting("Мод таргетхуда")
            .setValue("Обычный",
                    "Новый",
                    "Современный");


    public TargetHUDModule() {
        super("Target HUD", "Показывает табличку с информацией о главном враге на текущий момент если он есть", "Shows a sign with information about the main enemy at the moment, if there is one", ModuleCategory.Render);
        registerComponent(mode, particlesBool);
        mode.setSelected("Обычный");
    }
}
