package relake.menu.ui.components.module.setting;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.util.math.MathHelper;
import relake.Client;
import relake.animation.excellent.util.Easings;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.common.util.SoundUtil;
import relake.menu.ui.components.Component;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;
import relake.settings.implement.BooleanSetting;

@Getter
@RequiredArgsConstructor
public class CheckboxComponent extends Component {
    private final relake.animation.excellent.Animation eAnimation = new relake.animation.excellent.Animation();
    private final relake.animation.excellent.Animation sizeAnimation = new relake.animation.excellent.Animation();
    private final BooleanSetting setting;
    @Setter
    private float alpha = 1f;

    @Override
    public void init() {
        eAnimation.set(setting.getValue() ? 1 : 0);
    }

    @Override
    public void updateComponent() {
        visible = setting.getVisible().get();

        if (!visible) {
            setting.setValue(false);
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

        eAnimation.update();
        sizeAnimation.update();
        eAnimation.run(setting.getValue() ? 1 : 0, 0.5F, Easings.BACK_OUT, false);
        sizeAnimation.run((eAnimation.get() > 0.35F && eAnimation.get() < 0.65F) ? 8F : 0, 0.05F, Easings.LINEAR, false);

        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

        float alphaPC = alpha;
        float opacity = MathHelper.clamp(Math.round((eAnimation.get() * 255) * alphaPC), 0, 255);

        int[] color = {
                ColorUtil.applyOpacity(rgb, opacity),
                ColorUtil.applyOpacity(rgb, opacity),
                ColorUtil.applyOpacity(ColorUtil.darker(rgb, 100), opacity),
                ColorUtil.applyOpacity(ColorUtil.darker(rgb, 100), opacity)
        };

        ShapeRenderer checkbox = Render2D.box(matrixStack, x + width - 28, y + 5, 28, 15);
        checkbox.quad(7, ColorUtil.multAlpha(0x4F111112, alphaPC));
        checkbox.quad(7, color);

        ShapeRenderer point = Render2D.box(matrixStack, (eAnimation.get() * 13) + (checkbox.x + 2) - sizeAnimation.get(), checkbox.y + 2, 11 + sizeAnimation.get() * 2F, 11);
        point.quad(5, ColorUtil.multAlpha(0xFF252525, alphaPC));
        point.quad(5, ColorUtil.applyOpacity(-1, opacity));

        Render2D.size(FontRegister.Type.BOLD, 11).string(matrixStack, setting.getName(), x, y + 6, ColorUtil.multAlpha(-1, alphaPC), 110);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean clicked = MathUtil.isHovered(mouseX, mouseY, x + width - 28, y + 5, 28, 15) && button == 0;

        if (clicked) {
            if (Client.instance.moduleManager.clientSoundsModule.checkbox.getValue())
                SoundUtil.playSound(setting.getValue() ? "enablecb.wav" : "disablecb.wav", 0.1f);
            setting.setValue(!setting.getValue());
        }

        return clicked || super.mouseClicked(mouseX, mouseY, button);
    }
}
