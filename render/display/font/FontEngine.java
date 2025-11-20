package relake.render.display.font;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.SneakyThrows;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.vector.Matrix4f;
import relake.common.InstanceAccess;
import relake.common.util.ColorUtil;
import relake.common.util.FileUtil;
import relake.render.display.Render2D;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import static java.awt.Font.TRUETYPE_FONT;
import static java.awt.Font.createFont;
import static java.awt.RenderingHints.*;
import static org.lwjgl.BufferUtils.createByteBuffer;

public class FontEngine implements InstanceAccess {
    private final int imageSize = 512;
    protected final Glyph[] glyphs = new Glyph[2100];
    protected final DynamicTexture loadedTexture;

    @SneakyThrows
    public FontEngine(String name, int size) {
        String path = I18n.format("font/%s", name);

        Font font = createFont(TRUETYPE_FONT, FileUtil.createStream(path))
                .deriveFont(Font.PLAIN, size);

        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);

        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        ImageIO.write(this.generateFontImage(font), "png", bas);

        byte[] bytes = bas.toByteArray();

        ByteBuffer data = createByteBuffer(bytes.length)
                .put(bytes)
                .flip();

        this.loadedTexture = new DynamicTexture(NativeImage.read(data));
    }


    protected void drawGlyph(MatrixStack stack,
                             Glyph glyph,
                             float x,
                             float y,
                             int rgba) {
        x = Math.round(x);
        y = Math.round(y);
        rgba = ColorUtil.applyOpacity(rgba, Render2D.alphaPushed ? Render2D.alpha : 255F);

        float pageX = glyph.x / (float) imageSize,
                pageY = glyph.y / (float) imageSize,
                pageWidth = glyph.width / (float) imageSize,
                pageHeight = glyph.height / (float) imageSize,
                width = glyph.width,
                height = glyph.height;

        Matrix4f matrix = stack.getLast().getMatrix();

        BUFFER.pos(matrix, x, y).tex(pageX, pageY).color(rgba).endVertex();
        BUFFER.pos(matrix, x, y + height).tex(pageX, pageY + pageHeight).color(rgba).endVertex();
        BUFFER.pos(matrix, x + width, y + height).tex(pageX + pageWidth, pageY + pageHeight).color(rgba).endVertex();
        BUFFER.pos(matrix, x + width, y).tex(pageX + pageWidth, pageY).color(rgba).endVertex();
    }

    private BufferedImage generateFontImage(Font font) {
        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setFont(font);
        graphics.setColor(new Color(255, 255, 255, 0));
        graphics.fillRect(0, 0, imageSize, imageSize);
        graphics.setColor(Color.WHITE);
        graphics.setRenderingHint(KEY_FRACTIONALMETRICS, VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);

        FontMetrics fontMetrics = graphics.getFontMetrics();
        int x = 0, y = 1, height = 0;

        for (char i = 0; i < glyphs.length; i++) {
            if (i > 1030 || i < 256) {
                Glyph vector = new Glyph();

                if (font.canDisplay(i)) {

                    Rectangle2D rectangle = fontMetrics.getStringBounds(String.valueOf(i), graphics);

                    vector.width = rectangle.getBounds().width + 8;
                    vector.height = rectangle.getBounds().height;

                    if (x + vector.width >= imageSize) {
                        x = 0;
                        y += height;
                        height = 0;
                    }

                    height = Math.max(vector.height, height);
                    vector.x = x;
                    vector.y = y;
                    glyphs[i] = vector;

                    graphics.drawString(String.valueOf(i), x + 2, y + fontMetrics.getAscent());
                    x += vector.width;
                }
            }
        }
        graphics.dispose();
        return image;
    }

    public static class Glyph {
        public int x, y,
                width,
                height;
    }
}
