package relake.draggable.implement;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.StringUtils;
import org.lwjgl.glfw.GLFW;
import relake.Client;
import relake.animation.tenacity.Direction;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.common.util.StringUtil;
import relake.draggable.Draggable;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;

import java.util.*;

public class PotionDraggable extends Draggable {
    private final Map<String, EffectInstance> effects = new TreeMap<>();

    public PotionDraggable() {
        super("Potion", 200, 100, 125, 30);
    }

    @Override
    public boolean visible() {
        return (!mc.player.getActivePotionEffects().isEmpty() || mc.currentScreen instanceof ChatScreen) && Client.instance.moduleManager.hudModule.isEnabled() && Client.instance.moduleManager.hudModule.selectComponent.isSelected("PotionList");
    }

    @Override
    public void update() {}
    @Override
    public void tick() {
        float maxCooldownWidth = (float) this.effects.entrySet().stream()
                .mapToDouble(entry -> {
                    String duration;
                    if (entry.getValue().getIsPotionDurationMax()) {
                        duration = "**:**";
                    } else {
                        duration = StringUtils.ticksToElapsedTime(entry.getValue().getDuration());
                    }
                    String potionName = MathUtil.getStringPercent(entry.getValue().getPotion().getDisplayName().getString(), Math.min(entry.getValue().animation.get() * 1.15F, 1.F));

                    String keyText = "[" + duration + "]";
                    return Render2D.size(FontRegister.Type.BOLD, 12).getWidth(potionName + keyText) + 32;
                })
                .max()
                .orElse(0) + 15;

        this.animatedWidth = Math.max(this.defaultWidth, maxCooldownWidth);
        this.width = this.animWidth.get();
    }

    @Override
    public void render(MatrixStack matrixStack, float partialTicks) {
        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();
        float effectHeight = 10;
        int round = 10;

        ShapeRenderer box = Render2D.box(matrixStack, x, y, width, Math.max(60, height));

        box.quad(round, 0xB70E0E0F);
        box.quad(round, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f));

        Render2D.size(FontRegister.Type.BOLD, 14).string(matrixStack, name, x + 5, y + 5, ColorUtil.applyOpacity(ColorUtil.getColor(235, 235, 235), 255));
        Render2D.size(FontRegister.Type.ICONS, 20).string(matrixStack, "f", x + width - 5 - Render2D.size(FontRegister.Type.ICONS, 20).getWidth("f"), y + 4, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.35f));

        ShapeRenderer box1 = Render2D.box(matrixStack, x, y + 28.f, width, Math.max(60, height) - 29);
        box1.corner(new float[]{0, round, 0, round}, 0x590E0E0F);

        ShapeRenderer boxOutLine = Render2D.box(matrixStack, x - 2, y - 2, width +4, Math.max(60, height) + 4);
        boxOutLine.outlineHud(round, 2, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f));

        mc.player.getActivePotionEffects()
                .stream()
                .filter(effect -> {
                    effect.animation.switchDirection(effect.getDuration() > 0);
                    return !effect.animation.isDone(Direction.BACKWARD);
                }).count();


        float yOffset = y + 35;
        double factor = mc.getMainWindow().getGuiScaleFactor();
        EffectInstance toRemove = null;

        List<EffectInstance> effects = new ArrayList<>(this.effects.values());
        List<EffectInstance> original = mc.player.getActivePotionEffects().stream().sorted(Comparator.comparing(EffectInstance::getDuration)).toList();

        for (EffectInstance eff : original) {
            if (eff == null || eff.getRealName() == null) continue;
            if (!this.effects.containsKey(eff.getRealName())) {
                this.effects.put(eff.getRealName(), eff);
            } else {
                this.effects.replace(eff.getRealName(), eff);
            }
        }
        int hasPotionsCount = 0;
        float lastPotionAnimation = 0;
        boolean wasAllToRemove = true;
        for (EffectInstance effectInstance : effects) {
            ++hasPotionsCount;
            lastPotionAnimation = effectInstance.animation.get();
            if (effectInstance.animation.getDirection() == Direction.FORWARD) wasAllToRemove = false;
            String duration;
            if (effectInstance.getIsPotionDurationMax()) {
                duration = "**:**";
            } else {
                duration = StringUtils.ticksToElapsedTime(effectInstance.getDuration());
            }
            String keyText = "[" + duration + "]";
            TextureAtlasSprite textureatlassprite = Minecraft.getInstance().getPotionSpriteUploader().getSprite(effectInstance.getPotion());
            matrixStack.push();
            matrixStack.translate(0, yOffset / factor, 0);
            matrixStack.scale(1,effectInstance.animation.get(),1);

            matrixStack.push();
            Minecraft.getInstance().getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, effectInstance.animation.get());
            AbstractGui.blit(matrixStack, (int) ((x + 10) / factor), (int) ((0 + 2) / factor), 0, (int) (12 / factor), (int) (12 / factor), textureatlassprite);
            RenderSystem.disableBlend();
            matrixStack.pop();

            String potionName = MathUtil.getStringPercent(effectInstance.getPotion().getDisplayName().getString(), Math.min(effectInstance.animation.get() * 1.15F, 1.F));
            Render2D.size(FontRegister.Type.BOLD, 12).string(matrixStack, potionName, x + 25, 0, ColorUtil.applyOpacity(ColorUtil.getColor(200, 200, 200), effectInstance.animation.get() * 255));

            float durationTextWidth = Render2D.size(FontRegister.Type.BOLD, 12).getWidth("[" + duration + "]");
            float maxTextX = x + width - 10;
            float durationTextX = x + width;

            if (durationTextX + durationTextWidth > maxTextX) {
                durationTextX = maxTextX - durationTextWidth;
            }

            Render2D.size(FontRegister.Type.BOLD, 12).string(
                    matrixStack, keyText, durationTextX - 1.5f, -1,
                    ColorUtil.applyOpacity(ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 95), -1, .2f), effectInstance.animation.get() * 255), (int) (x + width - 10)
            );
            matrixStack.pop();

            yOffset += 20 * effectInstance.animation.get();
            effectHeight += 20 * effectInstance.animation.get();

            if (!effectInstance.cleared && !original.contains(effectInstance)) {
                effectInstance.animation.switchDirection(false);
                effectInstance.cleared = true;
            }

            if (effectInstance.animation.isDone(Direction.BACKWARD)) toRemove = effectInstance;
        }

        if (wasAllToRemove || hasPotionsCount <= 1) {
            float stringPC = 1.F - lastPotionAnimation;
            String empty = MathUtil.getStringPercent("Список пуст.", stringPC);
            matrixStack.push();
            matrixStack.translate(0, (yOffset + 6) / factor, 0);
            matrixStack.scale(1, stringPC, 1);
            matrixStack.translate(0, -(yOffset + 6) / factor, 0);
            Render2D.size(FontRegister.Type.BOLD, 12).string(
                    matrixStack,
                    empty,
                    x + width / 2 - Render2D.size(FontRegister.Type.BOLD, 12).getWidth(empty) / 2 - 10,
                    y + 35,
                    ColorUtil.applyOpacity(ColorUtil.getColor(200, 200, 200), 215.F * stringPC)
            );
            matrixStack.pop();
            //semi bug-fix
            effectHeight += 20.F * stringPC;
            //
            height = 60;
        }

        if (toRemove != null) {
            this.effects.remove(toRemove.getRealName(), toRemove);
        }

        height = effectHeight + 30;
    }
}