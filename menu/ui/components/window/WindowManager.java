package relake.menu.ui.components.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import relake.animation.tenacity.Direction;
import relake.common.util.MathUtil;
import relake.menu.ui.components.Component;

import java.util.ArrayList;
import java.util.List;

public class WindowManager extends Component {
    private final List<Window> windows = new ArrayList<>();

    public void add(Window window) {
        if (windows.stream().noneMatch(w -> w.getName().contains(window.getName()))) {
            windows.add(window);
            window.getAnimation().switchDirection(true);
        }
    }

    public void delete(Window window) {
        window.getAnimation().switchDirection(false);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        for (int i = 0; i < windows.size(); i++) {
            Window window = windows.get(i);

            matrixStack.push();
            MathUtil.scaleFix(matrixStack, window.x + window.width / 2, window.y + window.height / 2, window.getAnimation().get());

            window.render(matrixStack, mouseX, mouseY, partialTicks);

            matrixStack.pop();

            if (window.getAnimation().isDone(Direction.BACKWARD)) {
                windows.remove(window);

                i--;
            }
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Window window : windows) {
            if (window.isHovered(mouseX, mouseY)) {
                if (window.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            } else {
                delete(window);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Window window : windows) {
            window.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        for (Window window : windows) {
            if (window.mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Window window : windows) {
            if (window.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (Window window : windows) {
            if (window.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }
}
