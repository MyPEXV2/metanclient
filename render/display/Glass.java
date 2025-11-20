package relake.render.display;

import com.mojang.blaze3d.platform.GlStateManager;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import relake.common.InstanceAccess;
import relake.common.util.ColorUtil;

import static relake.shader.ShaderRegister.TORUS_SHADER;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Glass implements InstanceAccess {

    @NonFinal
    static Framebuffer framebuffer = new Framebuffer(1, 1, false);

    public static void draw() {
        //shader = new Glass();

        GlStateManager.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL13.glBindTexture(GL13.GL_TEXTURE_2D, mc.getFramebuffer().framebufferTexture);
        TORUS_SHADER.begin();
        TORUS_SHADER.setUniformi("inputSampler", 0);
        TORUS_SHADER.setUniform("inputResolution", (float) mc.getFramebuffer().framebufferTextureWidth, (float) mc.getFramebuffer().framebufferTextureHeight);
        TORUS_SHADER.setUniform("blurAmount", 0);
        TORUS_SHADER.setUniform("reflect", 22);
        TORUS_SHADER.setUniform("noiseValue", 0.01f);
        TORUS_SHADER.setUniform("vertexColor", ColorUtil.getRGBAf(-1));
    }

    public static void draw(Framebuffer framebuffer) {
        //shader = new Glass();

        GL13.glBindTexture(GL13.GL_TEXTURE_2D, framebuffer.framebufferTexture);
        TORUS_SHADER.begin();
        TORUS_SHADER.setUniformi("inputSampler", 0);
        TORUS_SHADER.setUniform("inputResolution", (float) framebuffer.framebufferTextureWidth, (float) framebuffer.framebufferTextureHeight);
        TORUS_SHADER.setUniform("blurAmount", 0);
        TORUS_SHADER.setUniform("reflect", 22);
        TORUS_SHADER.setUniform("noiseValue", 0.01f);
        TORUS_SHADER.setUniform("vertexColor", ColorUtil.getRGBAf(-1));
    }

    public static void end() {
        TORUS_SHADER.end();

        GlStateManager.disableBlend();
        GlStateManager.bindTexture(0);
    }
}