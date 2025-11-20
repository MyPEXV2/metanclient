package relake.module.implement.player;

import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;

public class NoInteractModule extends Module {

    public final Setting<Boolean> auraOnly = new BooleanSetting("Только с аурой")
            .setValue(false);

    public NoInteractModule() {
        super("No Interact", "Игнорирует клики по умным блокам", "Ignores clicks on smart blocks", ModuleCategory.Player);
        registerComponent(auraOnly);
    }
}
