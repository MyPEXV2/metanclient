package relake.menu.ui.components.category;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import relake.Client;
import relake.animation.apelcin4ik.Animation;
import relake.animation.apelcin4ik.impl.EaseAnimation;
import relake.animation.excellent.util.Easings;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.common.util.SoundUtil;
import relake.menu.ui.components.Component;
import relake.menu.ui.components.module.ModuleComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
public class CategoryComponent extends Component {
    private final List<ModuleComponent> moduleComponents = new ArrayList<>();
    private final relake.animation.excellent.Animation animation = new relake.animation.excellent.Animation(),
            colorAnimation = new relake.animation.excellent.Animation();
    private final ModuleCategory category;
    public double smoothedScroll = 0;
    public float scroll;
    private boolean hoverSound;
    private long lastSoundTime = 0;
    private static final long SOUND_DELAY = 200;

    public CategoryComponent(ModuleCategory category) {
        this.category = category;

        for (Module module : Client.instance.moduleManager.modules) {
            if (module.getModuleCategory() == category) {
                moduleComponents.add(new ModuleComponent(module));
            }
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.animation.update();
        this.colorAnimation.update();

        if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height) && !hoverSound) {
            if (Client.instance.moduleManager.clientSoundsModule.category.getValue() && !(Client.instance.getMenu().category == category)) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSoundTime >= SOUND_DELAY) {
                    SoundUtil.playSound("viewcategory.wav", 0.05f);
                    hoverSound = true;
                    lastSoundTime = currentTime;
                }
            }
        } else if (!MathUtil.isHovered(mouseX, mouseY, x, y, width, height) && hoverSound) {
            hoverSound = false;
        }

        this.animation.run(MathUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height) || Client.instance.getMenu().category == category ? 1F : 0F, 0.10F, Easings.LINEAR, true);
        this.colorAnimation.run(Client.instance.getMenu().mewCategory == category ? 1F : 0F, 0.15F, Easings.LINEAR, true);

        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();
        ShapeRenderer category = Render2D.box(matrixStack, x, y, width, height);

        float xS = (this.animation.get() * 4F);

        Render2D.size(FontRegister.Type.BOLD, 12).string(matrixStack, this.category.name(), category.x + 30 + xS, category.y + 10, -1);
        Render2D.size(FontRegister.Type.ICONS, 18).string(matrixStack, this.category.getTextureID(), category.x + 8 + xS, category.y + 8, ColorUtil.interpolateColor(-1, rgb, this.colorAnimation.get() * 50F));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height) && button == 0) {
            if (!(Client.instance.getMenu().category == category))
                if (Client.instance.moduleManager.clientSoundsModule.category.getValue()) SoundUtil.playSound("switchcategory.wav", 0.1f);

            Client.instance.getMenu().mewCategory = category;
        }
        return false;
    }

    public ModuleCategory getCategory() {
        return category;
    }
}
