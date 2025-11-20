package relake.draggable;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import relake.animation.apelcin4ik.Animation;
import relake.animation.apelcin4ik.impl.DecelerateAnimation;
import relake.common.InstanceAccess;

public abstract class Draggable implements InstanceAccess {
    @Getter

    public final relake.animation.excellent.Animation animation = new relake.animation.excellent.Animation();
    public float updatedScale, updatedAlpha;
    public final String name;
    public float defaultWidth;
    public float x, y, width, height;
    public float animatedX, animatedY, animatedWidth;
    public final relake.animation.excellent.Animation animX = new relake.animation.excellent.Animation();
    public final relake.animation.excellent.Animation animY = new relake.animation.excellent.Animation();
    public final relake.animation.excellent.Animation animWidth = new relake.animation.excellent.Animation();
    public final Animation draggbleAnimation = new DecelerateAnimation(350, 1);
    public final Animation draggbleAnimation2 = new DecelerateAnimation(250, 1);

    public boolean dragging;
    public float mouseX, mouseY;

    public Draggable(String name, float x, float y, float width, float height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.animatedX = x;
        this.animatedY = y;
        this.defaultWidth = width;
        this.animatedWidth = width;
        this.width = width;
        this.height = height;
    }

    public abstract boolean visible();

    public abstract void tick();

    public abstract void update();

    public abstract void render(MatrixStack matrixStack, float partialTicks);
}
