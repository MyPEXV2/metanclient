package relake.menu.ui.components.module.setting;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lwjgl.glfw.GLFW;
import relake.common.util.MathUtil;
import relake.common.util.SoundUtil;
import relake.common.util.StringUtil;
import relake.menu.ui.components.Component;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;
import relake.render.display.shape.Side;
import relake.settings.implement.KeySetting;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

@Getter
@RequiredArgsConstructor
public class BindComponent extends Component {
    private final KeySetting setting;
    private boolean binding;

    @Override
    public void updateComponent() {
        visible = setting.getVisible().get();
        if (!visible) {
            setting.setValue(GLFW.GLFW_KEY_UNKNOWN);
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Integer value = setting.getValue();

        String keyName = binding ? "..." : value == GLFW.GLFW_KEY_UNKNOWN
                ? "None"
                : StringUtil.getKeyName(value);

        int padding = 5;
        float keyWidth = Render2D.size(FontRegister.Type.BOLD,9).getWidth(keyName);

        ShapeRenderer keys = Render2D.box(matrixStack, x + width - keyWidth - padding, y + 8, keyWidth, 10);
        keys.expand(Side.ALL, padding);
        //keys.quad(5, 0xFF161617);

        Render2D.size(FontRegister.Type.BOLD,9).string(matrixStack, keyName, x + width - keyWidth - padding - 1, y + 7, -1, 90);
        Render2D.size(FontRegister.Type.BOLD,11).string(matrixStack, setting.getName(), x, y + 6, -1, (int) (160 - keys.width));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height)) {
            if(!binding && button == 2 ){
                setting.setValue(GLFW.GLFW_KEY_UNKNOWN);
            }

            if (button == 0) {
                binding = !binding;
            }
        } else {
            binding = false;
        }

        if (binding && button > 1) {
            setting.setValue(button);
            binding = false;
        }

        return binding;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            setting.setValue(keyCode);
            binding = false;
            if (keyCode == GLFW_KEY_ESCAPE) {
                setting.setValue(-1);
            }
        }
        return binding;
    }
}
