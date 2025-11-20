package relake.menu.ui.components.module.setting;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lwjgl.glfw.GLFW;
import relake.Client;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.common.util.SoundUtil;
import relake.common.util.StopWatch;
import relake.menu.ui.components.Component;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;
import relake.settings.implement.StringSetting;

@Getter
@RequiredArgsConstructor
public class TextFieldComponent extends Component {

    private final StringSetting setting;

    private final StringBuilder inputBuffer = new StringBuilder();

    private boolean focused = false;
    private int cursorPosition = 0;
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private final StopWatch backspaceTimer = new StopWatch();

    @Override
    public void updateComponent() {
        visible = setting.getVisible().get();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

        if (focused) {
            Client.instance.moduleManager.screenWalkModule.setTempStop();
            if (backspaceTimer.finished(125) && GLFW.glfwGetKey(mw.getHandle(), GLFW.GLFW_KEY_BACKSPACE) == GLFW.GLFW_PRESS) {
                deleteText(-1);
                SoundUtil.playSound("searchtyping.wav", 0.1f);
                backspaceTimer.reset();
            }
        }

        height = height + 33;

        Render2D.size(FontRegister.Type.BOLD, 11).string(matrixStack, setting.getName(), x, y + 6, -1, 90);

        ShapeRenderer textField = Render2D.box(matrixStack, x, y + 28, width, 25);
        //textField.quad(15, 0xFF181819);
        textField.quad(10, 0x4F111112);

        boolean typing = inputBuffer.isEmpty() && !focused;

        if (!typing) {
            if (hasSelection()) {
                float startX = Render2D.size(FontRegister.Type.BOLD, 11).getWidth(inputBuffer.substring(0, selectionStart));
                float endX = Render2D.size(FontRegister.Type.BOLD, 11).getWidth(inputBuffer.substring(0, selectionEnd)) + 4;

                ShapeRenderer selection = Render2D.box(matrixStack, x + 5 + startX, y + 33, endX, 16);
                selection.quad(0, 0x551E90FF);
            }

            if (focused) {
                int cursorX = (int) (x + 4 + Render2D.size(FontRegister.Type.BOLD, 11).getWidth(inputBuffer.substring(0, cursorPosition))) + 2;
                Render2D.box(matrixStack, cursorX, y + 33, 1, 16).quad(0, -1);
            }
        } else {
            clearSelection();
        }

        String displayText = typing ? setting.getValue() : inputBuffer.toString();

        Render2D.size(FontRegister.Type.BOLD, 11).string(matrixStack, displayText, x + 5, y + 33, ColorUtil.getColor(focused ? 255 : 175, 255), 135);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        focused = MathUtil.isHovered(mouseX, mouseY, x, y + 30, width, 30) && button == 0;
        return focused || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (focused) {
            if (GLFW.glfwGetKey(mw.getHandle(), GLFW.GLFW_KEY_BACKSPACE) != GLFW.GLFW_PRESS && GLFW.glfwGetKey(mw.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) != GLFW.GLFW_PRESS) {
                SoundUtil.playSound("searchtyping.wav", 0.1f);
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                focused = false;
            }

            if (GLFW.glfwGetKey(mw.getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(mw.getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS) {
                handleCtrlKey(keyCode);
            } else {
                switch (keyCode) {
                    case GLFW.GLFW_KEY_LEFT -> moveCursor(-1);
                    case GLFW.GLFW_KEY_RIGHT -> moveCursor(1);
                    case GLFW.GLFW_KEY_DELETE -> deleteText(0);
                }
            }

            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {

        //wtf
        boolean isValidChar = !Character.toString(codePoint).matches(setting.getPattern().pattern());

        if (focused && isValidChar) {
            if (hasSelection()) {
                inputBuffer.delete(selectionStart, selectionEnd);
                cursorPosition = 0;
                clearSelection();
            }

            if (Render2D.size(FontRegister.Type.BOLD, 11).getWidth(inputBuffer.toString() + codePoint) < width - 8) {
                inputBuffer.insert(cursorPosition++, codePoint);
                setting.setValue(inputBuffer.toString());
            }
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void moveCursor(int dir) {
        cursorPosition = Math.max(0, Math.min(cursorPosition + dir, inputBuffer.length()));
        if (!isShiftPressed()) {
            clearSelection();
        } else {
            selectionEnd = cursorPosition;
        }
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
        setting.setValue(inputBuffer.toString());
        clearSelection();
    }

    private void handleCtrlKey(int key) {
        switch (key) {
            case GLFW.GLFW_KEY_C -> {
                if (hasSelection()) {
                    mc.keyboardListener.setClipboardString(inputBuffer.substring(selectionStart, selectionEnd));
                }
            }
            case GLFW.GLFW_KEY_V -> {

                if (hasSelection()) {
                    inputBuffer.delete(selectionStart, selectionEnd);
                    cursorPosition = 0;
                    clearSelection();
                }

                String pasteText = textFilter(mc.keyboardListener.getClipboardString());
                if (cursorPosition > inputBuffer.length()) {
                    cursorPosition = inputBuffer.length();
                }

                String currentText = inputBuffer.toString();
                String newText = currentText.substring(0, cursorPosition) + pasteText + currentText.substring(cursorPosition);

                if (Render2D.size(FontRegister.Type.BOLD, 11).getWidth(newText) < width - 8) {
                    inputBuffer.insert(cursorPosition, pasteText);
                    cursorPosition += pasteText.length();
                    setting.setValue(inputBuffer.toString());
                }
            }
            case GLFW.GLFW_KEY_A -> {
                selectionStart = 0;
                cursorPosition = selectionEnd = inputBuffer.length();
            }
        }
    }

    private boolean hasSelection() {
        return selectionStart != -1 && selectionEnd != -1 && selectionStart != selectionEnd;
    }

    private void clearSelection() {
        selectionStart = selectionEnd = -1;
    }

    private boolean isShiftPressed() {
        return GLFW.glfwGetKey(mw.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
    }

    private String textFilter(String text) {
        return text.replaceAll(setting.getPattern().pattern(), "");
    }
}
