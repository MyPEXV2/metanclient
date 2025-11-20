package relake.module.implement.render;

import relake.event.EventHandler;
import relake.event.impl.render.Render3DEvent;
import relake.event.impl.render.RenderPre2DEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.render.display.bqrender.BloomShader;
import relake.render.display.bqrender.Shaders;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.ColorSetting;
import relake.settings.implement.FloatSetting;

import java.awt.*;

public class ShaderHandsModule extends Module {
    public static final Setting<Float> chromaticity = new FloatSetting("Насышенность цвета").range(1f, 100f, 0.5F).setValue(1.0f);
    public static final Setting<Boolean> blur = new BooleanSetting("Размытость").setValue(true);
    public static final Setting<Boolean> reflect = new BooleanSetting("Переотражать").setValue(true);
    public static final Setting<Boolean> bloom = new BooleanSetting("Свечение").setValue(false);
    public static final Setting<Float> iterations = new FloatSetting("Кол-во итераций").range(1f, 6, 1f).setValue(4f).setVisible(bloom::getValue);
    public static final Setting<Float> offset = new FloatSetting("Размер свечения").range(1f, 6, 0.1f).setValue(4f).setVisible(bloom::getValue);
    private final Setting<Color> pickColor = new ColorSetting("Пикер цвета").setValue(Color.CYAN).setVisible(bloom::getValue);

    public final BloomShader BLOOM_SHADER = new BloomShader();

    public ShaderHandsModule() {
        super("Shader Hands", "Добавляет эффект размытого стекла в рендер рук и предметов от первого лица", "Adds a blurred glass effect to the first-person hand and items rendering", ModuleCategory.Render);
        registerComponent(chromaticity, blur, reflect, bloom, iterations, offset, pickColor);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
    }

    @EventHandler
    public void onEvent(RenderPre2DEvent event) {
        if (!bloom.getValue()) return;
        BLOOM_SHADER.draw(1 + iterations.getValue().intValue(), offset.getValue(), BloomShader.RenderType.DISPLAY, pickColor.getValue().getRGB());
    }

    @EventHandler
    public void onEvent(Render3DEvent.PostWorld event) {
        if (!bloom.getValue()) return;
        BLOOM_SHADER.addTask3D(() -> mc.gameRenderer.renderHand(event.getMatrix(), event.getActiveRenderInfo(), event.getPartialTicks(), true, true, false));
        BLOOM_SHADER.draw(1 + iterations.getValue().intValue(), offset.getValue(), BloomShader.RenderType.CAMERA, pickColor.getValue().getRGB());
    }

    @EventHandler
    public void onPreHand(Render3DEvent.PreHand event) {
        Shaders stencil = Shaders.stencilShader;
        stencil.load();
        stencil.setUniformi("originalTexture", 0);
        stencil.setUniformi("replaceTexture", 5);
        stencil.setUniformf("multiplier", 1f, 1f, 1f, 1f);
        stencil.setUniformf("viewOffset", mw.getScaledWidth() / (reflect.getValue() ? 2.F : 1.F), mw.getScaledHeight() / (reflect.getValue() ? 2.F : 1.F));
        stencil.setUniformf("resolution", mw.getScaledWidth(), mw.getScaledHeight());
    }

    @EventHandler
    public void onPostHand(Render3DEvent.PostHand event) {
        Shaders stencil = Shaders.stencilShader;
        stencil.unload();
    }

    private void reset() {
        BLOOM_SHADER.cameraBlurQueue.clear();
        BLOOM_SHADER.displayBlurQueue.clear();
        BLOOM_SHADER.reset();
    }

}
