package relake.menu.ui.components.module.setting;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
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
import relake.settings.implement.SelectSetting;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SelectComponent extends Component {
    private final SelectSetting setting;
    private boolean openned = false;
    private Animation openAnim = new Animation();
    private List<Animation> selectAnimations = new ArrayList<>();
    private List<Animation> getUpdatedSelectAnimations() {
        if (selectAnimations.size() != setting.getList().size()) {
            selectAnimations.clear();
            for (int i = 0; i < setting.getList().size(); i++) selectAnimations.add(new Animation());
        }
        for (int i = 0; i < setting.getList().size(); i++) {
            selectAnimations.get(i).update();
            selectAnimations.get(i).run(setting.isSelected(setting.getList().get(i)) ? 1 : 0, 0.1, Easings.LINEAR);
        }
        return selectAnimations;
    }

    public SelectComponent(SelectSetting setting) {
        this.setting = setting;
    }


    @Override
    public void init() {
        super.init();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        //setup
        openAnim.update();
        float componentOffset = 2.F;
        height = getTotaHeight(openAnim.get()) + componentOffset;
        List<Animation> selectedAnimations = getUpdatedSelectAnimations();

        //bg
        ShapeRenderer mainBox = Render2D.box(matrixStack, x - 3, y + 2, width + 5.5f, getCupHeight());
        mainBox.quad(10, 0x4F111112);
        ShapeRenderer selectBox = Render2D.box(matrixStack, x - 3, y + 2, width + 5.5f, height - componentOffset);
        selectBox.quad(10, 0x4F111112);
        Render2D.size(FontRegister.Type.BOLD, 11).string(matrixStack, setting.getName() + MathUtil.getStringPercent(" - " + setting.getSelected() + " ", 1.F - openAnim.get()), x + 5, y + 12, -1, 120);
        Render2D.size(FontRegister.Type.ICONS, 18).string(matrixStack, "m", x + width - 26, y + 11, ColorUtil.applyOpacity(ColorUtil.getColor(215, 215, 215), 215));

        //elements
        if (openAnim.get() == 0 && !openned) return;
        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();
        float settingsY = y + getCupHeight() * (.5F + .5F * openAnim.get()), yPadding = getYPadding() * openAnim.get();
        int indexElement = 0;
        for (String string : setting.getList()) {
            float offsetY = indexElement * yPadding;
            float selectedPCAnim = selectedAnimations.get(indexElement).get();
            float settingX = x + 5.F;
            float highlightPC = selectedPCAnim * openAnim.get();
            int settingColor = ColorUtil.multAlpha(ColorUtil.getOverallColorFrom(ColorUtil.getColor(175, 175, 175), ColorUtil.darker(rgb, 15), selectedPCAnim), (float) Easings.SINE_IN.ease(openAnim.get()));
            int selectColor = ColorUtil.applyOpacity(ColorUtil.getOverallColorFrom(ColorUtil.getColor(235, 235, 235), ColorUtil.darker(rgb, 15), selectedPCAnim), ColorUtil.getAlphaFromColor(settingColor) * highlightPC);
            ShapeRenderer box = Render2D.box(matrixStack, settingX, settingsY + offsetY + yPadding * .5F * (1.F - highlightPC), 5.F * highlightPC, yPadding * highlightPC);
            box.quad((int) (box.width / 2.F), selectColor);
            box.drawShadow(matrixStack, box.width * 2.F, selectColor);
            settingX += (4.F + box.width) * highlightPC;
            Render2D.size(FontRegister.Type.BOLD, 11).string(matrixStack, string, settingX, settingsY + offsetY + 3.F, settingColor, 90);
            float waveAPC = selectedAnimations.get(indexElement).getToValue() == 1 ? (float)Easings.QUINT_IN.ease(selectedPCAnim) : 0;
            if (waveAPC > .01F) {
                float prevWaveAPC = waveAPC;
                waveAPC = selectedAnimations.get(indexElement).getToValue() == 1 ? Math.min((waveAPC > .5F ? 1.F - waveAPC : waveAPC) * 3.F, 1.F) : 0.F;
                Render2D.size(FontRegister.Type.BOLD, 11).string(matrixStack, MathUtil.getStringPercent(string, prevWaveAPC * 1.5F), settingX + .5F, settingsY + offsetY + 3.F, ColorUtil.multAlpha(ColorUtil.getColor(235, 235, 235), waveAPC));
            }
            ++indexElement;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && MathUtil.isHovered(mouseX, mouseY, x + width - 25, y + 10, 20, 20)) {
            openned = !openned;
            SoundUtil.playSound(openned ? "closeselectsettings.wav" : "openselectsettings.wav", 0.25f);
            openAnim.run(openned ? 1 : 0, 0.4, Easings.EXPO_OUT);
        }

        if (button == 0 && openAnim.get() == 1) {
            float offsetY = -1.F, cupHeight = getCupHeight(), yPadding = getYPadding();
            for (String string : setting.getList()) {
                if (MathUtil.isHovered(mouseX, mouseY, x, y + cupHeight + offsetY, width, yPadding)) {
                    if (!setting.isSelected(string)) {
                        SoundUtil.playSound("mode.wav", 0.25f);
                        setting.setSelected(string);
                        break;
                    }
                }
                offsetY += yPadding;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private float getYPadding() {return 20.F;}
    private float getPostPadding() {return 2.F;}
    private float getCupHeight() {return 35.F;}
    private float getTotaHeight(float openPercent) {return getCupHeight() + getYPadding() * setting.getList().size() * openPercent + getPostPadding() * openPercent;}
}
