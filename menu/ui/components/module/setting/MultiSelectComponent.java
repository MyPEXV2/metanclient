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
import relake.menu.ui.components.module.ModuleComponent;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;
import relake.render.display.shape.Side;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.MultiSelectSetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class MultiSelectComponent extends Component {
    private final MultiSelectSetting setting;
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
            selectAnimations.get(i).run(setting.isSelected(setting.getList().get(i)) ? 1 : 0, 0.25, Easings.QUAD_OUT);
        }
        return selectAnimations;
    }

    public MultiSelectComponent(MultiSelectSetting setting) {
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
        Render2D.size(FontRegister.Type.BOLD, 11).string(matrixStack, setting.getName() + " (" + setting.getSelected().size() + "/" + setting.getList().size() + ")", x + 5, y + 12, -1, 120);
        Render2D.size(FontRegister.Type.ICONS, 18).string(matrixStack, "m", x + width - 26, y + 11, ColorUtil.applyOpacity(ColorUtil.getColor(215, 215, 215), 215));

        //elements
        if (openAnim.get() == 0 && !openned) return;
        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();
        float settingsY = y + getCupHeight() * (.5F + .5F * openAnim.get()) + getPostPadding() * openAnim.get(), yPadding = getYPadding() * openAnim.get();
        int indexElement = 0;
        for (String string : setting.getList()) {
            float offsetY = indexElement * yPadding;
            float selectedPCAnim = selectedAnimations.get(indexElement).get();
            float settingX = x + 5.F;
            int offCol = ColorUtil.getColor(235, 235, 235, (float) Easings.SINE_IN.ease(openAnim.get()));
            int onCol = ColorUtil.multAlpha(ColorUtil.darker(rgb, 15), ColorUtil.getGLAlphaFromColor(offCol));
            int settingColor = ColorUtil.getOverallColorFrom(ColorUtil.multDark(offCol, .7F), onCol, selectedPCAnim);
            float boxScale = Math.max(yPadding / 1.5F / Math.min(openAnim.get() * 2.F, 1.F), 0.F);
            ShapeRenderer box = Render2D.box(matrixStack, settingX, settingsY + offsetY + 2.F, boxScale, boxScale);
            box.quad((int) (box.width / 2.F), ColorUtil.multDark(offCol, (.2F + selectedPCAnim * .8F) * openAnim.get()));
            box.expand(Side.ALL, -1.F);
            box.quad((int) (box.width / 2.F), ColorUtil.multDark(ColorUtil.getOverallColorFrom(offCol, onCol, selectedPCAnim), .1F + .35F * selectedPCAnim));
            box.expand(Side.ALL, -1.F - 2.F * selectedPCAnim);
            box.quad((int) (box.width / 2.F), ColorUtil.multAlpha(offCol, selectedPCAnim));

            settingX += boxScale + 3.F;
            Render2D.size(FontRegister.Type.BOLD, 11).string(matrixStack, string, settingX, settingsY + offsetY + 3.F, settingColor, 90);
            float waveAPC = (selectedPCAnim > .5F ? 1.F - selectedPCAnim : selectedPCAnim) * 2.F;
            if (waveAPC > .05F) {
                Render2D.size(FontRegister.Type.BOLD, 11).string(matrixStack, MathUtil.getStringPercent(string, selectedPCAnim * 1.15F), settingX + .5F, settingsY + offsetY + 3.F, ColorUtil.multAlpha(setting.isSelected(string) ? offCol : onCol, waveAPC));
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
            float offsetY = -1.F, cupHeight = getCupHeight(), postPadding = getPostPadding(), yPadding = getYPadding();
            for (String string : setting.getList()) {
                if (MathUtil.isHovered(mouseX, mouseY, x, y + cupHeight + postPadding + offsetY, width, yPadding)) {
                    if (setting.getSelected().contains(string)) {
                        setting.getSelected().remove(string);
                        SoundUtil.playSound("enablecb.wav", 0.15f);
                    } else {
                        setting.getSelected().add(string);
                        SoundUtil.playSound("disablecb.wav", 0.15f);
                    }
                    break;
                }
                offsetY += yPadding;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private float getYPadding() {return 22.F;}
    private float getPostPadding() {return 3.F;}
    private float getCupHeight() {return 35.F;}
    private float getTotaHeight(float openPercent) {return getCupHeight() + getYPadding() * setting.getList().size() * openPercent + getPostPadding() * 2.F * openPercent;}
}
