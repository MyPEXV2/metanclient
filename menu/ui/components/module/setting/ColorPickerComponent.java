package relake.menu.ui.components.module.setting;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.RequiredArgsConstructor;
import relake.Client;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.menu.ui.components.Component;
import relake.menu.ui.components.window.ColorPickerWindow;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;
import relake.render.display.shape.Side;
import relake.settings.implement.ColorSetting;

@RequiredArgsConstructor
public class ColorPickerComponent extends Component {
    private final ColorSetting setting;

    @Override
    public void updateComponent() {
        visible = setting.getVisible().get();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

//        ShapeRenderer debug = Render2D.box(matrixStack, x, y, width, height);
//        debug.quad(0, 0x50FFFFFF);

        int rgb = setting.getValue().getRGB();

        ShapeRenderer color = Render2D.box(matrixStack, x + width - 16, y + 5, 16, 16);
        color.circle(ColorUtil.applyOpacity(rgb, 55));
        color.expand(Side.ALL, -3);
        color.circle(rgb);

        String hexString = Integer.toHexString(setting.getValue().hashCode())
                .replaceFirst("ff", "#")
                .toUpperCase();

        Render2D.size(FontRegister.Type.BOLD, 11).string(matrixStack, hexString, x + width - (16 + 6) - Render2D.size(FontRegister.Type.BOLD, 11).getWidth(hexString), y + 6, 0xFFAAAAAA, 90);
        Render2D.size(FontRegister.Type.BOLD, 11).string(matrixStack, setting.getName(), x, y + 6, -1, 90);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean clicked = MathUtil.isHovered(mouseX, mouseY, x + width - 16, y + 5, 16, 16) && button == 0;

        if (clicked) {
            Client.instance.windowManager.add(
                    new ColorPickerWindow(setting)
                            .pos(x + width + 20, y)
                            .size(150, 140)
            );
        }

        return clicked || super.mouseClicked(mouseX, mouseY, button);
    }
}

