package relake.draggable.implement;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.Item;
import net.minecraft.util.StringUtils;
import relake.Client;
import relake.animation.tenacity.Animation;
import relake.animation.tenacity.Direction;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.draggable.Draggable;
import relake.event.EventHandler;
import relake.event.impl.player.ItemCooldownStateEvent;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.font.TextRenderer;
import relake.render.display.shape.ShapeRenderer;


import java.time.Duration;
import java.util.*;

public class CooldownsDraggable extends Draggable {
    private final List<CooldownWrapper> cooldowns = new ArrayList<>();

    public CooldownsDraggable() {
        super("Cooldowns", 100, 100, 125, 55);
        Client.instance.eventManager.register(this);
    }

    @Override
    public boolean visible() {
        if (!Client.instance.moduleManager.hudModule.selectComponent.isSelected("Cooldowns") || !Client.instance.moduleManager.hudModule.isEnabled()) {
            return false;
        }
        return !this.cooldowns.isEmpty() || mc.currentScreen instanceof ChatScreen;
    }

    @Override
    public void tick() {
        Iterator<CooldownWrapper> iterator = this.cooldowns.iterator();
        while (iterator.hasNext()) {
            CooldownWrapper cooldownWrapper = iterator.next();
            Animation cooldownAnimation = cooldownWrapper.getAnimation();

            if (cooldownAnimation.isDone(Direction.BACKWARD)) {
                iterator.remove();
            }
        }

        float maxCooldownWidth = (float) this.cooldowns.stream()
                .mapToDouble(cooldown -> Render2D.size(FontRegister.Type.BOLD, 12).getWidth(cooldown.getDisplayName() + cooldown.getKeyText()) + 15)
                .max()
                .orElse(0) + 15;

        this.animatedWidth = Math.max(this.defaultWidth, maxCooldownWidth);
        this.width = this.animWidth.get();
    }
    @Override
    public void update() {}
    @Override
    public void render(MatrixStack matrixStack, float partialTicks) {
        float x = this.x;
        float y = this.y;
        float yOffset = y + 35;
        float backgroundHeight = 10;
        float scaleFactor = (float) mc.getMainWindow().getGuiScaleFactor();
        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();
        int round = 10;
        ShapeRenderer box = Render2D.box(matrixStack, x, y, width, Math.max(60, height));

        box.quad(round, 0xB70E0E0F);
        box.quad(round, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f));

        Render2D.size(FontRegister.Type.BOLD, 14).string(matrixStack, name, x + 5, y + 5, ColorUtil.applyOpacity(ColorUtil.getColor(235, 235, 235), 255));
        Render2D.size(FontRegister.Type.ICONS, 20).string(matrixStack, "n", x + width - 10 - Render2D.size(FontRegister.Type.ICONS, 20).getWidth("g"), y + 4, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.35f));

        ShapeRenderer box1 = Render2D.box(matrixStack, x, y + 28.f, width, Math.max(60, height) - 29);
        box1.corner(new float[]{0, round, 0, round}, 0x590E0E0F);

        ShapeRenderer boxOutLine = Render2D.box(matrixStack, x - 2, y - 2, width + 4, Math.max(60, height) + 4);
        boxOutLine.outlineHud(round, 2, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f));

        int cooldownCount = 0;
        float lastCooldownAnimation = 0;

        for (CooldownWrapper cooldown : this.cooldowns) {
            Animation cooldownAnimation = cooldown.getAnimation();
            cooldownAnimation.switchDirection(cooldown.isActive());

            float anim = lastCooldownAnimation = cooldownAnimation.get();
            if (anim <= 0) {
                continue;
            }

            String displayName = cooldown.getDisplayName();
            String keyText = cooldown.getKeyText();

            cooldownCount++;
            matrixStack.push();
            matrixStack.translate(0, yOffset / scaleFactor, 0);
            matrixStack.scale(1, anim, 1);

            TextRenderer size = Render2D.size(FontRegister.Type.BOLD, 12);

            size.string(
                    matrixStack,
                    displayName,
                    x + 5, 0,
                    ColorUtil.applyOpacity(ColorUtil.getColor(200, 200, 200), anim * 255)
            );

            size.string(
                    matrixStack, keyText, x + this.width - size.getWidth(keyText) - 10, 0,
                    ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, anim * 25), -1, .4f
            ));

            yOffset += 20 * anim;
            backgroundHeight += 20 * anim;
            matrixStack.pop();
        }

        if (cooldownCount <= 1) {
            float stringPC = cooldownCount == 0 ? 1.F : 1.F - lastCooldownAnimation;
            String empty = MathUtil.getStringPercent("Список пуст.", stringPC);
            matrixStack.push();
            matrixStack.translate(0, (yOffset + 6) / scaleFactor, 0);
            matrixStack.scale(1, stringPC, 1);
            matrixStack.translate(0, -(yOffset + 6) / scaleFactor, 0);
            Render2D.size(FontRegister.Type.BOLD, 12).string(
                    matrixStack,
                    empty,
                    x + width / 2 - Render2D.size(FontRegister.Type.BOLD, 12).getWidth(empty) / 2 - 10,
                    y + 35,
                    ColorUtil.applyOpacity(ColorUtil.getColor(200, 200, 200), 215.F * stringPC)
            );
            matrixStack.pop();

            height = 60;
            return;
        }

        height = backgroundHeight + 30;
    }

    @EventHandler
    public void onItemCooldownState(ItemCooldownStateEvent event) {
        Item item = event.getItem();
        ItemCooldownStateEvent.State state = event.getState();
        if (state != ItemCooldownStateEvent.State.ADD) {
            return;
        }
        synchronized (this.cooldowns) {
            CooldownWrapper cooldownWrapper = new CooldownWrapper(item);
            if (!this.cooldowns.contains(cooldownWrapper)) {
                this.cooldowns.add(cooldownWrapper);
            }
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class CooldownWrapper {
        private final Item item;
        private final Animation animation = new Animation(1, Duration.ofMillis(150))
                .setDirection(Direction.BACKWARD);

        public int resolveItemCooldown() {
            return mc.player.getCooldownTracker().getExpiredTicks(this.item);
        }

        public boolean isActive() {
            return this.resolveItemCooldown() > 0;
        }

        public String getDisplayName() {
            return this.getItem().getName().getString();
        }

        public String getKeyText() {
            return String.format("[%s]", StringUtils.ticksToElapsedTime(this.resolveItemCooldown()));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CooldownWrapper that = (CooldownWrapper) o;
            return Objects.equals(item, that.item);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(item);
        }
    }
}
