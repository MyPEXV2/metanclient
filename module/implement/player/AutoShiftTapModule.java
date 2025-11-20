package relake.module.implement.player;

import net.minecraft.client.settings.KeyBinding;
import relake.Client;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.event.impl.player.AttackEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;

public class AutoShiftTapModule extends Module {

    private final Setting<Boolean> auraOnly = new BooleanSetting("Только с аурой")
            .setValue(false);

    public AutoShiftTapModule() {
        super("Auto Shift Tap", "Тапает шифт при ударах", "The shift stomps on impact", ModuleCategory.Player);
        registerComponent(auraOnly);
    }

    private boolean isShifted;
    private int ticks;

    @EventHandler
    public void attack(AttackEvent event) {
        if (auraOnly.getValue() && Client.instance.moduleManager.attackAuraModule.getTarget() == null) {
            return;
        }

        setSneaking(true);
    }

    @EventHandler
    public void tick(TickEvent event) {
        if (isShifted) ticks++;

        if (ticks >= 4) {
            setSneaking(false);
            ticks = 0;
        }
    }

    public void setSneaking(boolean shift) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.keyCode, shift);
        isShifted = shift;
    }

    @Override
    public void enable() {
        super.enable();
        ticks = 0;
        isShifted = false;
    }

    @Override
    public void disable() {
        super.disable();

        if (mc.player != null) {
            if (mc.player.isSneaking() && isShifted) {
                setSneaking(false);
            }
        }
    }
}
