package relake.module.implement.combat;

import relake.module.Module;
import relake.module.ModuleCategory;

public class NoPlayerTraceModule extends Module {

    public NoPlayerTraceModule() {
        super("No Player Trace", "Позволяет взаимодействовать с умными блоками сквозь хитбокс игрока или любого другого существа", "Allows you to interact with smart blocks through the player's hitbox or any other creature", ModuleCategory.Combat);
    }
}
