package relake.module.implement.player;

import relake.module.Module;
import relake.module.ModuleCategory;

public class NoServerDesyncModule extends Module {
    public NoServerDesyncModule() {
        super("No Server Desync", "Избегает разсинхронизации с сервером", "Avoids out-of-sync with the server", ModuleCategory.Player);
    }
}

