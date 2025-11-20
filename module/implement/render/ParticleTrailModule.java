package relake.module.implement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import relake.Client;
import relake.animation.tenacity.Animation;
import relake.animation.tenacity.Direction;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.common.util.MoveUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
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

public class ParticleTrailModule extends Module {
    private final List<Particle> particles = new ArrayList<>();

    public final Setting<Boolean> fitstface = new BooleanSetting("От первого лица").setValue(true);

    public final Setting<Float> size = new FloatSetting("Размер")
            .range(50F, 75F, 1f)
            .setValue(50F);

    public final Setting<Float> timerForParticles = new FloatSetting("Время существования")
            .range(350F, 1500F, 10f)
            .setValue(500F);


    public static final Setting<Float> gravity = new FloatSetting("Сила гравитации")
            .range(-15F, 20F, 1f)
            .setValue(5F);

    public final Setting<Float> count = new FloatSetting("Количество")
            .range(1, 5f, 1f)
            .setValue(5F);


    public static final Setting<Float> radius = new FloatSetting("Радиус")
            .range(-0F, 1.5F, 0.5f)
            .setValue(0F);


    private static final Setting<Boolean> colission = new BooleanSetting("Коллизия").setValue(true);
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

    public ParticleTrailModule() {
        super("Particle Trail", "Рендерит красивые частицы сзади вас при ходьбе", "Renders beautiful particles from behind you as you walk", ModuleCategory.Render);
        registerComponent(
                fitstface, selectComponent, size, timerForParticles, gravity, count, radius, colission, scale
        );
    }

    @EventHandler
    public void tick(TickEvent event) {
        //if (particles.size() >= maxCount.getValue()) {
        //    return;
        //}

        if (!fitstface.getValue()) {
            if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
                return;
            }

        }

        float w = mc.player.getWidth() / 2f;
        int spawnedParticles = 0;
        for (int i = 0; i < count.getValue(); i++) {
            Vector3d positionVec = mc.player.getPositionVec().add(MathUtil.random(-w, w), MathUtil.random(0.25f, mc.player.getHeight()), MathUtil.random(-w, w));
            if (MoveUtil.isMoving()) {
                BlockPos blockPos = new BlockPos(positionVec);
                if (mc.world.isAirBlock(blockPos)) {

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
                    particles.add(new Particle(positionVec, name));
                    spawnedParticles++;
                }
            }
        }
    }

    @EventHandler
    public void worldRender(WorldRenderEvent worldRenderEvent) {
        for (Particle particle : particles) {
            particle.render(worldRenderEvent.getStack());
        }

        particles.removeIf(particle -> particle.getAnimation().isDone(Direction.BACKWARD));
    }

    private boolean isInPlayerView(Vector3d pos) {
        Vector3d playerViewVec = mc.player.getLookVec();
        Vector3d playerToParticle = pos.subtract(mc.player.getPositionVec()).normalize();

        return playerViewVec.dotProduct(playerToParticle) > 0.1;
    }

    @Getter
    public static class Particle {
        private final Animation animation;
        private Vector3d pos;
        private Vector3d motion = getRandomMotion();
        private final double rotate = MathUtil.random(0, 180);
        private final long currentTimeMillis = System.currentTimeMillis();
        private final String type;

        public Particle(Vector3d pos, String type) {
            this.pos = pos;
            float timerValue = Client.instance.moduleManager.particleTrailModule.timerForParticles.getValue();
            this.animation = new Animation(255, Duration.ofMillis((long) timerValue));
            this.type = type;
        }

        private Vector3d getRandomMotion() {
            return new Vector3d(
                    MathUtil.random(-radius.getValue() - 0.25f, radius.getValue() + 0.25f),
                    MathUtil.random(-radius.getValue() / 2, radius.getValue() / 4),
                    MathUtil.random(-radius.getValue() - 0.25f, radius.getValue() + 0.25f)
            );
        }

        public void update() {
            float speed = (float) ((Minecraft.debugFPS > 0 ? (1.0000 / Minecraft.debugFPS) : 1) / 0.05F);

            float motionMultiplier = 0.05f;

            pos = pos.add(motion.mul(speed * motionMultiplier, speed * motionMultiplier, speed * motionMultiplier));
            motion = motion.add(
                    MathUtil.deltaTime() * motionMultiplier,
                    (ParticleTrailModule.gravity.getValue()) * MathUtil.deltaTime() * motionMultiplier,
                    MathUtil.deltaTime() * motionMultiplier);
            AxisAlignedBB alignedBB = new AxisAlignedBB(
                    pos.add(-0.5, -0.5, -0.5),
                    pos.add(0.5, 0.5, 0.5)
            );
            if (colission.getValue()) {
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
                    Vector3d reflection = motion.subtract(normal.mul(2 * dotProduct, 2 * dotProduct, 2 * dotProduct));

                    motion = reflection.mul(0.8, 0.8, 0.8);
                }
            }
        }

        public void render(MatrixStack stack) {
            update();

            double x = pos.x - mc.getRenderManager().renderPosX(),
                    y = pos.y - mc.getRenderManager().renderPosY(),
                    z = pos.z - mc.getRenderManager().renderPosZ();

            ParticleTrailModule particlesModule = Client.instance.moduleManager.particleTrailModule;
            float sizeValue = particlesModule.size.getValue();

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

            ShapeRenderer box = Render2D.box(stack, -sizeValue, -sizeValue, sizeValue, sizeValue);
            box.texture(new ResourceLocation("relake/" + type), ColorUtil.applyOpacity(rgb, animation.get()));

            stack.pop();

            if (animation.isDone(Direction.FORWARD)) {
                animation.switchDirection(Direction.BACKWARD);
            }
        }
    }
}
