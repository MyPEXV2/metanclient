package relake.render.display.bqrender.buffer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;
import relake.common.InstanceAccess;

public class ByteqFramebuffer extends Framebuffer implements InstanceAccess {
    private boolean linear;

    public ByteqFramebuffer(int width, int height, boolean useDepth) {
        super(width, height, useDepth, Minecraft.IS_RUNNING_ON_MAC);
    }

    public ByteqFramebuffer(boolean useDepth) {
        super(1, 1, useDepth, Minecraft.IS_RUNNING_ON_MAC);
    }

    private static void resizeFramebuffer(ByteqFramebuffer framebuffer) {
        if (needsNewFramebuffer(framebuffer)) {
            framebuffer.createBuffers(Math.max(mc.getMainWindow().getFramebufferWidth(), 1), Math.max(mc.getMainWindow().getFramebufferHeight(), 1), Minecraft.IS_RUNNING_ON_MAC);
        }
    }

    public ByteqFramebuffer setLinear() {
        this.linear = true;
        return this;
    }

    @Override
    public void setFramebufferFilter(int framebufferFilterIn) {
        super.setFramebufferFilter(this.linear ? 9729 : framebufferFilterIn);
    }

    public void setup(boolean clear) {
        resizeFramebuffer(this);
        if (clear) this.framebufferClear(Minecraft.IS_RUNNING_ON_MAC);
        this.bindFramebuffer(false);
    }

    public void setup() {
        setup(true);
    }

    public static void drawQuads(MatrixStack matrix, double x, double y, double width, double height) {
        Matrix4f matrix4f = matrix.getLast().getMatrix();

        x = step(x, 0.5);
        y = step(y, 0.5);
        width = step(width, 0.5);
        height = step(height, 0.5);

        BUFFER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        BUFFER.pos(matrix4f, (float) x, (float) y, 0).tex(1, 0).endVertex();
        BUFFER.pos(matrix4f, (float) x, (float) (y + height), 0).tex(1, 1).endVertex();
        BUFFER.pos(matrix4f, (float) (x + width), (float) (y + height), 0).tex(0, 1).endVertex();
        BUFFER.pos(matrix4f, (float) (x + width), (float) y, 0).tex(0, 0).endVertex();
        TESSELLATOR.draw();
    }

    public static void drawQuads(double x, double y, double width, double height) {
        x = step(x, 0.5);
        y = step(y, 0.5);
        width = step(width, 0.5);
        height = step(height, 0.5);

        BUFFER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        BUFFER.pos((float) x, (float) y, 0).tex(1, 0).endVertex();
        BUFFER.pos((float) x, (float) (y + height), 0).tex(1, 1).endVertex();
        BUFFER.pos((float) (x + width), (float) (y + height), 0).tex(0, 1).endVertex();
        BUFFER.pos((float) (x + width), (float) y, 0).tex(0, 0).endVertex();
        TESSELLATOR.draw();
    }

    public static void flipQuads(double x, double y, double width, double height) {
        x = step(x, 0.5);
        y = step(y, 0.5);
        width = step(width, 0.5);
        height = step(height, 0.5);

        BUFFER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        BUFFER.pos(x, y, 0).tex(0, 1).endVertex();
        BUFFER.pos(x, y + height, 0).tex(0, 0).endVertex();
        BUFFER.pos(x + width, y + height, 0).tex(1, 0).endVertex();
        BUFFER.pos(x + width, y, 0).tex(1, 1).endVertex();
        TESSELLATOR.draw();
    }

    public static void flipQuads(double width, double height) {
        flipQuads(0, 0, width, height);
    }

    public static void drawQuads(MatrixStack matrix) {
        drawQuads(matrix, 0, 0, mw.getScaledWidth(), mw.getScaledHeight());
    }

    public static void drawQuads() {
        drawQuads(0, 0, mw.getScaledWidth(), mw.getScaledHeight());
    }

