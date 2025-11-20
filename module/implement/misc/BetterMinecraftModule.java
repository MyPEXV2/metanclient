package relake.module.implement.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.optifine.Config;
import relake.event.EventHandler;
import relake.event.impl.misc.MouseScrollEvent;
import relake.event.impl.render.RenderPre2DEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;

public class BetterMinecraftModule extends Module {
    public final Setting<Boolean> perspectiveAnimation = new BooleanSetting("Анимация при смене перспективы")
            .setValue(true);

    public final Setting<Boolean> inventoryAnimation = new BooleanSetting("Анимация при открытие инвентаря")
            .setValue(true);

    public final Setting<Boolean> chatAnimation = new BooleanSetting("Анимация на сообщения чата")
            .setValue(false);

    public final Setting<Boolean> tabAnimation = new BooleanSetting("Анимация на список игроков")
            .setValue(false);

    public final Setting<Boolean> scopeAnimation = new BooleanSetting("Анимация на приближение камеры")
            .setValue(false);

    public final Setting<Boolean> zoomWithMouseWheel = new BooleanSetting("Приближение камеры колёсиком мыши")
            .setValue(false);

    public final Setting<Boolean> offSmoothInZoom = new BooleanSetting("Отключить замедление при приближение камеры")
            .setValue(false);

    public final Setting<Boolean> modelGapFix = new BooleanSetting("Исправить зазоры в моделях")
            .setValue(false)
            .onAction(() -> {
                if (Minecraft.getInstance().getResourceManager() != null && isEnabled())
                    Minecraft.getInstance().reloadResources();
            });

    public final Setting<Boolean> addDropItemsButton = new BooleanSetting("Кнопка дропа в инве").setValue(true);

    public BetterMinecraftModule() {
        super("Better Minecraft", "Улучшает некоторые визуальные элементы в игре", "Improves some visual elements in the game", ModuleCategory.Misc);
        registerComponent(perspectiveAnimation, inventoryAnimation, chatAnimation, tabAnimation, scopeAnimation, zoomWithMouseWheel, offSmoothInZoom, modelGapFix, addDropItemsButton);
    }

    private float zoom = 0;
    private float smoothedZoom = 0;

    @Override
    public void onEnable() {
        super.onEnable();
        if (modelGapFix.getValue()) {
            Minecraft.getInstance().reloadResources();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (modelGapFix.getValue()) {
            Minecraft.getInstance().reloadResources();
        }
    }

    @EventHandler
    public void onRender(RenderPre2DEvent event) {
        this.smoothedZoom = MathHelper.lerp(mc.getRenderPartialTicks() * 0.1F, this.smoothedZoom, this.zoom);
    }

    @EventHandler
    public void onMouseScroll(MouseScrollEvent event) {
        this.zoom = MathHelper.clamp(this.zoom + (float) event.getScrollDelta() * 2F, -2F, 14F);

        event.setCancelled(Config.zoomMode);
    }

    public float getSmoothedZoom(boolean finished) {
        if (finished) {
            this.zoom = 0;
            this.smoothedZoom = 0;
        }

        return smoothedZoom;
    }
}