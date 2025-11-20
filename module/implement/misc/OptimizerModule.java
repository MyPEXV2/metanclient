package relake.module.implement.misc;

import relake.menu.ui.components.module.setting.MultiSelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.MultiSelectSetting;
import relake.settings.implement.SelectSetting;

public class OptimizerModule extends Module {

    public final MultiSelectSetting remove = new MultiSelectSetting("Удалять")
            .setValue("Траву",
                    "Тень",
                    "Партиклы");

    public OptimizerModule() {
        super("Optimizer", "Оптимизирует игру для повышения производительности, может негативно сказаться на восприятии окружения", "Optimizes the game to increase productivity, may negatively affect the perception of the environment", ModuleCategory.Misc);
        registerComponent(remove);
        remove.getSelected().add("Траву");
        remove.getSelected().add("Тень");
        remove.getSelected().add("Партиклы");
    }
}
