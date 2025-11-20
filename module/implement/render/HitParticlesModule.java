package relake.module.implement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;

import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import relake.Client;
import relake.animation.tenacity.Animation;
import relake.animation.tenacity.Direction;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.event.EventHandler;
import relake.event.impl.player.AttackEvent;
import relake.event.impl.render.WorldRenderEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.render.display.Render2D;
import relake.render.display.shape.ShapeRenderer;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.FloatSetting;
import relake.settings.implement.SelectSetting;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class HitParticlesModule extends Module {
    private final List<Particle> particles = new ArrayList<>();
    public final Setting<Float> count = new FloatSetting("Количество")
            .range(1F, 10F, 1f)
            .setValue(3F);
    public static final Setting<Float> size = new FloatSetting("Размер")
            .range(50F, 100F, 1f)
            .setValue(50F);
    public static final Setting<Float> time = new FloatSetting("Время существования")
            .range(350f, 2000f, 25f)
            .setValue(1500F);

    private static final Setting<Boolean> scale = new BooleanSetting("Скейл").setValue(true);

    private final SelectSetting selectComponent = new SelectSetting("Отображать")
            .setValue("Отображать всё",
                    "Point",
                    "Star",
                    "Lightning",
                    "Cross",
                    "Crown",
                    "Heart",
                    "Line",
                    "Rhombus",
                    "Dollar",
                    "Snowflake",
                    "Triangle");

    private final List<String> strings = Arrays.asList(
            "point", "star", "lightning", "cross", "crown",
            "heart", "line", "rhombus", "dollar", "snowflake", "triangle"
    );

    public HitParticlesModule() {
        super("Hit Particle", "Добавляет красивые частицы от ударов", "Adds beautiful particles from impacts", ModuleCategory.Render);
        registerComponent(
                selectComponent, count, size, time, scale
        );
    }

    @EventHandler
    public void attack(AttackEvent attackEvent) {
        if (attackEvent.getEntity() instanceof PlayerEntity player) {
            for (int i = 0; i < count.getValue(); i++) {
                Vector3d pos = player.getPositionVec().add(0, player.getWidth(), 0);
                BlockPos blockPos = new BlockPos(pos);

                if (mc.world.isAirBlock(blockPos) && isInPlayerView(pos)) {
                    String name;
                    if (selectComponent.isSelected("Point")) {
                        name = strings.get(0);
                    } else if (selectComponent.isSelected("Star")) {
                        name = strings.get(1);
                    } else if (selectComponent.isSelected("Lightning")) {
                        name = strings.get(2);
                    } else if (selectComponent.isSelected("Cross")) {
                        name = strings.get(3);
                    } else if (selectComponent.isSelected("Crown")) {
                        name = strings.get(4);
                    } else if (selectComponent.isSelected("Heart")) {
                        name = strings.get(5);
                    } else if (selectComponent.isSelected("Line")) {
                        name = strings.get(6);
                    } else if (selectComponent.isSelected("Rhombus")) {
                        name = strings.get(7);
                    } else if (selectComponent.isSelected("Dollar")) {
                        name = strings.get(8);
                    } else if (selectComponent.isSelected("Snowflake")) {
                        name = strings.get(9);
                    } else if (selectComponent.isSelected("Triangle")) {
                        name = strings.get(10);
                    } else if (selectComponent.isSelected("Отображать всё")) {
                        name = strings.get(new Random().nextInt(strings.size()));
                    } else {
                        name = strings.get(0);
                    }
                    name += ".png";
                    particles.add(new Particle(pos, name));
                }
            }
        }
    }

    @EventHandler
    public void worldRender(WorldRenderEvent worldRenderEvent) {
        for (Particle particle : particles) {
            particle.render(worldRenderEvent.getStack());
        }

        particles.removeIf(circle -> circle.getAnimation().isDone(Direction.BACKWARD));
    }

    private boolean isInPlayerView(Vector3d pos) {
        Vector3d playerViewVec = mc.player.getLookVec();
        Vector3d playerToParticle = pos.subtract(mc.player.getPositionVec()).normalize();

        return playerViewVec.dotProduct(playerToParticle) > 0.1;
    }

    @Getter
    public static class Particle {
        private final Animation animation = new Animation(255, Duration.ofMillis(time.getValue().longValue()));
        private Vector3d pos;
        private Vector3d motion = getRandomMotion();
        private final double rotate = MathUtil.random(0, 180);
        private final String type;

        public Particle(Vector3d pos, String type) {
            this.pos = pos;
            this.type = type;
        }

        private Vector3d getRandomMotion() {
            return new Vector3d(
                    MathUtil.random(-0.1f, 0.1f),
                    MathUtil.random(0.001f, 0.2f),
                    MathUtil.random(-0.1f, 0.1f)
            );
        }

        public void update() {

            float speed = (float) ((Minecraft.debugFPS > 0 ? (1.0000 / Minecraft.debugFPS) : 1) / 0.05F);

            pos = pos.add(motion.mul(speed, speed, speed));
            motion = motion.add(0, -0.0002 * 2, 0);

            if (!mc.world.isAirBlock(new BlockPos(pos)) &&
                    mc.world.getBlockState(new BlockPos(pos)).getBlock() != Blocks.TALL_GRASS &&
                    mc.world.getBlockState(new BlockPos(pos)).getBlock() != Blocks.GRASS &&
                    mc.world.getBlockState(new BlockPos(pos)).getBlock() != Blocks.FLOWER_POT &&
                    !(mc.world.getBlockState(new BlockPos(pos)).getBlock() instanceof BushBlock) &&
                    mc.world.getBlockState(new BlockPos(pos)).getBlock() != Blocks.FERN &&
                    mc.world.getBlockState(new BlockPos(pos)).getBlock() != Blocks.PEONY &&
                    mc.world.getBlockState(new BlockPos(pos)).getBlock() != Blocks.DEAD_BUSH &&
                    !(mc.world.getBlockState(new BlockPos(pos)).getBlock() instanceof FlowerBlock)) {
                Vector3d normal = new Vector3d(0, 1, 0);

                double dotProduct = motion.dot(normal);
                Vector3d reflection = motion.subtract(normal.mul(8 * dotProduct, 2.1 * dotProduct, 16 * dotProduct));

                motion = reflection.mul(0.8, 0.8, 0.8);
            }
        }


        public void render(MatrixStack stack) {
            update();

            double x = pos.x - mc.getRenderManager().renderPosX(),
                    y = pos.y - mc.getRenderManager().renderPosY() + 0.2f,
                    z = pos.z - mc.getRenderManager().renderPosZ();

            int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

            float scaleFactor = 0.006F;

            stack.push();
            stack.translate(x, y, z);
            stack.scale(scaleFactor, scaleFactor, scaleFactor);

            if (scale.getValue()) {
                float animationValue = (animation.get() / 255.f);
                stack.scale(animationValue, animationValue, animationValue);
            }
            stack.rotate(mc.getRenderManager().getCameraOrientation());
            stack.rotate(new Quaternion(new Vector3f(0, 0, 1), (float) rotate, false));

            ShapeRenderer box = Render2D.box(stack, -size.getValue(), -size.getValue(), size.getValue(), size.getValue());
            box.texture(new ResourceLocation("relake/" + type), ColorUtil.applyOpacity(rgb, animation.get()));

            stack.pop();

            if (animation.isDone(Direction.FORWARD)) {
                animation.switchDirection(Direction.BACKWARD);
            }
        }
    }
}