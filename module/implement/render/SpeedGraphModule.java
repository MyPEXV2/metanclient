package relake.module.implement.render;

import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.FloatSetting;
import relake.settings.implement.SelectSetting;

public class SpeedGraphModule extends Module {

    public final Setting<Boolean> graphData = new BooleanSetting("Показать результаты").setValue(true);

    public final Setting<Boolean> adaptiveSensitivity = new BooleanSetting("Адаптивный расчёт").setValue(true);

    public final SelectSetting graphSize = new SelectSetting("Размер таблички")
            .setValue("Меньше",
                    "Стандартно",
                    "Больше");

    public final SelectSetting optimizeLevel = new SelectSetting("Приоритет качества")
            .setValue("Производительность",
                    "Сбалансированно",
                    "Качество");

    public SpeedGraphModule() {
        super("Speed Graph", "Добавляет табличку с графиком изменения скорости передвижения", "Adds a sign with a timetable for changing the speed of movement", ModuleCategory.Render);
        registerComponent(optimizeLevel, graphSize, graphData, adaptiveSensitivity);
        graphSize.setSelected("Меньше");
        optimizeLevel.setSelected("Сбалансированно");
    }
}
