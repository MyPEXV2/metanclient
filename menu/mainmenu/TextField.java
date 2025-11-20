package relake.menu.mainmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.lwjgl.glfw.GLFW;
import relake.common.util.MathUtil;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;

import static relake.common.InstanceAccess.mw;

@Getter
@Setter
@RequiredArgsConstructor
public class TextField {
    private String value;

    private final StringBuilder inputBuffer = new StringBuilder();

    private boolean focused = false;

    private int cursorPosition = 0;
    private int selectionStart = -1;
    private int selectionEnd = -1;

    public float x;
    public float y;
    public float width;
    public float height;

    public TextField box(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        ShapeRenderer box = Render2D.box(matrixStack, x, y, width, height);
        box.quad(10, 0xFF151516);

        boolean typing = inputBuffer.isEmpty() && !focused;

        if (!typing) {
            if (hasSelection()) {
                float startX = Render2D.size(FontRegister.Type.BOLD, 2).getWidth(inputBuffer.substring(0, selectionStart));
                float endX = Render2D.size(FontRegister.Type.BOLD, 12).getWidth(inputBuffer.substring(0, selectionEnd)) + 4;

                ShapeRenderer selection = Render2D.box(matrixStack, x + 5 + startX, y + 33, endX, 16);
                selection.quad(0, 0x551E90FF);
            }

            if (focused) {
                int cursorX = (int) (x + Render2D.size(FontRegister.Type.BOLD, 12).getWidth(inputBuffer.substring(0, cursorPosition))) + 5;
                Render2D.box(matrixStack, cursorX, y + 11, 1, 16).quad(0, -1);
            }
        } else {
            clearSelection();
        }

        String displayText = typing ? "Name..." : inputBuffer.toString();

        Render2D.size(FontRegister.Type.BOLD, 12).string(matrixStack, displayText, x + 5, y + 11, -1, (int) width);
    }


    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        focused = MathUtil.isHovered(mouseX, mouseY, x, y, width, height) && button == 0;
        return focused;
    }


    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (focused) {

            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                focused = false;
            }

            switch (keyCode) {
                case GLFW.GLFW_KEY_LEFT -> moveCursor(-1);
                case GLFW.GLFW_KEY_RIGHT -> moveCursor(1);
                case GLFW.GLFW_KEY_BACKSPACE -> deleteText(-1);
                case GLFW.GLFW_KEY_DELETE -> deleteText(0);
            }

            return true;
        }
        return false;
    }

    private void moveCursor(int dir) {
        cursorPosition = Math.max(0, Math.min(cursorPosition + dir, inputBuffer.length()));
        if (!isShiftPressed()) {
            clearSelection();
        } else {
            selectionEnd = cursorPosition;
        }
    }

    private boolean isShiftPressed() {
        return GLFW.glfwGetKey(mw.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        boolean isValidChar = Character.toString(codePoint).matches("[a-zA-Z0-9_]");

        if (focused && isValidChar) {
            if (inputBuffer.length() >= 16) {
                return false;
            }

            if (hasSelection()) {
                inputBuffer.delete(selectionStart, selectionEnd);
                cursorPosition = 0;
                clearSelection();
            }

            if (Render2D.size(FontRegister.Type.BOLD, 12).getWidth(inputBuffer.toString() + codePoint) < width - 8) {
                inputBuffer.insert(cursorPosition++, codePoint);
                value = inputBuffer.toString();
            }
            return true;
        }
        return false;
    }

    private void deleteText(int offset) {
        if (hasSelection()) {
            inputBuffer.delete(selectionStart, selectionEnd);
            cursorPosition = selectionStart;
        } else if (offset != 0 && cursorPosition > 0) {
            inputBuffer.deleteCharAt(cursorPosition - 1);
            cursorPosition--;
        } else if (offset == 0 && cursorPosition < inputBuffer.length()) {
            inputBuffer.deleteCharAt(cursorPosition);
        }
        value = inputBuffer.toString();
        clearSelection();
    }

    private boolean hasSelection() {
        return selectionStart != -1 && selectionEnd != -1 && selectionStart != selectionEnd;
    }

    private void clearSelection() {
        selectionStart = selectionEnd = -1;
    }
}
