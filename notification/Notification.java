package relake.notification;


import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Data;
import relake.Client;
import relake.animation.excellent.Animation;
import relake.animation.excellent.util.Easing;
import relake.animation.excellent.util.Easings;
import relake.common.InstanceAccess;
import relake.common.util.ChatUtil;
import relake.common.util.ColorUtil;
import relake.common.util.StopWatch;
import relake.render.display.Render2D;
import relake.render.display.shape.ShapeRenderer;

import static relake.render.display.font.FontRegister.Type.BOLD;
import static relake.render.display.font.FontRegister.Type.LOGO;

@Data
public class Notification implements InstanceAccess {
    private final String title, content;
    private final boolean circle;
    private final int color;
    private final long delay, wait = 500;
    private final int index;
    private final NotificationType type;
    private final Animation animationY = new Animation();
    private final Animation animation = new Animation();
    private final StopWatch time = new StopWatch();

    public Notification(String title, String content, long delay, int index, NotificationType type) {
        this.title = title;
        this.content = content;
        this.circle = false;
        this.color = -1;
        this.delay = delay;
        this.index = index;
        this.type = type;

        this.animationY.set(index);
    }

    public Notification(String title, String content, boolean circle, int color, long delay, int index, NotificationType type) {
        this.title = title;
        this.content = content;
        this.circle = circle;
        this.color = color;
        this.delay = delay;
        this.index = index;
        this.type = type;

        this.animationY.set(index);
    }


    public void render(MatrixStack matrix, final int multiplier) {
        int fontSize = 14;
        float mcwidth = mw.getWidth();
        float mcheight = mw.getHeight();
        boolean finished = finished();

        animation.update();
        animationY.update();

        Easing easing = finished ? Easings.QUAD_IN : Easings.QUAD_OUT;

        float margin = 20;
        float width = margin + Render2D.size(BOLD, fontSize).getWidth(content) + margin + 15;
        float height = margin + fontSize + margin;


        animation.run(finished ? 0 : 1, (wait / 1000F), easing, true);
        animationY.run(multiplier, (wait / 1000F), Easings.BACK_OUT, true);

        float x = mcwidth - margin - (width * animation.get());
        float y = mcheight - height - 32 - ((animationY.get() * (height + (margin / 4))));

        float alpha = (float) Math.pow(animation.get(), 15);
        matrix.push();
        matrix.translate((x + width), (y + height / 2F), 0);
        matrix.scale(1, 1, 1);
        matrix.translate(-(x + width), -(y + height / 2F), 0);

        ShapeRenderer box = Render2D.box(matrix, x, y, width, height);
        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

        int secondColor = ColorUtil.multAlpha(0xEE0E0E0F, alpha);

        box.quad(8, secondColor);
        box.quad(8, ColorUtil.multAlpha(rgb, alpha * 0.13725490196F), secondColor, secondColor, ColorUtil.multAlpha(rgb, alpha * 0.13725490196F));
        box.outlineHud(8, 1, ColorUtil.multAlpha(rgb, alpha * 0.29411764705F), secondColor, secondColor, ColorUtil.multAlpha(rgb, alpha * 0.29411764705F));

        ShapeRenderer line = Render2D.box(matrix, x + 38.5f, y + 5, 1, height - 10);

        line.quad(3, ColorUtil.applyOpacity(rgb, alpha * 100));

        if (circle) {
            ShapeRenderer circle = Render2D.box(matrix, x + width - 16, y + 10, 5, 5);

            circle.circle(color);
            circle.drawShadow(matrix, 20, 10, ColorUtil.applyOpacity((int) (alpha * color), alpha * 100));
        }


        float pointOffXY = 7.5f, pointScale = 3.5F;
        ShapeRenderer point = Render2D.box(matrix, x + width - pointOffXY - 4, y + pointOffXY, pointScale, pointScale);

        if (!title.contains("Module")) {
            int pointCol = -1;
            switch (this.type) {
                case INFO -> pointCol = ColorUtil.multAlpha(ColorUtil.getColor(0, 255, 0), alpha);
                case WARN -> pointCol = ColorUtil.multAlpha(ColorUtil.getColor(255, 200, 0), alpha);
                case ERROR -> pointCol = ColorUtil.multAlpha(ColorUtil.getColor(255, 0, 0), alpha);
            }
            for (int i = 0; i < 2; i++)
                point.drawShadow(matrix, pointOffXY * 2.F, pointCol, pointCol, pointCol, pointCol);
            point.circle(pointCol);
        }

        Render2D.size(LOGO, 70).string(matrix, "R", x - 5, y - 5, ColorUtil.applyOpacity(rgb, alpha * 155));
        Render2D.size(BOLD, fontSize).string(matrix, title, x + margin + 25, (int) y + margin - 12, ColorUtil.getColor(185, 185, 185, alpha));
        Render2D.size(BOLD, fontSize).string(matrix, content, x + margin + 25, (int) y + margin + 7, ColorUtil.getColor(185, 185, 185, alpha));
        matrix.pop();
    }

    public boolean finished() {
        return time.finished(wait + delay);
    }

    public boolean hasExpired() {
        return time.finished(wait + delay + wait);
    }
}
