package relake.module.implement.misc;

import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.FloatSetting;

public class ItemScrollerModule extends Module {
    public final Setting<Float> speed = new FloatSetting("Задержка").range(0, 150, 5).setValue(30F);

    public ItemScrollerModule() {
        super("Item Scroller", "Ускоряет перекладывание вещей в инвентаре или других меню", "Speeds up the shuffling of items in inventory or other menus", ModuleCategory.Misc);
        registerComponent(speed);
    }
}
