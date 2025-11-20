package relake.module.implement.render;

import relake.Constants;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;

public class ClientSoundsModule extends Module {

    public final Setting<Boolean> openGUI = new BooleanSetting("Открытие гуи")
            .setValue(true);

    public final Setting<Boolean> checkbox = new BooleanSetting("Чекбоксы")
            .setValue(true);

    public final Setting<Boolean> slider = new BooleanSetting("Слайдеры")
            .setValue(true);

    public final Setting<Boolean> modes = new BooleanSetting("Смена мода")
            .setValue(true);

    public final Setting<Boolean> typeing = new BooleanSetting("Тайпинг")
            .setValue(true);

    public final Setting<Boolean> bind = new BooleanSetting("Бинд")
            .setValue(true);

    public final Setting<Boolean> category = new BooleanSetting("Смена категорий")
            .setValue(true);

    public final Setting<Boolean> cfg = new BooleanSetting("Конфиги")
            .setValue(true);

    public final Setting<Boolean> friends = new BooleanSetting("Друзья")
            .setValue(true);

    public final Setting<Boolean> targets = new BooleanSetting("Выбор таргета")
            .setValue(true);

    public ClientSoundsModule() {
        super("Client Sounds", "Настройка звукового сопровождения в " + Constants.NAME, "Setting up the sounds in " + Constants.NAME, ModuleCategory.Render);
        registerComponent(openGUI, checkbox, slider, modes, typeing, bind, category, cfg, friends, targets);
    }
}
