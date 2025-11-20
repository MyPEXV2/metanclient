package relake.module.implement.misc;

import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.SelectSetting;

public class RTXSoundsModule extends Module {

    public final SelectSetting perfomancePriority = new SelectSetting("Приоритет качества")
            .setValue("Производительность",
                    "Качество звука");


    public final Setting<Boolean> betterStereo = new BooleanSetting("Эмуляция 3D стерео").setValue(true);
    public final Setting<Boolean> toneCompense = new BooleanSetting("Тон-компенсация").setValue(true);

    public RTXSoundsModule() {
        super("RTX Sounds", "Невероятно улучшает погружение в игру путём улучшения качества звука, звук становится полноценоо трёхмерным с учитыванием окружения", "Incredibly improves the immersion in the game by improving the sound quality, the sound becomes fully three-dimensional, taking into account the environment", ModuleCategory.Misc);
        registerComponent(perfomancePriority, betterStereo, toneCompense);
        perfomancePriority.setSelected("Качество звука");
    }
}
