package relake.menu.ui.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import relake.Client;
import relake.common.util.MathUtil;
import relake.common.util.SoundUtil;
import relake.common.util.StopWatch;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;

@Getter
public class SearchComponent extends Component {


    private String value;

    private final StringBuilder inputBuffer = new StringBuilder();

    public boolean focused = false;

    private int cursorPosition = 0;
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private final StopWatch backspaceTimer = new StopWatch();

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (focused) {
            Client.instance.moduleManager.screenWalkModule.setTempStop();
            if (backspaceTimer.finished(125) && GLFW.glfwGetKey(mw.getHandle(), GLFW.GLFW_KEY_BACKSPACE) == GLFW.GLFW_PRESS) {
                if (Client.instance.moduleManager.clientSoundsModule.typeing.getValue() && !value.isEmpty())
                    SoundUtil.playSound("searchtyping.wav", 0.1f);
                deleteText(-1);
                backspaceTimer.reset();
            }
        }

        ShapeRenderer textField = Render2D.box(matrixStack, x, y + height, width, 1);
        textField.quad(0, 0x405F5F5F);

        boolean typing = inputBuffer.isEmpty() && !focused;

        if (!typing) {
            if (hasSelection()) {
                float startX = Render2D.size(FontRegister.Type.BOLD, 2).getWidth(inputBuffer.substring(0, selectionStart));
                float endX = Render2D.size(FontRegister.Type.BOLD, 12).getWidth(inputBuffer.substring(0, selectionEnd)) + 4;

                ShapeRenderer selection = Render2D.box(matrixStack, x + 2 + startX, y + 3, endX, 16);
                selection.quad(0, 0x551E90FF);
            }

            if (focused) {
                int cursorX = (int) (x + Render2D.size(FontRegister.Type.BOLD, 12).getWidth(inputBuffer.substring(0, cursorPosition))) + 2;
                Render2D.box(matrixStack, cursorX, y + 3, 1, 16).quad(0, -1);
            }
        } else {
            clearSelection();
        }

        String displayText = typing ? "Search..." : inputBuffer.toString();

        Render2D.size(FontRegister.Type.BOLD, 12).string(matrixStack, displayText, x, y + 3, -1, 135);
    }

    public void clearText() {
        selectionStart = 0;
        cursorPosition = selectionEnd = inputBuffer.length();
        deleteText(0);
        focused = false;
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
                    value = inputBuffer.toString();
                }
            }
            case GLFW.GLFW_KEY_A -> {
                selectionStart = 0;
                cursorPosition = selectionEnd = inputBuffer.length();
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        focused = MathUtil.isHovered(mouseX, mouseY, x, y, width, height) && button == 0;
        if (focused) {
            if (Client.instance.moduleManager.clientSoundsModule.typeing.getValue())
                SoundUtil.playSound("searchfield.wav", 0.1f);
        }
        return focused || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (focused) {
            if (GLFW.glfwGetKey(mw.getHandle(), GLFW.GLFW_KEY_BACKSPACE) != GLFW.GLFW_PRESS) {
                if (Client.instance.moduleManager.clientSoundsModule.typeing.getValue())
                    SoundUtil.playSound("searchtyping.wav", 0.1f);
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
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

    private void moveCursor(int dir) {
        cursorPosition = Math.max(0, Math.min(cursorPosition + dir, inputBuffer.length()));
        if (!isShiftPressed()) {
            clearSelection();
        } else {
            selectionEnd = cursorPosition;
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        boolean isValidChar = !Character.toString(codePoint).matches("[^а-яА-ЯёЁa-zA-Z0-9]");

        if (focused && isValidChar) {
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
        return super.charTyped(codePoint, modifiers);
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

    private boolean isShiftPressed() {
        return GLFW.glfwGetKey(mw.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
    }

    private String textFilter(String text) {
        return text.replaceAll("[^а-яА-ЯёЁa-zA-Z0-9]", "");
    }

    private void clearSelection() {
        selectionStart = selectionEnd = -1;
    }
}
