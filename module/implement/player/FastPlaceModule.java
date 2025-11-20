package relake.module.implement.player;

import relake.event.EventHandler;
import relake.event.impl.player.PlayerEvent;
import relake.module.Module;
import relake.module.ModuleCategory;

public class FastPlaceModule extends Module {

    public FastPlaceModule() {
        super("Fast Place", "Убирает задержку использования предметов и блоков при зажатии правой кнопкой мыши", "Removes the delay in using objects and blocks when right-clicking", ModuleCategory.Player);
    }

    @EventHandler
    public void player(PlayerEvent playerEvent) {
        mc.rightClickDelayTimer = 0;
    }

    @Override
    public void disable() {
        mc.rightClickDelayTimer = 4;
        super.disable();
    }
}
