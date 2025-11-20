package relake.module.implement.render;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import relake.Client;
import relake.common.util.ColorUtil;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;

import java.awt.*;

public class GlowESPModule extends Module {
    private final Setting<Boolean> hpColor = new BooleanSetting("Цвет от хп").setValue(false);
    public GlowESPModule() {
        super("Glow ESP", "Выделяет игроков гловом", "Highlights the players by glow", ModuleCategory.Render);
        registerComponent(hpColor);
    }

    public int getGlowColor(Entity entity, int prevColor) {
        if (!this.isEnabled()) return prevColor;
        if (entity instanceof PlayerEntity player && !player.equals(mc.player)) {
            if (hpColor.getValue()) prevColor = Color.getHSBColor(Math.min(Math.max(player.getHealth() - 3.5F, 0.F) / (player.getMaxHealth() - 3.5F), 1.F) * .37F, .81F, 1.F).getRGB();
            else prevColor = Client.instance.moduleManager.hudModule.color.getValue().getRGB();
        }
        return prevColor;
    }
}
