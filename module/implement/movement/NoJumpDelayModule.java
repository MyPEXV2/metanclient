package relake.module.implement.movement;

import relake.event.EventHandler;
import relake.event.impl.player.PlayerEvent;
import relake.module.Module;
import relake.module.ModuleCategory;

public class NoJumpDelayModule extends Module {
    public NoJumpDelayModule() {
        super("No Jump Delay", "Убирает задержку на прыжок при зажатом пробеле", "Removes the jump delay when the space bar is clamped", ModuleCategory.Movement);
    }

    @EventHandler
    public void player(PlayerEvent playerEvent) {
        mc.player.jumpTicks = 1;
    }
}
