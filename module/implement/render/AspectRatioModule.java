package relake.module.implement.render;

import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.FloatSetting;

public class AspectRatioModule extends Module {
    public final Setting<Float> width = new FloatSetting("Ширина").range(0, 0.75F).setValue(0.25F);

    public AspectRatioModule() {
        super("Aspect Ratio", "Изменяет соотношение сторон экрана", "Changes the aspect ratio of the screen", ModuleCategory.Render);
        registerComponent(width);
    }
}
