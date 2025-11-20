package relake.module.implement.render;

import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;

public class NoRenderModule extends Module {

    public final Setting<Boolean> entityArmor = new BooleanSetting("Броня на существах")
            .setValue(true);

    public final Setting<Boolean> fire = new BooleanSetting("Огонь")
            .setValue(true);

    public final Setting<Boolean> entityFire = new BooleanSetting("Огонь на существах")
            .setValue(true);

    public final Setting<Boolean> hurtCam = new BooleanSetting("Тряска камеры")
            .setValue(true);

    public final Setting<Boolean> totem = new BooleanSetting("Тотем")
            .setValue(true);

    public final Setting<Boolean> badEffects = new BooleanSetting("Плохие эффекты")
            .setValue(true);

    public final Setting<Boolean> bossBar = new BooleanSetting("Босс-бар")
            .setValue(false);

    public final Setting<Boolean> handShake = new BooleanSetting("Тряска рук")
            .setValue(false);

    public final Setting<Boolean> scoreboard = new BooleanSetting("Скорборд")
            .setValue(false);

    public final Setting<Boolean> allParticles = new BooleanSetting("Все частицы")
            .setValue(false);

    public NoRenderModule() {
        super("No Render", "Удаляет всяческие мешающие элементы игры", "Removes all kinds of interfering elements of the game", ModuleCategory.Render);
        registerComponent(entityArmor, fire, entityFire, hurtCam, totem, badEffects, bossBar, handShake, scoreboard, allParticles);
    }
}