    public static void flipQuads() {
        flipQuads(0, 0, mw.getScaledWidth(), mw.getScaledHeight());
    }

    public static void drawQuads(MatrixStack matrix, double width, double height) {
        drawQuads(matrix, 0, 0, width, height);
    }

    public static void drawQuads(double width, double height) {
        drawQuads(0, 0, width, height);
    }

    public static void drawQuads(MatrixStack matrix, double x, double y, double width, double height, int color) {
        Matrix4f matrix4f = matrix.getLast().getMatrix();

        x = step(x, 0.5);
        y = step(y, 0.5);
        width = step(width, 0.5);
        height = step(height, 0.5);

        BUFFER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
        BUFFER.pos(matrix4f, (float) x, (float) y, 0).color(color).tex(1, 0).endVertex();
        BUFFER.pos(matrix4f, (float) x, (float) (y + height), 0).color(color).tex(1, 1).endVertex();
        BUFFER.pos(matrix4f, (float) (x + width), (float) (y + height), 0).color(color).tex(0, 1).endVertex();
        BUFFER.pos(matrix4f, (float) (x + width), (float) y, 0).color(color).tex(0, 0).endVertex();
        TESSELLATOR.draw();
    }

    public static void drawQuads(double x, double y, double width, double height, int color) {

        x = step(x, 0.5);
        y = step(y, 0.5);
        width = step(width, 0.5);
        height = step(height, 0.5);

        BUFFER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
        BUFFER.pos((float) x, (float) y, 0).color(color).tex(1, 0).endVertex();
        BUFFER.pos((float) x, (float) (y + height), 0).color(color).tex(1, 1).endVertex();
        BUFFER.pos((float) (x + width), (float) (y + height), 0).color(color).tex(0, 1).endVertex();
        BUFFER.pos((float) (x + width), (float) y, 0).color(color).tex(0, 0).endVertex();
        TESSELLATOR.draw();
    }

    public static void drawQuads(MatrixStack matrix, int color) {
        drawQuads(matrix, 0, 0, mw.getScaledWidth(), mw.getScaledHeight(), color);
    }

    public static void drawQuads(int color) {
        drawQuads(0, 0, mw.getScaledWidth(), mw.getScaledHeight(), color);
    }

    public static void drawQuads(MatrixStack matrix, double width, double height, int color) {
        drawQuads(matrix, 0, 0, width, height, color);
    }

    public static void drawQuads(double width, double height, int color) {
        drawQuads(0, 0, width, height, color);
    }

    public void draw() {
        this.bindFramebufferTexture();
        drawQuads();
    }

    public void flipDraw() {
        this.bindFramebufferTexture();
        flipQuads();
    }

    public void draw(int color) {
        this.bindFramebufferTexture();
        drawQuads(color);
    }

    public void draw(Framebuffer framebuffer) {
        framebuffer.bindFramebufferTexture();
        drawQuads();
    }

    public void stop() {
        unbindFramebuffer();
        mc.getFramebuffer().bindFramebuffer(true);
    }

    public static ByteqFramebuffer createFrameBuffer(ByteqFramebuffer framebuffer) {
        return createFrameBuffer(framebuffer, false);
    }

    public static ByteqFramebuffer createFrameBuffer(ByteqFramebuffer framebuffer, boolean depth) {
        if (needsNewFramebuffer(framebuffer)) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new ByteqFramebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), depth);
        }
        return framebuffer;
    }

    public static boolean needsNewFramebuffer(ByteqFramebuffer framebuffer) {
        return framebuffer == null || framebuffer.framebufferWidth != mw.getFramebufferWidth() || framebuffer.framebufferHeight != mw.getFramebufferHeight();
    }

    public static double step(double value, double steps) {
        double roundedValue = Math.round(value / steps) * steps;
        return Math.round(roundedValue * 100D) / 100D;
    }
}