package relake.module.implement.movement;

import relake.common.util.MoveUtil;
import relake.event.EventHandler;
import relake.event.impl.player.PlayerEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.FloatSetting;
import relake.settings.implement.SelectSetting;

public class FlightModule extends Module {

    public final Setting<Float> speed = new FloatSetting("Скорость глайда")
            .range(.1F, 15.F)
            .setValue(5F);


    public final SelectSetting mode = new SelectSetting("Обход")
            .setValue("Ванильный",
                    "Глайд");

    private boolean flyState;

    public FlightModule() {
        super("Flight", "Позволяет летать как с ракетой в заднице", "Allows you to fly like a rocket in the ass", ModuleCategory.Movement);
        registerComponent(mode);
        mode.setSelected("Глайд");
    }

    @EventHandler
    public void player(PlayerEvent event) {
        if (mode.isSelected("Глайд")) handleGlide();
    }

    private void handleGlide() {
        if (mc.player.isOnGround()) {
            mc.player.jump();
        } else {
            mc.player.motion.x = 0;
            mc.player.motion.z = 0;
            mc.player.motion.y = -0.01;
            MoveUtil.setSpeed(speed.getValue());
            mc.player.speedInAir = 0.3f;
            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.player.motion.y -= speed.getValue();
            } else if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.motion.y += speed.getValue();
            }
        }
    }

    @Override
    public void enable() {
        super.enable();
        if (mode.isSelected("Ванильный")) {
            flyState = mc.player.abilities.allowFlying;

            mc.player.abilities.isFlying = true;
            mc.player.abilities.allowFlying = true;
        }
    }

    @Override
    public void disable() {
        super.disable();

        if (mc.player != null) {
            mc.player.speedInAir = 0.02f;

            if ((mode.isSelected("Ванильный"))) {
                mc.player.abilities.isFlying = flyState;
                mc.player.abilities.allowFlying = flyState;
            }

            if ((mode.isSelected("Глайд"))) {
                mc.player.motion.x = 0;
                mc.player.motion.y = 0;
                mc.player.motion.z = 0;
            }
        }
    }
}
