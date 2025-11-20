package relake.module.implement.player;

import relake.menu.ui.components.module.setting.MultiSelectComponent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.MultiSelectSetting;
import relake.settings.implement.SelectSetting;

public class NoPushModule extends Module {

    public final MultiSelectSetting selectComponent = new MultiSelectSetting("Выбор")
            .setValue("Вода",
                    "Блоки",
                    "Игроки");

    public NoPushModule() {
        super("No Push", "Игнорирует отталкивание при столкновениях", "Ignores collision repulsion", ModuleCategory.Player);
        registerComponent(selectComponent);
        selectComponent.getSelected().add("Блоки");
        selectComponent.getSelected().add("Игроки");

    }
}
