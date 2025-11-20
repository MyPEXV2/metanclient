package relake.module.implement.render;
import relake.animation.excellent.Animation;
import relake.animation.excellent.util.Easings;
import relake.common.util.StopWatch;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.FloatSetting;
import relake.settings.implement.SelectSetting;

import java.time.Duration;

public class SwingAnimationModule extends Module {

    public final Setting<Float> size = new FloatSetting("Размер")
            .range(0.1f, 1f, 0.1f)
            .setValue(0.1f);

    public final Setting<Float> strength = new FloatSetting("Сила")
            .range(25f, 100f, 5f)
            .setValue(75f);

    public final SelectSetting mode = new SelectSetting("Режимы")
            .setValue("Смахнуть",
                    "Скольжение",
                    "Вниз",
                    "Тычка",
                    "Тряска");

    private Animation fapAnim = new Animation();
    private StopWatch timerSwinging = new StopWatch();
    public float getFapUpdatedAnimation(boolean swinging) {
        if (!swinging) timerSwinging.reset();
        fapAnim.update();
        int delayWave = 200;
        int point1 = timerSwinging.elapsedTime() % delayWave >= delayWave / 2.F ? 0 : 1;
        fapAnim.run(swinging ? point1 : 0.F, .2F);
        fapAnim.setEasing(Easings.QUINT_OUT);
        return fapAnim.get();
    }

    public SwingAnimationModule() {
        super("Swing Animation", "Изменяет анимации взмаха рукой", "Modifies hand wave animations", ModuleCategory.Render);
        registerComponent(mode, size, strength);
        mode.setSelected("Смахнуть");
    }
}
