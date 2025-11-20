package relake.render.display.font;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import relake.common.InstanceAccess;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.render.display.Render2D;

import java.awt.Color;

import static com.mojang.blaze3d.platform.GlStateManager.*;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX_COLOR;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static relake.shader.ShaderRegister.FONT_RENDER;

public class TextRenderer implements InstanceAccess {
    private FontEngine fonts = FontRegister.getSize(12);

    public TextRenderer size(int size) {
        fonts = FontRegister.getSize(size);
        return this;
    }

    public TextRenderer size(FontRegister.Type font, int size) {
        fonts = FontRegister.getSize(font, size);
        return this;
    }

    public void string(MatrixStack stack, String text, float x, float y, int color) {
        this.string(stack, text, x, y, color, true);
    }

    public void string(MatrixStack stack, String text, float x, float y, int color, boolean scaleFix) {
        x = Math.round(x);
        y = Math.round(y);
        stack.push();
        if (scaleFix)
            stack.scaleFix(1, 1, 1);
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableTexture();
        bindTexture(fonts.loadedTexture.getGlTextureId());

        BUFFER.begin(GL_QUADS, POSITION_TEX_COLOR);

        if (text != null) {
            for (int i = 0; i < text.length(); i++) {
                final char c = text.charAt(i);
                if (c < fonts.glyphs.length && fonts.glyphs[c] != null) {
                    fonts.drawGlyph(stack, fonts.glyphs[c], x, y, color);
                    x += (float) MathUtil.roundPROBLYA(fonts.glyphs[c].width - 8, 0.5F);
                }
            }
        }

        TESSELLATOR.draw();
        stack.pop();
    }

    public void string(MatrixStack stack, ITextComponent text, float x, float y, int color) {
        x = Math.round(x);
        y = Math.round(y);
        stack.push();
        stack.scaleFix(1, 1, 1);
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableTexture();
        bindTexture(fonts.loadedTexture.getGlTextureId());

        BUFFER.begin(GL_QUADS, POSITION_TEX_COLOR);
        {
            final float[] currentX = {x};

            float finalY = y;
            text.func_241878_f().accept((index, style, codePoint) -> {
                if (codePoint < fonts.glyphs.length && fonts.glyphs[codePoint] != null) {
                    int styleColor = style.getColor() != null ? new Color(style.getColor().getColor()).getRGB() : color;

                    fonts.drawGlyph(stack, fonts.glyphs[codePoint], currentX[0], finalY, styleColor);

                    currentX[0] += (float) MathUtil.roundPROBLYA(fonts.glyphs[codePoint].width - 8, 0.5F);
                }

                return true;
            });
        }

        TESSELLATOR.draw();
        stack.pop();
    }

    public void string(MatrixStack stack,
                       String text,
                       float x,
                       float y,
                       int color,
                       int width) {
        FONT_RENDER.begin();
        FONT_RENDER.setUniform("inColor", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color, Render2D.alphaPushed ? Render2D.alpha : 255F)));
        FONT_RENDER.setUniform("width", width);
        FONT_RENDER.setUniform("maxWidth", (x + width));
        string(stack, text, x, y, color);
        FONT_RENDER.end();
    }

    public void centeredString(MatrixStack stack,
                               String text,
                               float x,
                               float y,
                               int color) {
        string(stack, text, x - getWidth(text) / 2, y, color);
    }

    public void centeredString(MatrixStack stack,
                               String text,
                               float x,
                               float y,
                               int color,
                               int width) {
        string(stack, text, x - getWidth(text) / 2, y, color, width);
    }

    public void centeredString(MatrixStack stack,
                               ITextComponent text,
                               float x,
                               float y,
                               int color) {
        string(stack, text, x - getWidth(StringUtils.stripControlCodes(text.getString())) / 2, y, color);
    }

    public float getWidth(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) < fonts.glyphs.length && fonts.glyphs[text.charAt(i)] != null) {
                width += fonts.glyphs[text.charAt(i)].width - 8;
            }
        }
        return width;
    }

    public float getWidth(ITextComponent text) {
        final float[] width = {0};

        text.func_241878_f().accept((index, style, codePoint) -> {
            if (codePoint < fonts.glyphs.length && fonts.glyphs[codePoint] != null) {
                width[0] += fonts.glyphs[codePoint].width - 8;
            }

            return true;
        });

        return width[0];
    }
}