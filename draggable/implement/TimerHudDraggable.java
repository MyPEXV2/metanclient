package relake.draggable.implement;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.ChatScreen;
import relake.Client;
import relake.animation.excellent.Animation;
import relake.animation.excellent.util.Easings;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.draggable.Draggable;
import relake.module.ModuleManager;
import relake.module.implement.movement.TimerModule;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.font.TextRenderer;
import relake.render.display.shape.ShapeRenderer;
import relake.render.display.shape.Side;

public class TimerHudDraggable extends Draggable {

    private final Animation animation = new Animation();

    public TimerHudDraggable() {
        super("TimerHud", 300, 100, 115, 20.5f);
    }

    @Override
    public boolean visible() {
        ModuleManager moduleManager = Client.instance.moduleManager;
        return (moduleManager.timerModule.smart.getValue() || mc.currentScreen instanceof ChatScreen) && (moduleManager.hudModule.selectComponent.isSelected("TimerHud") && moduleManager.hudModule.isEnabled());
    }

    @Override
    public void tick() {}
    @Override
    public void update() {}

    @Override
    public void render(MatrixStack matrixStack, float partialTicks) {
        //animation
        final TimerModule timerModule = Client.instance.moduleManager.timerModule;
        float percentage = (float) MathUtil.lerp(timerModule.prevPercent, timerModule.percent, partialTicks);
        animation.update();
        animation.run(percentage, .1F, Easings.QUAD_OUT, false);
        percentage = animation.get();
        //colors
        final int color = Client.instance.moduleManager.hudModule.color.getValue().getRGB();
        final int baseBgCol = 0xB70E0E0F, lineBgCol = 0x780E0E0F, iconCol = ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(color, 65 * percentage), -1, .5f);
        final int[] bgCols = new int[] {ColorUtil.applyOpacity(color, 35), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.applyOpacity(color, 35)},
                outCols = new int[] {ColorUtil.applyOpacity(color, 75), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.applyOpacity(color, 75)},
                lineCols = new int[] {color, color, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(color, 65), -1, .4f), ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(color, 65), -1, .4f)};

        //settings
        float setW = 120.F, setH = 27.F;
        int offsets = 8, outLineWidth = 2, lineBgInsideAmount = 1;
        final boolean iconFollowLine = false;


        //args
        offsets = (int) Math.min(offsets, Math.ceil(height / 2.F - 1.F));
        final int fontHeight = Math.min(Math.max((int) Math.ceil(height - offsets * 2.F + 1.F), 7), 72);
        final TextRenderer iconFont = Render2D.size(FontRegister.Type.ICONS, fontHeight);
        final String iconText = "p";
        float iconWidth = iconFont.getWidth(iconText);
        final float lineHeight = height - offsets * 2.F, lineWidth = (width - (offsets * 2.25F + iconWidth)) * percentage, lineWidthBG = width - (offsets * 2.25F + iconWidth);
        final int rectRound = (int) (height / 2.F);
        final float lineRound = lineHeight / 2.F;
        final float iconX = x + (iconFollowLine ? lineWidth : lineWidthBG) + offsets;


        //bg & out
        ShapeRenderer background = Render2D.box(matrixStack, x, y, width, height);
        background.expand(Side.ALL, -outLineWidth);
        int bgRound = (int) (rectRound - outLineWidth);
        background.quad(bgRound, baseBgCol);
        background.quad(bgRound, bgCols);
        ShapeRenderer outline = Render2D.box(matrixStack, x, y, width, height);
        outline.outlineHud(rectRound, (int) outLineWidth, outCols[0], outCols[1], outCols[2], outCols[3]);

        //line & bg line
        ShapeRenderer line = Render2D.box(matrixStack, x + offsets, y + offsets, lineWidthBG, height - offsets * 2.F);
        line.expand(Side.ALL, -lineBgInsideAmount);
        line.quad((int) (lineRound - lineBgInsideAmount), lineBgCol);
        line.expand(Side.ALL, lineBgInsideAmount);
        line.width = lineWidth;
        line.quad((int) lineRound, lineCols);

        //icon text
        iconFont.string(matrixStack, iconText, iconX, y + height / 2.F - fontHeight / 2.25F - 1.75F, iconCol);

        //apply settings
        width = setW;
        height = setH;
    }
}