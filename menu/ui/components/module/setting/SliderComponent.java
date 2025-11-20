package relake.menu.ui.components.module.setting;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import relake.Client;
import relake.animation.excellent.Animation;
import relake.animation.excellent.util.Easings;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.common.util.SoundUtil;
import relake.menu.ui.components.Component;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;
import relake.settings.implement.FloatSetting;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@RequiredArgsConstructor
public class SliderComponent extends Component {
    private final FloatSetting setting;
    private boolean dragging;
    private final Animation animation = new Animation();
    private final Animation sizeAnimation = new Animation();
    private long lastSoundTime = 0;
    private static final long SOUND_DELAY = 40;
    private float lastSliderValue;

    @Override
    public void updateComponent() {
        visible = setting.getVisible().get();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

        height = height + 9;

        int factor = (int) mw.getGuiScaleFactor();
        mouseX = mouseX * factor;

        ShapeRenderer slider = Render2D.box(matrixStack, x, y + 25, width, 7);
        slider.quad(2, 0x4F111112);

        float sliderValue = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        float percentValue = MathUtil.clamp(slider.width * sliderValue + 8, 10, slider.width);

        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

        int[] color = {
                rgb,
                rgb,
                ColorUtil.darker(rgb, 100),
                ColorUtil.darker(rgb, 100)
        };

        animation.update();
        sizeAnimation.update();
        animation.run(percentValue, 0.2, Easings.powOut(2), true);
        sizeAnimation.run(dragging ? 2F : 0F, 0.25, Easings.BACK_OUT, false);

        ShapeRenderer box = Render2D.box(matrixStack, slider.x, y + 25, animation.get(), 7);

        float size = 10 + sizeAnimation.get();

        box.quad(3, color);
        box.box(matrixStack, slider.x + animation.get() - (size / 2F) - 5, y + 28.5f - (size / 2F), size, size).quad((int) (size / 2F), -1);

        if (dragging) {
            float newValue = (mouseX - slider.x) / slider.width;
            double value = MathUtil.step(setting.getMin() + newValue * (setting.getMax() - setting.getMin()), setting.getIncrement());
            float clampedValue = MathUtil.clamp((float) value, setting.getMin(), setting.getMax());

            if (clampedValue != lastSliderValue) {
                long currentTime = System.currentTimeMillis();

                if (currentTime - lastSoundTime >= SOUND_DELAY && Client.instance.moduleManager.clientSoundsModule.slider.getValue()) {
                    SoundUtil.playSound("slider.wav", 0.1f);
                    lastSoundTime = currentTime;
                }

                lastSliderValue = clampedValue;
            }

            setting.setValue(clampedValue);
        }

        BigDecimal result = BigDecimal.valueOf(setting.getValue())
                .setScale(1, RoundingMode.HALF_UP);

        String value = String.valueOf(result).replace(".0", "");

        Render2D.size(FontRegister.Type.BOLD, 11).string(matrixStack, value, x + width - Render2D.size(FontRegister.Type.BOLD, 13).getWidth(value) - 2, y + 6, 0xFFAAAAAA);
        Render2D.size(FontRegister.Type.BOLD, 11).string(matrixStack, setting.getName(), x, y + 6, -1, 100);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        dragging = MathUtil.isHovered(mouseX, mouseY, x, y + 25, width, 7) && button == 0;
        lastSliderValue = setting.getValue();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        float sliderValue = (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin());
        float percentValue = MathUtil.clamp(width * sliderValue + 8, 10, width);
        animation.set(percentValue);
    }
}

