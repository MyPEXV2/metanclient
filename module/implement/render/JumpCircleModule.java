package relake.module.implement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import relake.Client;
import relake.animation.tenacity.Animation;
import relake.animation.tenacity.Direction;
import relake.common.util.ColorUtil;
import relake.event.EventHandler;
import relake.event.impl.player.JumpEvent;
import relake.event.impl.render.WorldRenderEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.render.display.Render2D;
import relake.render.display.shape.ShapeRenderer;
import relake.settings.Setting;
import relake.settings.implement.FloatSetting;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class JumpCircleModule extends Module {

    public static final Setting<Float> time = new FloatSetting("Время существования").range(300.0F, 2250.0F, 10).setValue(800.0F);

    public static final Setting<Float> sizeCircle = new FloatSetting("Размер").range(50F, 250F, 10F).setValue(50F);
    public static final Setting<Float> strength = new FloatSetting("Сила центрального смещения").range(0F, 5F, 1.0f).setValue(0F);

    public List<Circle> circles = new ArrayList<>();

    public JumpCircleModule() {
        super("Jump Circle", "Добавляет красивые круги под ногами от прыжков", "Adds beautiful circles under your feet from jumping", ModuleCategory.Render);
        registerComponent(time);
        registerComponent(sizeCircle);
        registerComponent(strength);
    }

    @EventHandler
    public void onJump(JumpEvent event) {
        if (mc.player == null) return;
        circles.add(new Circle(mc.player.getPositionVec()));
    }

    @EventHandler
    public void worldRender(WorldRenderEvent worldRenderEvent) {
        for (Circle circle : circles) {
            circle.render(worldRenderEvent.getStack());
        }

        circles.removeIf(circle -> circle.getAnimation().isDone(Direction.BACKWARD));
    }

    @Getter
    @RequiredArgsConstructor
    public static class Circle {
        private final Animation animation = new Animation(255, Duration.ofMillis(time.getValue().longValue()));
        private final Vector3d pos;

        public void render(MatrixStack stack) {
            double x = pos.x - mc.getRenderManager().renderPosX(),
                    y = pos.y - mc.getRenderManager().renderPosY() + 0.15f,
                    z = pos.z - mc.getRenderManager().renderPosZ();


            int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

            int[] color = {
                    ColorUtil.applyOpacity(ColorUtil.darker(rgb, 35), animation.get()),
                    ColorUtil.applyOpacity(ColorUtil.darker(rgb, 30), animation.get()),
                    ColorUtil.applyOpacity(ColorUtil.darker(rgb, 135), animation.get()),
                    ColorUtil.applyOpacity(ColorUtil.darker(rgb, 125), animation.get())
            };

            float size = (sizeCircle.getValue() / 10000) + (animation.get() / 255) / 50;

            stack.push();
            stack.translate(x, y, z);
            stack.scale(size, size, size);
            stack.rotate(new Quaternion(new Vector3f(1, 0, 0), 90, true));

            long cicleTime = 1700L;
            float degRotate = (System.currentTimeMillis() % cicleTime) / (float) cicleTime * 360.F;

            stack.rotate(Vector3f.ZP.rotationDegrees(degRotate));

            ShapeRenderer box = Render2D.box(stack, -25, -25, 50 + strength.getValue(), 50 + strength.getValue());
            box.texture(new ResourceLocation("relake/circle.png"), color);

            stack.rotate(Vector3f.ZN.rotationDegrees(degRotate));
            stack.pop();

            if (animation.isDone(Direction.FORWARD)) {
                animation.switchDirection(Direction.BACKWARD);
            }
        }
    }
}
