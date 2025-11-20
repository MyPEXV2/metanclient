package relake.render.display;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import relake.Client;
import relake.common.InstanceAccess;
import relake.common.util.ColorUtil;
import relake.render.display.font.FontRegister;
import relake.render.display.font.TextRenderer;
import relake.render.display.shape.ShapeRenderer;
import relake.util.ScaleUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX;
import static org.lwjgl.opengl.GL11.GL_POLYGON;

@UtilityClass
public class Render2D implements InstanceAccess {
    public float alpha = 255;
    public boolean alphaPushed = false;

    public void pushAlpha(float alpha) {
        alphaPushed = true;
        Render2D.alpha = Math.min(Render2D.alpha / 255F * alpha, 255F);
    }

    public void popAlpha() {
        alphaPushed = false;
        alpha = 255;
    }

    private static final HashMap<Integer, Integer> textures = new HashMap<>();

    public static int downloadImage(String url) {
        int texId = -1;
        int identifier = Objects.hash(url);

        if (textures.containsKey(identifier)) {
            texId = textures.get(identifier);
        } else {
            try {
                URL stringURL = new URL(url);
                BufferedImage img = ImageIO.read(stringURL);
                ByteArrayOutputStream bas = new ByteArrayOutputStream();
                ImageIO.write(img, "png", bas);
                byte[] bytes = bas.toByteArray();
                ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
                buffer.put(bytes);
                buffer.flip();
                DynamicTexture dynamicTexture = new DynamicTexture(NativeImage.read(buffer));
                texId = dynamicTexture.getGlTextureId();
            } catch (IOException e) {
                e.printStackTrace();
            }

            textures.put(identifier, texId);
        }

        return texId;
    }

    public ShapeRenderer box(MatrixStack matrixStack, float x, float y, float width, float height) {
        return new ShapeRenderer().box(matrixStack, x, y, width, height);
    }

    public TextRenderer size(int size) {
        return new TextRenderer().size(size);
    }

    public TextRenderer size(FontRegister.Type font, int size) {
        return new TextRenderer().size(font, size);
    }

    // Ротейт
    public void initRotate(float x, float y, float value) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glRotatef(value, 0, 0, 1);
        GL11.glTranslatef(-x, -y, 0);
    }

    public void endRotate() {
        GL11.glPopMatrix();
    }

    public void drawTexture(MatrixStack matrixStack, double x, double y, double width, double height) {
        BUFFER.begin(GL_POLYGON, POSITION_TEX);
        {
            Matrix4f matrix = matrixStack.getLast().getMatrix();
            BUFFER.pos(matrix, (float) x, (float) y).tex(0, 1).endVertex();
            BUFFER.pos(matrix, (float) x, (float) (y + height)).tex(0, 0).endVertex();
            BUFFER.pos(matrix, (float) (x + width), (float) (y + height)).tex(1, 0).endVertex();
            BUFFER.pos(matrix, (float) (x + width), (float) y).tex(1, 1).endVertex();
        }
        TESSELLATOR.draw();
    }
    private final List<Vec2fColored> VERTEXES_COLORED = new ArrayList<>();
    private final List<Vec2f> VERTEXES = new ArrayList<>();

    public void duadsCircle(MatrixStack matrix, float x, float y, double radius, double c360, float width, boolean bloom, float alpha) {
        VERTEXES_COLORED.clear();
        int rgb = ColorUtil.applyOpacity(Client.instance.moduleManager.hudModule.color.getValue().getRGB(), alpha);
        for (Vec2f vec2f : generateRadiusCircledVertexes(x, y, radius, 0, 180, 180 + c360, 6, false)) {
            VERTEXES_COLORED.add(getOfVec3f(vec2f, rgb));
        }
        GL11.glPointSize(width);
        matrix.push();
        matrix.scaleFix(1,1,1);
        drawVertexesList(matrix, VERTEXES_COLORED, GL11.GL_POINTS, false, bloom);
        matrix.pop();
        GL11.glPointSize(1F);
    }

    public void duadsCircle(MatrixStack matrix, float x, float y, double radius, double c360, float width, boolean bloom, int color, float alpha) {
        VERTEXES_COLORED.clear();
        int rgb = ColorUtil.applyOpacity(color, alpha);
        for (Vec2f vec2f : generateRadiusCircledVertexes(x, y, radius, 0, 180, 180 + c360, 6, false)) {
            VERTEXES_COLORED.add(getOfVec3f(vec2f, rgb));
        }
        GL11.glPointSize(width);
        matrix.push();
        matrix.scaleFix(1,1,1);
        drawVertexesList(matrix, VERTEXES_COLORED, GL11.GL_POINTS, false, bloom);
        matrix.pop();
        GL11.glPointSize(1F);
    }


    private void setupRenderRect(boolean texture, boolean bloom) {
        if (texture) RenderSystem.enableTexture();
        else RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.shadeModel(7425);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, bloom ? GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA : GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.disableAlphaTest();
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
    }

    private void endRenderRect(boolean bloom) {
        RenderSystem.enableAlphaTest();
        if (bloom)
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.shadeModel(7424);
        RenderSystem.enableCull();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
        RenderSystem.clearCurrentColor();
    }
    public void drawVertexesList(MatrixStack matrix, List<Vec2fColored> vec2c, int begin, boolean texture, boolean bloom) {
        setupRenderRect(texture, bloom);
        BUFFER.begin(begin, texture ? DefaultVertexFormats.POSITION_TEX_COLOR : DefaultVertexFormats.POSITION_COLOR);
        int counter = 0;
        for (final Vec2fColored vec : vec2c) {
            float[] rgba = ColorUtil.getRGBAf(vec.getColor());
            BUFFER.pos(matrix.getLast().getMatrix(), vec.getX(), vec.getY(), 0);
            if (texture) BUFFER.tex(counter == 0 || counter == 3 ? 0 : 1, counter == 0 || counter == 1 ? 0 : 1);
            BUFFER.color(rgba[0], rgba[1], rgba[2], rgba[3]);
            BUFFER.endVertex();
            counter++;
        }
        TESSELLATOR.draw();
        endRenderRect(bloom);
    }
    private List<Vec2f> generateRadiusCircledVertexes(float x, float y, double radius1, double radius2, double startRadius, double endRadius, double step, boolean doublepart) {
        VERTEXES.clear();
        double radius = startRadius;
        while (radius <= endRadius) {
            if (radius > endRadius) radius = endRadius;
            float x1 = (float) (Math.sin(Math.toRadians(radius)) * radius1);
            float y1 = (float) (-Math.cos(Math.toRadians(radius)) * radius1);
            VERTEXES.add(new Vec2f(x + x1, y + y1));
            if (doublepart) {
                x1 = (float) (Math.sin(Math.toRadians(radius)) * radius2);
                y1 = (float) (-Math.cos(Math.toRadians(radius)) * radius2);
                VERTEXES.add(new Vec2f(x + x1, y + y1));
            }
            radius += step;
        }
        return VERTEXES;
    }
    @Getter
    @AllArgsConstructor
    public class Vec2fColored {
        float x, y;
        int color;
    }
    public Vec2fColored getOfVec3f(Vec2f vec2f, int color) {
        return new Vec2fColored(vec2f.getX(), vec2f.getY(), color);
    }
    @Getter
    @AllArgsConstructor
    public class Vec2f {
        public float x, y;
    }
}
