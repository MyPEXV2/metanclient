package relake.menu.ui.components.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.util.ResourceLocation;
import relake.Client;
import relake.animation.excellent.Animation;
import relake.animation.excellent.util.Easings;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.render.display.Render2D;
import relake.render.display.shape.ShapeRenderer;
import relake.settings.implement.ColorSetting;

import java.awt.*;

@Getter
public class ColorPickerWindow extends Window {
    private final ColorSetting colorSetting;

    private boolean color;
    private boolean hue;
    private final Animation hueAnimation = new Animation();
    private final Animation xAnimation = new Animation();
    private final Animation yAnimation = new Animation();
    private boolean init;

    public ColorPickerWindow(ColorSetting colorSetting) {
        super(colorSetting.getName());
        this.colorSetting = colorSetting;
        init = true;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        hueAnimation.update();
        xAnimation.update();
        yAnimation.update();
        int factor = (int) mw.getGuiScaleFactor();

        mouseX = mouseX * factor;
        mouseY = mouseY * factor;
        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();
        ShapeRenderer box = Render2D.box(matrixStack, x, y, width, height);
        box.quad(15, 0xFF181819);
        box.quad(15, ColorUtil.applyOpacity(rgb, 15));

        int[] hsb = {
                0xFFFFFFFF,
                0xFF000000,
                Color.HSBtoRGB(colorSetting.getHue(), 1, 1),
                0xFF000000,
        };

        ShapeRenderer colorPicker = Render2D.box(matrixStack, x + 5, y + 5, width - 30, height - 10);
        colorPicker.quad(8, hsb);

        if (init) {
            xAnimation.set(colorPicker.x + (colorSetting.getSaturation() * colorPicker.width) - 5);
            yAnimation.set(colorPicker.y + colorPicker.height + (1 - colorSetting.getBrightness() * colorPicker.height) - 5);
            hueAnimation.set(colorPicker.y + (colorSetting.getHue() * colorPicker.height) - 5);
            init = false;
        }

        if (this.color) {
            colorSetting.setBrightness(MathUtil.clamp(1 - ((mouseY - colorPicker.y) / colorPicker.height), 0, 1));
            colorSetting.setSaturation(MathUtil.clamp(((mouseX - colorPicker.x) / colorPicker.width), 0, 1));
            xAnimation.run(colorPicker.x + (colorSetting.getSaturation() * colorPicker.width) - 5, 0.5F, Easings.BACK_OUT, true);
            yAnimation.run(colorPicker.y + colorPicker.height + (1 - colorSetting.getBrightness() * colorPicker.height) - 5, 0.5F, Easings.BACK_OUT, true);

        }
        float colorX = MathUtil.clamp(
                xAnimation.get(),
                colorPicker.x,
                colorPicker.x + colorPicker.width - 10
        );
        float colorY = MathUtil.clamp(
                yAnimation.get(),
                colorPicker.y,
                colorPicker.y + colorPicker.height - 10
        );

        ShapeRenderer pickerPoint = Render2D.box(matrixStack, colorX, colorY, 10, 10);
        pickerPoint.circle(0xFFFFFFFF);

        ShapeRenderer hue = Render2D.box(matrixStack, x + width - 18, y + 5, 10, height - 10);

        mc.getTextureManager().bindTexture(new ResourceLocation("relake/hue.png"));
        hue.texture(5);

        if (this.hue) {
            colorSetting.setHue(MathUtil.clamp(((mouseY - colorPicker.y) / colorPicker.height), 0, 1));
            hueAnimation.run(colorPicker.y + (colorSetting.getHue() * colorPicker.height) - 5, 0.5F, Easings.BACK_OUT, true);
        }

        float hueY = MathUtil.clamp(hueAnimation.get(),
                colorPicker.y,
                colorPicker.y + colorPicker.height - 10
        );

        ShapeRenderer huePoint = Render2D.box(matrixStack, hue.x, hueY, 10, 10);
        huePoint.circle(0xFFFFFFFF);

        colorSetting.setValue(
                new Color(
                        Color.HSBtoRGB(colorSetting.getHue(), colorSetting.getSaturation(), colorSetting.getBrightness())
                )
        );

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            color = MathUtil.isHovered(mouseX, mouseY, x + 5, y + 5, width - 30, height - 10);
            hue = MathUtil.isHovered(mouseX, mouseY, x + width - 18, y + 5, 10, height - 10);
        }
        return color || hue;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        color = false;
        hue = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
