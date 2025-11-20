package relake.menu.ui.components.module;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import relake.Client;
import relake.animation.excellent.Animation;
import relake.animation.excellent.util.Easings;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.common.util.SoundUtil;
import relake.common.util.StringUtil;
import relake.menu.ui.components.Component;
import relake.module.Module;
import relake.module.implement.render.ClientSoundsModule;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;
import relake.render.display.shape.Side;

import java.time.Duration;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

@Getter
public class ModuleComponent extends Component {

    ClientSoundsModule clientSoundsModule = Client.instance.moduleManager.clientSoundsModule;

    private final Animation descAnimation = new Animation();

    private final Module module;
    private boolean binding;

    public ModuleComponent(Module module) {
        this.module = module;
    }

    @Override
    public void init() {
        super.init();
        for (Component component : module.getComponents()) {
            component.init();
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        for (Component component : module.getComponents()) {
            component.onClose();
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        float componentHeight = 55;
        descAnimation.update();
        descAnimation.setEasing(Easings.QUAD_IN);
        if (descAnimation.getToValue() == 1 && descAnimation.isFinished())
        descAnimation.run(0, .85F);
        module.updateComponent();
        for (Component component : module.getComponents()) {
            if (component.visible) {
                componentHeight += component.height;
            }
        }
        height = module.getComponents().isEmpty() ? 40 : componentHeight;

        ShapeRenderer box = Render2D.box(matrixStack, x, y, width, height);

        float[] round = module.getComponents().isEmpty()
                ? new float[]{10, 10, 10, 10}
                : new float[]{10, 0, 10, 0};

        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

        float opacity = module.getAnimation().get() * 255;

        int[] color = {
                ColorUtil.applyOpacity(rgb, opacity),
                ColorUtil.applyOpacity(rgb, opacity),
                ColorUtil.applyOpacity(ColorUtil.darker(rgb, 100), opacity),
                ColorUtil.applyOpacity(ColorUtil.darker(rgb, 100), opacity)
        };


        int value = module.getKey();

        String keyName = binding ? "..." : StringUtil.getKeyName(value);
        float keyWidth = Render2D.size(FontRegister.Type.BOLD, 9).getWidth(keyName);

        int padding = 5;

        ShapeRenderer keys = Render2D.box(matrixStack, x + width - keyWidth - padding - 10, y + 15, keyWidth, 10);

        if (value != GLFW.GLFW_KEY_UNKNOWN || binding) {
            keys.expand(Side.ALL, padding);
            //keys.quad(5, 0xFF191919);

            Render2D.size(FontRegister.Type.BOLD, 9).string(matrixStack, keyName, x + width - keyWidth - padding - 11, y + 14, -1, 90);
        }

        //main bg
        box.quad(10, ColorUtil.applyOpacity(rgb, 35), ColorUtil.applyOpacity(rgb, 5), ColorUtil.applyOpacity(rgb, 5), ColorUtil.applyOpacity(rgb, 35));
        box.quad(10, 0x7E111112);

        //hat
        ShapeRenderer section = Render2D.box(matrixStack, x, y, width, 40);
        section.corner(round, 0x5B111112);

        ShapeRenderer outBox = Render2D.box(matrixStack, x - 2, y - 2, width + 4, height + 4);
        outBox.outlineHud(10, 3, ColorUtil.applyOpacity(rgb, 75), 0x5B111112, 0x5B111112, ColorUtil.applyOpacity(rgb, 75));

        //button
        boolean hoverModule = MathUtil.isHovered(mouseX, mouseY, x + 10, y + 10, 20, 20);
        ShapeRenderer button = Render2D.box(matrixStack, x + 10, y + 10, 20, 20);
        button.quad(7, 0x5B111112);
        if (opacity < 254) {
            button.expand(Side.ALL, -3);
            button.outline(6, 1, ColorUtil.getColor(ColorUtil.getRedFromColor(rgb), ColorUtil.getGreenFromColor(rgb), ColorUtil.getBlueFromColor(rgb), hoverModule ? 80 : 35));
            button.expand(Side.ALL, -3);
            button.outline(5, 1, ColorUtil.getColor(ColorUtil.getRedFromColor(rgb), ColorUtil.getGreenFromColor(rgb), ColorUtil.getBlueFromColor(rgb), hoverModule ? 45 : 20));
            button.expand(Side.ALL, 6);
        }
        button.quad(7, color);
        button.expand(Side.ALL, -5);
        button.circle(ColorUtil.applyOpacity(-1, opacity));

        //button desc
        float descAnimPC = Math.min(descAnimation.get() + (MathUtil.isHovered(mouseX, mouseY, x + 35, y + 10, 20, 20) && descAnimation.get() == 0 ? .2F : 0.F), 1.F);
        ShapeRenderer button2 = Render2D.box(matrixStack, x + 35, y + 10, 20, 20);
        button2.expand(Side.ALL, -2);
        button2.quad((int) (5 * (1.F + descAnimPC)), 0x5B111112);
        button2.expand(Side.ALL, 2);
        int colDesc = ColorUtil.multAlpha(rgb, .25F + descAnimPC * .75F);
        button2.outline(7, 1, colDesc);
        if (descAnimPC > 0) {
            button2.outline(4, (int) (1 + descAnimPC * 7.F), ColorUtil.multAlpha(colDesc, .4F));
        }
        Render2D.size(FontRegister.Type.BOLD, 13).string(matrixStack, "?", button2.x + button2.width / 2.F - Render2D.size(FontRegister.Type.BOLD, 13).getWidth("?") / 2.F - 2.F, button2.y + button2.height / 2.F - 8.5F, ColorUtil.getOverallColorFrom(colDesc, -1, descAnimPC));

        float yOffset = y + 45;
        for (Component component : module.getComponents()) {
            component.updateComponent();
            if (component.visible) {
                component.x = x + 10;
                component.y = yOffset;
                component.width = width - 20;
                component.height = 27;

                component.render(matrixStack, mouseX, mouseY, partialTicks);
                yOffset += component.height;
            }
        }
        Render2D.size(FontRegister.Type.BOLD, 13).string(matrixStack, module.getName(), x + 35 + 25, y + 12, -1, (int) (140 - keys.width));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, x + 10, y + 10, 20, 20) && button == 0) {
            module.switchState();
            return true;
        }

        if (MathUtil.isHovered(mouseX, mouseY, x + 35, y + 10, 20, 20) && descAnimation.get() == 0 && button == 0) {
            module.sayDesc(!mc.gameSettings.language.toLowerCase().contains("en"), true);
            Client.instance.getMenu().callShowDesc(module);
            descAnimation.run(1, .6F);
            return true;
        }

        if (MathUtil.isHovered(mouseX, mouseY, x, y, width, 40)) {
            if (button == 2) {
                if (clientSoundsModule.bind.getValue()) SoundUtil.playSound("waitingforbind.wav", 0.1f);
                binding = !binding;
            }
        } else {
            binding = false;
        }

        for (Component component : module.getComponents()) {
            if (component.visible) {
                if (component.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
            return false;
        }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Component component : module.getComponents()) {
            if (component.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        for (Component component : module.getComponents()) {
            if (component.visible) {
                if (component.mouseScrolled(mouseX, mouseY, delta)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (binding) {
            if (clientSoundsModule.bind.getValue()) SoundUtil.playSound("bind.wav", 0.1f);
            module.setKey(keyCode);
            binding = false;

            if (keyCode == GLFW_KEY_ESCAPE) {
                module.setKey(GLFW.GLFW_KEY_UNKNOWN);
            }

            return true;
        }
        for (Component component : module.getComponents()) {
            if (component.visible) {
                if (component.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (Component component : module.getComponents()) {
            if (component.visible) {
                if (component.charTyped(codePoint, modifiers)) {
                    return true;
                }
            }
        }
        return false;
    }
}
