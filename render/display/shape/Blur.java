package relake.render.display.shape;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import relake.common.InstanceAccess;
import relake.common.util.TextureUtil;
import relake.shader.ShaderRegister;

import java.util.ArrayList;
import java.util.List;

import static relake.shader.ShaderRegister.KAWASE_DOWN_SHADER;
import static relake.shader.ShaderRegister.KAWASE_UP_SHADER;

@UtilityClass
public class Blur implements InstanceAccess {

    public Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);

    private Framebuffer framebuffer = new Framebuffer(1, 1, false);

    private int currentIterations;

    private final List<Framebuffer> framebufferList = new ArrayList<>();

    private void initFramebuffers(float iterations) {
        for (Framebuffer framebuffer : framebufferList) {
            framebuffer.deleteFramebuffer();
        }

        framebufferList.clear();

        framebufferList.add(framebuffer = ShaderRegister.createFrameBuffer(framebuffer));

        for (int i = 1; i <= iterations; i++) {
            Framebuffer currentBuffer = new Framebuffer((int) (mw.getScaledWidth() / Math.pow(2, i)), (int) (mw.getScaledHeight() / Math.pow(2, i)), false);
            currentBuffer.setFramebufferFilter(GL11.GL_LINEAR);
            GlStateManager.bindTexture(currentBuffer.framebufferTexture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL14.GL_MIRRORED_REPEAT);
            GlStateManager.bindTexture(0);

            framebufferList.add(currentBuffer);
        }
    }

    public void renderBlur(MatrixStack ms, int stencilFrameBufferTexture, int iterations, float offset) {
        if (framebuffer.framebufferWidth != mw.getFramebufferWidth() || framebuffer.framebufferHeight != mw.getFramebufferHeight()) {
            initFramebuffers(5);
            currentIterations = iterations;
        }

        renderFBO(ms, framebufferList.get(1), mc.getFramebuffer().framebufferTexture, KAWASE_DOWN_SHADER, offset);

        // вниз
        for (int i = 1; i < iterations; i++) {
            renderFBO(ms, framebufferList.get(i + 1), framebufferList.get(i).framebufferTexture, KAWASE_DOWN_SHADER, i <= 2 ? 0 : offset);
        }

        // вверх
        for (int i = iterations; i > 1; i--) {
            renderFBO(ms, framebufferList.get(i - 1), framebufferList.get(i).framebufferTexture, KAWASE_UP_SHADER, i <= 2 ? 0 : offset);
        }

        Framebuffer lastBuffer = framebufferList.get(0);
        lastBuffer.framebufferClear();
        lastBuffer.bindFramebuffer(false);
        KAWASE_UP_SHADER.begin();
        KAWASE_UP_SHADER.setUniform("offset", offset, offset);
        KAWASE_UP_SHADER.setUniformi("inTexture", 0);
        KAWASE_UP_SHADER.setUniformi("check", 1);
        KAWASE_UP_SHADER.setUniformi("textureToCheck", 16);
        KAWASE_UP_SHADER.setUniform("halfpixel", 1.0f / lastBuffer.framebufferWidth, 1.0f / lastBuffer.framebufferHeight);
        KAWASE_UP_SHADER.setUniform("iResolution", lastBuffer.framebufferWidth, lastBuffer.framebufferHeight);

        GL13.glActiveTexture(GL13.GL_TEXTURE16);
        TextureUtil.bindTexture(stencilFrameBufferTexture);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        TextureUtil.bindTexture(framebufferList.get(1).framebufferTexture);
        KAWASE_UP_SHADER.drawQuads(ms);
        KAWASE_UP_SHADER.end();

        mc.getFramebuffer().bindFramebuffer(true);
        TextureUtil.bindTexture(framebufferList.get(0).framebufferTexture);
        GlStateManager.enableBlend();
        KAWASE_UP_SHADER.drawQuads(ms);
        GlStateManager.bindTexture(0);
    }

    private void renderFBO(MatrixStack ms, Framebuffer framebuffer, int framebufferTexture, ShaderRegister shader, float offset) {
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(false);
        shader.begin();
        TextureUtil.bindTexture(framebufferTexture);
        shader.setUniform("offset", offset, offset);
        shader.setUniformi("inTexture", 0);
        shader.setUniformi("check", 0);
        shader.setUniform("halfpixel", 1.0f / framebuffer.framebufferWidth, 1.0f / framebuffer.framebufferHeight);
        shader.setUniform("iResolution", framebuffer.framebufferWidth, framebuffer.framebufferHeight);
        shader.drawQuads(ms);
        shader.end();
    }
}
