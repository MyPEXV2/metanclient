package relake.menu.ui.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import relake.common.InstanceAccess;

public class Component implements InstanceAccess {
    public float x, y, width, height;
    public boolean visible = true;
    public void updateComponent() {
    }
    public void init() {
    }
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    public void onClose() {
    }
}
