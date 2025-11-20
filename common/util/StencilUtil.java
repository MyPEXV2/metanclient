package relake.common.util;

import lombok.experimental.UtilityClass;
import net.minecraft.client.shader.Framebuffer;
import relake.common.InstanceAccess;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT;
import static org.lwjgl.opengl.GL11.*;

@UtilityClass
public class StencilUtil implements InstanceAccess {
    public void begin() {
        Framebuffer framebuffer = mc.getFramebuffer();
        if (framebuffer.depthBuffer > -1) {
            mc.getFramebuffer().bindFramebuffer(false);

            glDeleteRenderbuffersEXT(framebuffer.depthBuffer);
            final int stencilDepthBufferID = glGenRenderbuffersEXT();
            glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, stencilDepthBufferID);
            glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_DEPTH_STENCIL_EXT, mw.getWidth(), mw.getHeight());
            glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_STENCIL_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT, stencilDepthBufferID);
            glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT, stencilDepthBufferID);
            framebuffer.depthBuffer = -1;
        }

        glStencilMask(0xFF);
        glClear(GL_STENCIL_BUFFER_BIT);
        glEnable(GL_STENCIL_TEST);
        glStencilFunc(GL_ALWAYS, 1, 1);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        glDisable(GL_DEPTH_TEST);
        glColorMask(false, false, false, false);
    }

    public void read(int ref) {
        glColorMask(true, true, true, true);
        glStencilFunc(GL_EQUAL, ref, 1);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
    }

    public void end() {
        glDisable(GL_STENCIL_TEST);
        glEnable(GL_DEPTH_TEST);
    }
}