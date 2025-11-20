package relake.module.implement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.LivingEntity;
import relake.animation.excellent.Animation;
import relake.common.util.ColorUtil;
import relake.event.EventHandler;
import relake.event.impl.render.ScreenRenderEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.render.display.Render2D;
import relake.render.display.shape.ShapeRenderer;
import relake.render.display.shape.Side;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.ColorSetting;
import relake.settings.implement.FloatSetting;
import relake.settings.implement.SelectSetting;

import java.awt.*;

public class CrosshairModule extends Module {

    public final SelectSetting mode = new SelectSetting("Внешний вид")
            .setValue("Крестик",
                    "Точка");

    public final Setting<Color> color = new ColorSetting("Цвет")
            .setValue(0xFFFFFFFF);

    public final Setting<Float> length = new FloatSetting("Длина")
            .range(3.5F, 5, 0.5f)
            .setValue(5F).setVisible(() -> mode.isSelected("Крестик"));

    public final Setting<Float> dist = new FloatSetting("Дистанция от центра")
            .range(1.0f, 5.0F, 0.5f)
            .setValue(1.0F).setVisible(() -> mode.isSelected("Крестик"));

    public final Setting<Float> strengthMove = new FloatSetting("Сила движения при аттаке")
            .range(5f, 15f, 0.5f)
            .setValue(7.5f).setVisible(() -> mode.isSelected("Крестик"));

    public final Setting<Boolean> dot = new BooleanSetting("Отображать точку").setValue(true).setVisible(() -> mode.isSelected("Крестик"));
    public final Setting<Boolean> onLook = new BooleanSetting("Краснеть при наводке").setValue(true);
    private final Animation redStatusAnim = new Animation();

    public CrosshairModule() {
        super("Crosshair", "Изменяет внешний вид прицела", "Changes the appearance of the scope", ModuleCategory.Render);
        registerComponent(mode, color, length, dist, strengthMove, dot, onLook);
        mode.setSelected("Крестик");
    }

    @EventHandler
    public void test(ScreenRenderEvent event) {
        float posX = mw.getWidth() / 2.F;
        float posY = mw.getHeight() / 2.F;

        if (mc.gameSettings.getPointOfView() != PointOfView.FIRST_PERSON) {
            return;
        }
        if (mode.isSelected("Крестик")) {


            MatrixStack matrixStack = event.getMatrixStack();

            float swingPC = mc.player.getSwingProgress(mc.getRenderPartialTicks());
            swingPC = (swingPC > .5F ? 1.F - swingPC : swingPC) * strengthMove.getValue();
            float dist = 4 + this.dist.getValue() + swingPC;
            redStatusAnim.update();
            redStatusAnim.run(onLook.getValue() && mc.pointedEntity instanceof LivingEntity ? 1 : 0, .1F);
            int rgb = ColorUtil.interpolateColor(color.getValue().getRGB(), Color.red.getRGB(), redStatusAnim.getValue() * 100F);

            if (!onLook.getValue())
                rgb = color.getValue().getRGB();

            if (dot.getValue()) {
                ShapeRenderer dot = Render2D.box(matrixStack, posX, posY, 0, 0);
                dot.expand(Side.ALL, 2);
                dot.quad(0, 0xFF000000);
                dot.expand(Side.ALL, -0.7F);
                dot.quad(0, rgb);
            }

            ShapeRenderer up = Render2D.box(matrixStack, posX, posY - dist, 0, 0);
            up.expand(Side.ALL, 2);
            up.expand(Side.TOP, length.getValue());
            up.quad(0, 0xFF000000);
            up.expand(Side.ALL, -0.7F);
            up.quad(0, rgb);

            ShapeRenderer down = Render2D.box(matrixStack, posX, posY + dist, 0, 0);
            down.expand(Side.ALL, 2);
            down.expand(Side.BOTTOM, length.getValue());
            down.quad(0, 0xFF000000);
            down.expand(Side.ALL, -0.7F);
            down.quad(0, rgb);

            ShapeRenderer left = Render2D.box(matrixStack, posX - dist, posY, 0, 0);
            left.expand(Side.ALL, 2);
            left.expand(Side.LEFT, length.getValue());
            left.quad(0, 0xFF000000);
            left.expand(Side.ALL, -0.7F);
            left.quad(0, rgb);

            ShapeRenderer right = Render2D.box(matrixStack, posX + dist, posY, 0, 0);
            right.expand(Side.ALL, 2);
            right.expand(Side.RIGHT, length.getValue());
            right.quad(0, 0xFF000000);
            right.expand(Side.ALL, -0.7F);
            right.quad(0, rgb);
        }
        if (mode.isSelected("Точка")) {
            boolean overheat = onLook.getValue() && mc.pointedEntity instanceof LivingEntity;
            redStatusAnim.update();
            redStatusAnim.run(overheat ? 1 : 0, .1F);
            float glowAPC = overheat ? 1.F : .6F;
            int colorCenter = ColorUtil.getOverallColorFrom(color.getValue().getRGB(), ColorUtil.getColor(255, 30, 40), redStatusAnim.get()), colorOut = ColorUtil.getOverallColorFrom(colorCenter, ColorUtil.getColor(0, 0, 0, ColorUtil.getAlphaFromColor(colorCenter)), .7F), colorGlow = ColorUtil.multAlpha(colorCenter, glowAPC);
            float swingPC = mc.player.getSwingProgress(mc.getRenderPartialTicks());
            swingPC = (swingPC > .5F ? 1.F - swingPC : swingPC) * 2.F;
            swingPC *= 2.F;
            float size = 4.F + swingPC, outSize = 5.5F + swingPC, glowSize = overheat ? 30.F : 20.F + swingPC * 3.25F;
            ShapeRenderer point = new ShapeRenderer().box(event.getMatrixStack(), posX - glowSize / 2.F, posY - glowSize / 2.F, glowSize, glowSize);
            point.drawShadow(point.matrixStack, (int) (glowSize / 2.F), 0.1f, colorGlow);
            ShapeRenderer point2 = new ShapeRenderer().box(event.getMatrixStack(), posX - outSize / 2.F, posY - outSize / 2.F, outSize, outSize);
            point2.circle(colorOut);
            ShapeRenderer point3 = new ShapeRenderer().box(event.getMatrixStack(), posX - size / 2.F, posY - size / 2.F, size, size);
            point3.circle(colorCenter);
        }

    }
}

