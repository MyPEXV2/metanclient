package relake.draggable.implement;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;
import relake.Client;
import relake.Constants;
import relake.animation.tenacity.Direction;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.common.util.StopWatch;
import relake.common.util.StringUtil;
import relake.draggable.Draggable;
import relake.module.Module;
import relake.module.ModuleManager;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;

public class KeyBindDraggable extends Draggable {

    private final StopWatch visibleStopWatch = new StopWatch();

    public KeyBindDraggable() {
        super("Keybinds", 300, 100, 125, 30);
    }

    @Override
    public boolean visible() {
        ModuleManager moduleManager = Client.instance.moduleManager;

        boolean anyMatch = moduleManager.modules
                .stream()
                .anyMatch(module -> !module.getAnimation().isDone(Direction.BACKWARD) && module.getKey() != GLFW.GLFW_KEY_UNKNOWN);

        if (anyMatch)
            visibleStopWatch.reset();

        return (anyMatch || !visibleStopWatch.finished(200) || mc.currentScreen instanceof ChatScreen) && moduleManager.hudModule.selectComponent.isSelected("KeyBinds") && moduleManager.hudModule.isEnabled();
    }

    @Override
    public void tick() {
        float maxCooldownWidth = (float) Client.instance.moduleManager.modules.stream().filter(module -> !module.getAnimation().isDone(Direction.BACKWARD) && module.getKey() != GLFW.GLFW_KEY_UNKNOWN)
                .mapToDouble(module -> Render2D.size(FontRegister.Type.BOLD, 12).getWidth(module.getName() + ("[" + StringUtil.getKeyName(module.getKey()) + "]")) + 15)
                .max()
                .orElse(0) + 15;

        this.animatedWidth = Math.max(this.defaultWidth, maxCooldownWidth);
        this.width = this.animWidth.get();
    }

    @Override
    public void update() {}

    @Override
    public void render(MatrixStack matrixStack, float partialTicks) {
        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();
        float effectHeight = 10;
        float maxTextX = x + width - 10;
        int round = 10;

        ShapeRenderer box = Render2D.box(matrixStack, x, y, width, Math.max(60, height));

        box.quad(round, 0xB70E0E0F);
        box.quad(round, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f));

        Render2D.size(FontRegister.Type.BOLD, 14).string(matrixStack, name, x + 5, y + 5, ColorUtil.applyOpacity(ColorUtil.getColor(235, 235, 235), 255));
        Render2D.size(FontRegister.Type.ICONS, 20).string(matrixStack, "g", x + width - 10 - Render2D.size(FontRegister.Type.ICONS, 20).getWidth("g"), y + 4, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.35f));

        ShapeRenderer box1 = Render2D.box(matrixStack, x, y + 28.f, width, Math.max(60, height) - 29);
        box1.corner(new float[]{0, round, 0, round}, 0x590E0E0F);

        ShapeRenderer boxOutLine = Render2D.box(matrixStack, x - 2, y - 2, width +4, Math.max(60, height) + 4);
        boxOutLine.outlineHud(round, 2, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f));

        float yOffset = y + 35;
        int hasKeybindsCount = 0;
        float lastKeybindAnimation = 0;
        float scaleFactor = (float) mc.getMainWindow().getGuiScaleFactor();
        for (Module module : Client.instance.moduleManager.modules) {
            if (!module.getAnimation().isDone(Direction.BACKWARD) && module.getKey() != GLFW.GLFW_KEY_UNKNOWN) {
                ++hasKeybindsCount;

                String keyText = "[" + StringUtil.getKeyName(module.getKey()) + "]";
                float textWidth = Render2D.size(FontRegister.Type.BOLD, 12).getWidth(keyText);
                float anim = lastKeybindAnimation = module.getAnimation().get();
                float textX = x + width;
                float motion = 0;

                if (textX + textWidth > maxTextX) {
                    textX = maxTextX - textWidth;
                }

                matrixStack.push();
                matrixStack.translate(0, yOffset / scaleFactor, 0);
                matrixStack.scale(1, anim, 1);
                String nameCut = MathUtil.getStringPercent(module.getName(), anim);
                Render2D.size(FontRegister.Type.BOLD, 12).string(
                        matrixStack, nameCut, x + 10 - motion + motion * anim, -1,
                        ColorUtil.applyOpacity(ColorUtil.getColor(200, 200, 200), module.getAnimation().get() * 255)
                );

                Render2D.size(FontRegister.Type.BOLD, 12).string(
                        matrixStack, keyText, textX - 1.5f, -1,
                        ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, anim * 25), -1, .4f
                        ));

                matrixStack.pop();

                yOffset += 20 * anim;
                effectHeight += 20 * anim;
            }
        }

        if (hasKeybindsCount <= 1) {
            float stringPC = hasKeybindsCount == 0 ? 1.F : 1.F - lastKeybindAnimation;
            String empty = MathUtil.getStringPercent("Список пуст.", stringPC);
            matrixStack.push();
            matrixStack.translate(0, (yOffset + 6) / scaleFactor, 0);
            matrixStack.scale(1, stringPC, 1);
            matrixStack.translate(0, -(yOffset + 6) / scaleFactor, 0);
            Render2D.size(FontRegister.Type.BOLD, 12).string(
                    matrixStack,
                    empty,
                    x + width / 2 - Render2D.size(FontRegister.Type.BOLD, 12).getWidth(empty) / 2 - 10,
                    y + 35,
                    ColorUtil.applyOpacity(ColorUtil.getColor(200, 200, 200), 215.F * stringPC)
            );
            matrixStack.pop();

            height = 60;
            return;
        }

        height = effectHeight + 30;
    }
}