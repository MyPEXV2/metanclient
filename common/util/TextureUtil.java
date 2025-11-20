package relake.common.util;

import com.mojang.blaze3d.platform.GlStateManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.optifine.Config;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

@UtilityClass
public class TextureUtil {
    private final IntBuffer DATA_BUFFER = GLAllocation.createDirectIntBuffer(4194304);
    public final DynamicTexture MISSING_TEXTURE = new DynamicTexture(16, 16, false);
    public final int[] MISSING_TEXTURE_DATA = MISSING_TEXTURE.getTextureData().makePixelArray();
    private final float[] COLOR_GAMMAS;
    private int[] dataArray = new int[4194304];


    public void deleteTexture(int textureId) {
        GlStateManager.deleteTexture(textureId);
    }

    public int uploadTextureImageAllocate(int textureId, BufferedImage texture, boolean blur, boolean clamp) {
        allocateTexture(textureId, texture.getWidth(), texture.getHeight());
        return uploadTextureImageSub(textureId, texture, 0, 0, blur, clamp);
    }

    public void allocateTexture(int textureId, int width, int height) {
        allocateTextureImpl(textureId, 0, width, height);
    }

    public void allocateTextureImpl(int glTextureId, int mipmapLevels, int width, int height) {
        Object object = net.minecraft.client.renderer.texture.TextureUtil.class;

        synchronized (object) {
            deleteTexture(glTextureId);
            bindTexture(glTextureId);
        }

        if (mipmapLevels >= 0) {
            GL11.glTexParameteri(3553, 33085, mipmapLevels);
            GL11.glTexParameteri(3553, 33082, 0);
            GL11.glTexParameteri(3553, 33083, mipmapLevels);
            GL11.glTexParameterf(3553, 34049, 0.0F);
        }

        for (int i = 0; i <= mipmapLevels; ++i) {
            GL11.glTexImage2D(3553, i, 6408, width >> i, height >> i, 0, 32993, 33639, (IntBuffer) null);
        }
    }

    public int uploadTextureImageSub(int textureId, BufferedImage p_110995_1_, int p_110995_2_, int p_110995_3_, boolean p_110995_4_, boolean p_110995_5_) {
        bindTexture(textureId);
        uploadTextureImageSubImpl(p_110995_1_, p_110995_2_, p_110995_3_, p_110995_4_, p_110995_5_);
        return textureId;
    }

    private void uploadTextureImageSubImpl(BufferedImage p_110993_0_, int p_110993_1_, int p_110993_2_, boolean p_110993_3_, boolean p_110993_4_) {
        int i = p_110993_0_.getWidth();
        int j = p_110993_0_.getHeight();
        int k = 4194304 / i;
        int[] aint = dataArray;
        setTextureBlurred(p_110993_3_);
        setTextureClamped(p_110993_4_);

        for (int l = 0; l < i * j; l += i * k) {
            int i1 = l / i;
            int j1 = Math.min(k, j - i1);
            int k1 = i * j1;
            p_110993_0_.getRGB(0, i1, i, j1, aint, 0, i);
            copyToBuffer(aint, k1);
            GL11.glTexSubImage2D(3553, 0, p_110993_1_, p_110993_2_ + i1, i, j1, 32993, 33639, DATA_BUFFER);
        }
    }

    public void setTextureClamped(boolean p_110997_0_) {
        if (p_110997_0_) {
            GL11.glTexParameteri(3553, 10242, 33071);
            GL11.glTexParameteri(3553, 10243, 33071);
        } else {
            GL11.glTexParameteri(3553, 10242, 10497);
            GL11.glTexParameteri(3553, 10243, 10497);
        }
    }

    private void setTextureBlurred(boolean p_147951_0_) {
        setTextureBlurMipmap(p_147951_0_, false);
    }

    public void setTextureBlurMipmap(boolean p_147954_0_, boolean p_147954_1_) {
        if (p_147954_0_) {
            GL11.glTexParameteri(3553, 10241, p_147954_1_ ? 9987 : 9729);
            GL11.glTexParameteri(3553, 10240, 9729);
        } else {
            int i = Config.getMipmapType();
            GL11.glTexParameteri(3553, 10241, p_147954_1_ ? i : 9728);
            GL11.glTexParameteri(3553, 10240, 9728);
        }
    }

    private void copyToBuffer(int[] p_110990_0_, int p_110990_1_) {
        copyToBufferPos(p_110990_0_, p_110990_1_);
    }

    private void copyToBufferPos(int[] p_110994_0_, int p_110994_2_) {

        DATA_BUFFER.clear();
        DATA_BUFFER.put(p_110994_0_, 0, p_110994_2_);
        DATA_BUFFER.position(0).limit(p_110994_2_);
    }

    public void bindTexture(int p_94277_0_) {
        GlStateManager.bindTexture(p_94277_0_);
    }


    static {

        int[] aint = new int[]{-524040, -524040, -524040, -524040, -524040, -524040, -524040, -524040};
        int[] aint1 = new int[]{-16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216};
        int k = aint.length;

        for (int l = 0; l < 16; ++l) {
            System.arraycopy(l < k ? aint : aint1, 0, MISSING_TEXTURE_DATA, 16 * l, k);
            System.arraycopy(l < k ? aint1 : aint, 0, MISSING_TEXTURE_DATA, 16 * l + k, k);
        }

        MISSING_TEXTURE.updateDynamicTexture();
        COLOR_GAMMAS = new float[256];

        for (int i1 = 0; i1 < COLOR_GAMMAS.length; ++i1) {
            COLOR_GAMMAS[i1] = (float) Math.pow((double) ((float) i1 / 255.0F), 2.2D);
        }
    }
}


