package relake.menu.mainmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Setter;
import relake.Client;
import relake.animation.tenacity.Animation;
import relake.animation.tenacity.Direction;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;
import relake.render.display.shape.Side;

import java.time.Duration;

@Setter
public class Button {
    public final Animation animation = new Animation(100, Duration.ofMillis(150));

    public final String name;
    public final Runnable runnable;

    public final FontRegister.Type fontType;

    public float x;
    public float y;
    public float width;
    public float height;

    public Button(String name, Runnable runnable) {
        this.name = name;
        this.runnable = runnable;
        this.fontType = FontRegister.Type.DEFAULT;
        animation.setDirection(Direction.BACKWARD);
    }

    public Button(FontRegister.Type customText, String name, Runnable runnable) {
        this.name = name;
        this.runnable = runnable;
        this.fontType = customText;
        animation.setDirection(Direction.BACKWARD);
    }

    public Button box(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

        animation.switchDirection(MathUtil.isHovered(mouseX, mouseY, x, y, width, height));

        ShapeRenderer box = Render2D.box(matrixStack, x, y, width, height);

        box.quad(10, ColorUtil.interpolateColor(0xFF151516, ColorUtil.applyOpacity(rgb, 50), animation.get()));
        box.expand(Side.ALL, 3);
        box.outline(10, 3, ColorUtil.applyOpacity(rgb, animation.get() / 2));

        Render2D.size(this.fontType, 15).centeredString(matrixStack, name, (int) (x + width / 2), (int) (y + height / 4), -1);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int key) {
        if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height)) {
            runnable.run();
            return true;
        }

        return false;
    }
}
