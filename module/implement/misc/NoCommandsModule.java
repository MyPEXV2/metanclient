package relake.module.implement.misc;

import relake.module.Module;
import relake.module.ModuleCategory;

public class NoCommandsModule extends Module {

    public NoCommandsModule() {
        super("No Commands", "Отключает команды клиента", "Disables client commands", ModuleCategory.Misc);
    }
}
