package relake.render.display.bqrender;

import lombok.Getter;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import relake.Client;
import relake.common.util.ColorUtil;
import relake.module.implement.render.ShaderHandsModule;
import relake.render.display.bqrender.buffer.ByteqFramebuffer;
import relake.render.display.bqrender.framelimiter.FrameLimiterSimple;

public enum BlurShader {
    INSTANCE;

    @Getter
    private final ByteqFramebuffer buffer = new ByteqFramebuffer(false).setLinear();
    @Getter
    private final ByteqFramebuffer cache = new ByteqFramebuffer(false).setLinear();

    private final Shaders kawaseUp = Shaders.kawaseUp;
    private final Shaders kawaseDown = Shaders.kawaseDown;
    private final FrameLimiterSimple frameLimiter = new FrameLimiterSimple(60);

    public void updateBlur(float offset, int steps) {
        if (!Client.instance.moduleManager.shaderHandsModule.isEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        MainWindow mw = mc.getMainWindow();
        Framebuffer framebuffer = mc.getFramebuffer();

        float saturation = 1.1f;
        float tintIntensity = ShaderHandsModule.chromaticity.getValue() / 1100f;
        float[] tintColor = ColorUtil.getRGBf(Client.instance.moduleManager.hudModule.color.getValue().getRGB());
        boolean blur = true;
        if (blur && !ShaderHandsModule.blur.getValue()) {
            offset = 0;
            steps = 2;
        }
        if (!blur) {
            buffer.setup();
            framebuffer.bindFramebufferTexture();
            ByteqFramebuffer.drawQuads();
            GL13.glActiveTexture(GL20.GL_TEXTURE5);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, buffer.framebufferTexture);
            GL13.glActiveTexture(GL20.GL_TEXTURE0);
            framebuffer.bindFramebuffer(false);
            buffer.stop();
            return;
        }
        float finalOffset = offset;
        int finalSteps = steps;
        frameLimiter.render(() -> {
            cache.setup();
            framebuffer.bindFramebufferTexture();

            setupBlur(finalOffset, saturation, tintIntensity, tintColor, kawaseDown);
            ByteqFramebuffer.flipQuads(mw.getScaledWidth(), mw.getScaledHeight());
            cache.stop();
            ByteqFramebuffer[] buffers = {this.cache, this.buffer};

            for (int i = 1; i < finalSteps; ++i) {
                int step = i % 2;
                buffers[step].setup();
                buffers[(step + 1) % 2].bindFramebufferTexture();
                ByteqFramebuffer.flipQuads(mw.getScaledWidth(), mw.getScaledHeight());
                buffers[step].stop();
            }

            setupBlur(finalOffset, saturation, tintIntensity, tintColor, kawaseUp);

            for (int i = 0; i < finalSteps; ++i) {
                int step = i % 2;
                buffers[(step + 1) % 2].setup();
                buffers[step].bindFramebufferTexture();
                ByteqFramebuffer.flipQuads(mw.getScaledWidth(), mw.getScaledHeight());
                buffers[step].stop();
            }

            kawaseUp.unload();
            kawaseDown.unload();
            framebuffer.bindFramebuffer(true);
        });

        GL13.glActiveTexture(GL20.GL_TEXTURE5);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, buffer.framebufferTexture);
        GL13.glActiveTexture(GL20.GL_TEXTURE0);
    }

    private void setupBlur(float offset, float saturation, float tintIntensity, float[] tintColor, Shaders shader) {
        shader.load();
        shader.setUniformi("image", 0);
        shader.setUniformf("offset", offset);
        shader.setUniformf("resolution", 1f / buffer.framebufferWidth, 1f / buffer.framebufferHeight);
        shader.setUniformf("saturation", saturation);
        shader.setUniformf("tintIntensity", tintIntensity);
        shader.setUniformf("tintColor", tintColor);
    }
}
