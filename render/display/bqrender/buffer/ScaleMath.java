package relake.render.display.bqrender.buffer;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import relake.common.InstanceAccess;

@UtilityClass
public class ScaleMath implements InstanceAccess {
    public void setupOverlayRendering(float scale) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        mw.setGuiScale(scale);
        RenderSystem.clear(256, Minecraft.IS_RUNNING_ON_MAC);
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, mw.getScaledWidth(), mw.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        RenderSystem.matrixMode(5888);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
        RenderHelper.setupGui3DDiffuseLighting();
    }

    public void setupOverlayRendering() {
        int guiScale = mw.calcGuiScale(mc.gameSettings.guiScale, mc.getForceUnicodeFont());
        setupOverlayRendering(guiScale);
    }

    public void resetProjectionMatrix() {
        RenderSystem.translatef(0.0F, 0.0F, 2000.0F);
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.matrixMode(5888);
        RenderSystem.loadIdentity();
    }
}
