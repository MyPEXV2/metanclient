package relake.render.display.shape;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;
import relake.Client;
import relake.common.util.ColorUtil;
import relake.render.display.Render2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.mojang.blaze3d.systems.RenderSystem.*;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX_COLOR;
import static org.lwjgl.opengl.GL11.*;
import static relake.shader.ShaderRegister.*;

public class ShapeRenderer {
    public static List<BlurData> BLUR_DATA_LIST = new ArrayList<>();
    public MatrixStack matrixStack;
    public float x, y, width, height;

    public ShapeRenderer box(MatrixStack matrixStack, float x, float y, float width, float height) {
        this.matrixStack = matrixStack;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }

    public ShapeRenderer expand(Side side, float amount) {
        side.expand(this, amount);
        return this;
    }

    public ShapeRenderer move(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public void blur(int round) {
        this.blur(round, (int) MathHelper.clamp(Render2D.alphaPushed ? Render2D.alpha : 255, 0, 255));
    }

    public void blur(int round, int alpha) {
        BLUR_DATA_LIST.add(new BlurData(this, round, alpha));
    }

    public void quad(int round, int color) {
        quad(round, color, color, color, color);
    }

    public void quad(int round, int color1, int color2) {
        quad(round, color1, color2, color1, color2);
    }

    public void quad(int round, int... color) {
        matrixStack.push();
        matrixStack.scaleFix(1, 1, 1);
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        ROUND_SHADER.begin();

        ROUND_SHADER.setUniform("size", width, height);
        ROUND_SHADER.setUniform("radius", round);
        ROUND_SHADER.setUniform("noise", .015F);
        ROUND_SHADER.setUniform("color1", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color[0], Render2D.alphaPushed ? Render2D.alpha : 255F)));
        ROUND_SHADER.setUniform("color2", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color[1], Render2D.alphaPushed ? Render2D.alpha : 255F)));
        ROUND_SHADER.setUniform("color3", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color[2], Render2D.alphaPushed ? Render2D.alpha : 255F)));
        ROUND_SHADER.setUniform("color4", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color[3], Render2D.alphaPushed ? Render2D.alpha : 255F)));

        ROUND_SHADER.drawQuads(matrixStack, x, y, width, height);

        ROUND_SHADER.end();
        disableBlend();
        matrixStack.pop();
    }

    public void glow(int round, int shadow, int color) {
        glow(round, shadow, color, color, color, color);
    }

    public void glow(int round, int shadow, int... color) {
        matrixStack.push();
        matrixStack.scaleFix(1, 1, 1);
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GLOW_SHADER.begin();

        GLOW_SHADER.setUniform("size", width, height);
        GLOW_SHADER.setUniform("radius", round);
        GLOW_SHADER.setUniform("shadow", (float) shadow / 2);

        GLOW_SHADER.setUniform("noise", 0);
        GLOW_SHADER.setUniform("color1", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color[0], Render2D.alphaPushed ? Render2D.alpha : 255F)));
        GLOW_SHADER.setUniform("color2", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color[1], Render2D.alphaPushed ? Render2D.alpha : 255F)));
        GLOW_SHADER.setUniform("color3", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color[2], Render2D.alphaPushed ? Render2D.alpha : 255F)));
        GLOW_SHADER.setUniform("color4", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color[3], Render2D.alphaPushed ? Render2D.alpha : 255F)));

        GLOW_SHADER.drawQuads(matrixStack, x - shadow, y - shadow, width + shadow * 2, height + shadow * 2);

        GLOW_SHADER.end();
        disableBlend();
        matrixStack.pop();
    }

    public void corner(float[] round, int color) {
        matrixStack.push();
        matrixStack.scaleFix(1, 1, 1);
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        CORNER_ROUND_SHADER.begin();

        CORNER_ROUND_SHADER.setUniform("size", width, height);
        CORNER_ROUND_SHADER.setUniform("round", round);
        CORNER_ROUND_SHADER.setUniform("smoothness", 0, 2.0f);
        CORNER_ROUND_SHADER.setUniform("color", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color, Render2D.alphaPushed ? Render2D.alpha : 255F)));

        CORNER_ROUND_SHADER.drawQuads(matrixStack, x, y, width, height);

        CORNER_ROUND_SHADER.end();
        disableBlend();
        matrixStack.pop();
    }

    private void start() {
        RenderSystem.clearCurrentColor();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableDepthTest();
        RenderSystem.shadeModel(7425);
    }

    public void customRound(MatrixStack matrix, float x, float y, float width, float height, boolean shadow, float shadowAlpha, float offset, float value, float smoothness1, float smoothness2, int color1, int color2, int color3, int color4, int outlineColor, Round round) {
        matrixStack.push();
        matrixStack.scaleFix(1, 1, 1);
        enableBlend();
        defaultAlphaFunc();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        ROUND_HUD_SHADER.begin();

        ROUND_HUD_SHADER.setUniform("size", width, height);
        ROUND_HUD_SHADER.setUniform("round", round.RT, round.RB, round.LT, round.LB);
        ROUND_HUD_SHADER.setUniform("smoothness", smoothness1, smoothness2);
        ROUND_HUD_SHADER.setUniform("value", value);// у тебя бинд на ту же клавишу на которую дебаг((
        ROUND_HUD_SHADER.setUniform("shadow", shadow ? Render2D.alpha / 255F : 0);
        ROUND_HUD_SHADER.setUniform("shadowAlpha", shadow ? Render2D.alphaPushed ? Render2D.alpha / 255F * shadowAlpha : shadowAlpha : 0);
        ROUND_HUD_SHADER.setUniform("color1", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color1, Render2D.alphaPushed ? Render2D.alpha : 255F)));
        ROUND_HUD_SHADER.setUniform("color2", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color2, Render2D.alphaPushed ? Render2D.alpha : 255F)));
        ROUND_HUD_SHADER.setUniform("color3", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color3, Render2D.alphaPushed ? Render2D.alpha : 255F)));
        ROUND_HUD_SHADER.setUniform("color4", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color4, Render2D.alphaPushed ? Render2D.alpha : 255F)));
        start();
        ROUND_HUD_SHADER.drawQuads(matrixStack, x, y, width, height);

        ROUND_HUD_SHADER.end();
        disableBlend();
        matrixStack.pop();
    }

    public static void drawFace(MatrixStack matrixStack,
                                LivingEntity target,
                                float x,
                                float y,
                                int width,
                                int height,
                                float radius,
                                float hurtStrength) {
        ResourceLocation entityTexture = mc.getRenderManager().getRenderer(target).getEntityTexture(target);

        mc.getTextureManager().bindTexture(entityTexture);

        matrixStack.push();
        matrixStack.scaleFix(1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableAlphaTest();

        RenderSystem.color4f(1, 1, 1, 1);

        HEAD_SHADER.begin();
        HEAD_SHADER.setUniform("width", width);
        HEAD_SHADER.setUniform("height", height);
        HEAD_SHADER.setUniform("radius", radius);
        HEAD_SHADER.setUniform("hurtStrength", hurtStrength);
        HEAD_SHADER.setUniform("alpha", Render2D.alphaPushed ? Render2D.alpha / 255F * 1.5F : 1.5F);
        HEAD_SHADER.drawQuads(matrixStack, x, y, width, height);
        HEAD_SHADER.end();

        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
        matrixStack.pop();
    }

    public void outlineHud(int round, int outline, int color) {
        outlineHud(round, outline, color, color, color, color);
    }

    public void outlineHud(int round, int outline, int color1, int color2, int color3, int color4) {
        matrixStack.push();
        matrixStack.scaleFix(1, 1, 1);
        enableBlend();
        defaultAlphaFunc();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        OUTLINE_ROUND_HUD_SHADER.begin();

        OUTLINE_ROUND_HUD_SHADER.setUniform("size", width, height);
        OUTLINE_ROUND_HUD_SHADER.setUniform("round", round, round, round, round);
        OUTLINE_ROUND_HUD_SHADER.setUniform("smoothness", -outline, outline);
        OUTLINE_ROUND_HUD_SHADER.setUniform("softness", -outline, outline);
        OUTLINE_ROUND_HUD_SHADER.setUniform("thickness", 0, outline);
        OUTLINE_ROUND_HUD_SHADER.setUniform("value", outline);
        OUTLINE_ROUND_HUD_SHADER.setUniform("color1", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color1, Render2D.alphaPushed ? Render2D.alpha : 255F)));
        OUTLINE_ROUND_HUD_SHADER.setUniform("color2", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color2, Render2D.alphaPushed ? Render2D.alpha : 255F)));
        OUTLINE_ROUND_HUD_SHADER.setUniform("color3", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color3, Render2D.alphaPushed ? Render2D.alpha : 255F)));
        OUTLINE_ROUND_HUD_SHADER.setUniform("color4", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color4, Render2D.alphaPushed ? Render2D.alpha : 255F)));
        start();
        OUTLINE_ROUND_HUD_SHADER.drawQuads(matrixStack, x, y, width, height);

        OUTLINE_ROUND_HUD_SHADER.end();
        disableBlend();
        matrixStack.pop();
    }

    public void outline(float round, int thickness, int color) {
        matrixStack.push();
        matrixStack.scaleFix(1, 1, 1);
        enableBlend();
        defaultAlphaFunc();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        OUTLINE_ROUND_SHADER.begin();

        OUTLINE_ROUND_SHADER.setUniform("size", width, height);
        OUTLINE_ROUND_SHADER.setUniform("radius", round);
        OUTLINE_ROUND_SHADER.setUniform("borderSize", thickness);
        OUTLINE_ROUND_SHADER.setUniform("color", ColorUtil.getRGBAf(ColorUtil.applyOpacity(color, Render2D.alphaPushed ? Render2D.alpha : 255F)));

        OUTLINE_ROUND_SHADER.drawQuads(matrixStack, x, y, width, height);

        OUTLINE_ROUND_SHADER.end();
        disableBlend();
        matrixStack.pop();
    }

    public void texture(int round) {
        matrixStack.push();
        matrixStack.scaleFix(1, 1, 1);
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        TEXTURE_ROUND_SHADER.begin();

        TEXTURE_ROUND_SHADER.setUniform("rectSize", width, height);
        TEXTURE_ROUND_SHADER.setUniform("radius", round);
        TEXTURE_ROUND_SHADER.setUniform("alpha", Render2D.alphaPushed ? Render2D.alpha / 255F * 1.5F : 1.5F); //

        TEXTURE_ROUND_SHADER.drawQuads(matrixStack, x, y, width, height);

        bindTexture(0);

        TEXTURE_ROUND_SHADER.end();
        disableBlend();
        matrixStack.pop();
    }

    public void texture(ResourceLocation resourceLocation, int color) {
        pushMatrix();
        depthMask(false);
        enableBlend();
        shadeModel(GL_SMOOTH);
        disableAlphaTest();
        blendFuncSeparate(GL_SRC_ALPHA, GL_ONE, GL_ZERO, GL_ONE);

        color = ColorUtil.applyOpacity(color, Render2D.alphaPushed ? Render2D.alpha : 255F);

        mc.getTextureManager().bindTexture(resourceLocation);

        BUFFER.begin(GL_QUADS, POSITION_TEX_COLOR);
        {
            Matrix4f matrix = matrixStack.getLast().getMatrix();
            BUFFER.pos(matrix, x, y).tex(0, 0).color(color).endVertex();
            BUFFER.pos(matrix, x, y + height).tex(0, 1).color(color).endVertex();
            BUFFER.pos(matrix, x + width, y + height).tex(1, 1).color(color).endVertex();
            BUFFER.pos(matrix, x + width, y).tex(1, 0).color(color).endVertex();
        }
        TESSELLATOR.draw();

        disableBlend();
        enableAlphaTest();
        depthMask(true);
        popMatrix();
    }

    public void texture(ResourceLocation resourceLocation, int[] color) {
        this.texture(resourceLocation, color, true);
    }

    public void texture(ResourceLocation resourceLocation, int[] color, boolean colorBlend) {
        pushMatrix();
        depthMask(false);
        enableBlend();
        shadeModel(GL_SMOOTH);
        disableAlphaTest();

        if (colorBlend)
            blendFuncSeparate(GL_SRC_ALPHA, GL_ONE, GL_ZERO, GL_ONE);
        else
            GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        color[0] = ColorUtil.applyOpacity(color[0], Render2D.alphaPushed ? Render2D.alpha : 255F);
        color[1] = ColorUtil.applyOpacity(color[1], Render2D.alphaPushed ? Render2D.alpha : 255F);
        color[2] = ColorUtil.applyOpacity(color[2], Render2D.alphaPushed ? Render2D.alpha : 255F);
        color[3] = ColorUtil.applyOpacity(color[3], Render2D.alphaPushed ? Render2D.alpha : 255F);

        mc.getTextureManager().bindTexture(resourceLocation);

        BUFFER.begin(GL_QUADS, POSITION_TEX_COLOR);

        {
            Matrix4f matrix = matrixStack.getLast().getMatrix();

            BUFFER.pos(matrix, x, y).tex(0, 0).color(color[0]).endVertex();
            BUFFER.pos(matrix, x, y + height).tex(0, 1).color(color[1]).endVertex();
            BUFFER.pos(matrix, x + width, y + height).tex(1, 1).color(color[2]).endVertex();
            BUFFER.pos(matrix, x + width, y).tex(1, 0).color(color[3]).endVertex();
        }

        TESSELLATOR.draw();

        enableAlphaTest();
        disableBlend();
        depthMask(true);
        popMatrix();
    }

    public void circle(int color) {
        quad((int) (width / 2 - 1), color);
    }

    public void drawShadow(MatrixStack matrix, float radius, int color) {
        drawShadow(matrix, radius, 1f, color, color, color, color);
    }

    public void drawShadow(MatrixStack matrix, float radius, int color1, int color2, int color3, int color4) {
        customRound(matrix, x - radius, y - radius, width + (radius * 2F), height + (radius * 2F), true, 1f, 0, radius, -radius, radius, color1, color2, color3, color4, 0, Round.of(radius));
    }

    public void drawShadow(MatrixStack matrix, float radius, int color1, int color2, int color3, int color4, Round round) {
        customRound(matrix, x - radius, y - radius, width + (radius * 2F), height + (radius * 2F), true, 1f, 0, radius, -radius, radius, color1, color2, color3, color4, 0, round);
    }

    public void drawShadow(MatrixStack matrix, float radius, int color, Round round) {
        drawShadow(matrix, radius, 1f, color, color, color, color, round);
    }

    public void drawShadow(MatrixStack matrix, float radius, float alpha, int color) {
        drawShadow(matrix, radius, alpha, color, color, color, color);
    }

    public void drawShadow(MatrixStack matrix, float radius, float alpha, int color1, int color2, int color3, int color4) {
        customRound(matrix, x - radius, y - radius, width + (radius * 2F), height + (radius * 2F), true, alpha, 0, radius, -radius, radius, color1, color2, color3, color4, 0, Round.of(radius));
    }

    public void drawShadow(MatrixStack matrix, float radius, float alpha, int color1, int color2, int color3, int color4, Round round) {
        customRound(matrix, x - radius, y - radius, width + (radius * 2F), height + (radius * 2F), true, alpha, 0, radius, -radius, radius, color1, color2, color3, color4, 0, round);
    }

    public void drawShadow(MatrixStack matrix, float radius, float alpha, int color, Round round) {
        drawShadow(matrix, radius, alpha, color, color, color, color, round);
    }

    public record BlurData(ShapeRenderer shapeRenderer, int rounding, int alpha) {
    }

    public static void enable(final int glTarget) {
        GL11.glEnable(glTarget);
    }

    public static void disable(final int glTarget) {
        GL11.glDisable(glTarget);
    }

    public static final void color(Color color) {
        if (color == null)
            color = Color.white;
        color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
    }

    public static final void color(double red, double green, double blue, double alpha) {
        GL11.glColor4d(red, green, blue, alpha);
    }


    public static void start1() {
        GlStateManager.pushMatrix();
        enable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        disable(GL11.GL_TEXTURE_2D);
        disable(GL11.GL_CULL_FACE);
        depthMask(false);
    }

    public static void stop1() {
        depthMask(true);
        enable(GL11.GL_CULL_FACE);
        enable(GL11.GL_TEXTURE_2D);
        disable(GL11.GL_BLEND);
        color(Color.white);
        GlStateManager.popMatrix();
    }


}
