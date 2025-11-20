package relake.common.util;

import lombok.AccessLevel;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.texture.DownloadingTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.entity.LivingEntity;
import relake.common.InstanceAccess;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static net.optifine.util.TextureUtils.readBufferedImage;
import static org.lwjgl.opengl.GL11.glGenTextures;

@UtilityClass
public class SkinUtil implements InstanceAccess {
    private static final HashMap<String, SkinData> cache = new HashMap<>();

    public SkinData getSkin(LivingEntity entity) {
        if (entity instanceof AbstractClientPlayerEntity clientPlayer) {
            SkinData data = cache.computeIfAbsent(clientPlayer.getGameProfile().getName(), name -> {
                SkinData newData = new SkinData();
                BufferedImage image = parseBufferedImage(mc.getTextureManager().getTexture(clientPlayer.getLocationSkin()));

                if (image != null) {
                    newData.setBufferedImage(image);
                    newData.setLoaded(true);
                }

                return newData;
            });
            return data.isLoaded() ? data : null;
        }

        return null;
    }

    @SneakyThrows
    private BufferedImage parseBufferedImage(Texture texture) {
        if (texture instanceof DownloadingTexture downloadingTexture && downloadingTexture.getNativeImage() != null) {
            downloadingTexture.loadTexture(mc.getResourceManager());

            return getBufferedImage(downloadingTexture);
        } else if (texture instanceof SimpleTexture simpleTexture) {
            return readBufferedImage(mc.getResourceManager()
                    .getResource(simpleTexture.textureLocation)
                    .getInputStream()
            );
        }

        return null;
    }

    @SneakyThrows
    private BufferedImage getBufferedImage(DownloadingTexture downloadingTexture)  {
        NativeImage nativeImage = downloadingTexture.getTextureData(mc.getResourceManager()).getNativeImage();

        int width = nativeImage.getWidth(),
                height = nativeImage.getHeight();

        BufferedImage bufferedImage = new BufferedImage(width, height, TYPE_INT_ARGB);

        nativeImage.downloadFromTexture(0, true);
        bufferedImage.setRGB(0, 0, width, height, nativeImage.makePixelArray(), 0, width);

        return bufferedImage;
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SkinData {
        int texture;
        boolean loaded;

        public void setBufferedImage(BufferedImage bufferedImage) {
            this.texture = glGenTextures();
            TextureUtil.uploadTextureImageAllocate(getTexture(), bufferedImage.getSubimage(8, 8, 8, 8), false, true);
        }
    }
}

