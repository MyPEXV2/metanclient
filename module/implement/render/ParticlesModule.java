package relake.module.implement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.gen.Heightmap;
import relake.Client;
import relake.animation.tenacity.Animation;
import relake.animation.tenacity.Direction;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
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

public class ParticlesModule extends Module {
    private final List<Particle> particles = new ArrayList<>();

    public final Setting<Float> range = new FloatSetting("Радиус спавна")
            .range(10, 50, 1f)
            .setValue(50F);

    public final Setting<Float> rangeY = new FloatSetting("Высота спавна")
            .range(0.05f, 30, 0.05f)
            .setValue(50F);

    public final Setting<Float> gravity = new FloatSetting("Сила гравитации")
            .range(-10F, 10F, 1f)
            .setValue(0F);

    public final Setting<Float> motionPower = new FloatSetting("Сила движения")
            .range(0.1F, 2F, 0.1f)
            .setValue(1F);

    public final Setting<Float> incline = new FloatSetting("Наклон полёта по X")
            .range(-17.5F, 17.5F, 0.5f)
            .setValue(0F);

    public static final Setting<Float> incline2 = new FloatSetting("Наклон полёта по Z")
            .range(-17.5F, 17.5F, 0.5f)
            .setValue(17.5F);

    public final Setting<Float> count = new FloatSetting("Количество в секунду")
            .range(5F, 60F, 1F)
            .setValue(15F);


    public final Setting<Float> size = new FloatSetting("Размер")
            .range(100F, 200F, 10F)
            .setValue(150F);

    public final Setting<Float> timerForParticles = new FloatSetting("Время существования")
            .range(150F, 1500F, 10F)
            .setValue(800F);

    private static final Setting<Boolean> colission = new BooleanSetting("Коллизия").setValue(true);
    private static final Setting<Boolean> spawnFromGround = new BooleanSetting("Спавнить от земли").setValue(true);
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

    public ParticlesModule() {
        super("Particles", "Рендерит красивые частицы вокруг вас", "Renders beautiful particles around you", ModuleCategory.Render);
        registerComponent(
                selectComponent, range, rangeY, gravity, motionPower, incline, incline2, count,
                size, timerForParticles, colission, spawnFromGround, scale);
        selectComponent.setSelected("Отображать всё");
    }

    @EventHandler
    public void tick(TickEvent event) {
        // if (particles.size() >= maxCount.getValue()) {
        //     return;
        // }

        for (int i = 0; i < count.getValue(); i++) {
            Float value = range.getValue();
            double offsetX = MathUtil.random(-value, value);
            double offsetY = MathUtil.random(+.5f, rangeY.getValue());
            double offsetZ = MathUtil.random(-value, value);
            Vector3d additional = mc.player.getPositionVec().add(offsetX, 0, offsetZ);
            BlockPos bpos = mc.world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(additional));
            Vector3d pos = new Vector3d(bpos.getX(), spawnFromGround.getValue() ? bpos.getY() : mc.player.getPosY() + offsetY, bpos.getZ());

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

    @EventHandler
    public void worldRender(WorldRenderEvent worldRenderEvent) {
        for (Particle particle : particles) {
            particle.render(worldRenderEvent.getStack());
        }

        particles.removeIf(particle -> particle.getAnimation().isDone(Direction.BACKWARD));
    }

    private boolean isInPlayerView(Vector3d pos) {
        float oldYaw = mc.player.rotationYaw;
        mc.player.rotationYaw = getCameraYaw();
        Vector3d playerViewVec = mc.player.getLookVec();
        mc.player.rotationYaw = oldYaw;
        Vector3d playerToParticle = pos.subtract(mc.player.getPositionVec()).normalize();

        return playerViewVec.dotProduct(playerToParticle) > 0.1;
    }
    private static float getCameraYaw() {
        float yaw = mc.player.rotationYaw;
        if (mc.gameSettings.getPointOfView().func_243193_b()) yaw += 180;
        return yaw;
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
            float timerValue = Client.instance.moduleManager.particlesModule.timerForParticles.getValue();
            this.animation = new Animation(255, Duration.ofMillis((long) timerValue));
            this.type = type;
        }

        private Vector3d getRandomMotion() {
            return new Vector3d(
                    MathUtil.random(-0.04f, 0.04f),
                    MathUtil.random(0, 0.05f),
                    MathUtil.random(-0.04f, 0.04f)
            );
        }
        public void update() {
            float speed = (float) ((Minecraft.debugFPS > 0 ? (1.0000 / Minecraft.debugFPS) : 1) / 0.05F);
            ParticlesModule particlesModule = Client.instance.moduleManager.particlesModule;
            float yaw = getCameraYaw();
            float motionMultiplier = particlesModule.motionPower.getValue();

            pos = pos.add(motion.mul(speed * motionMultiplier, speed * motionMultiplier, speed * motionMultiplier));

            double xY = Math.sin(Math.toRadians(yaw));
            double zY = -Math.cos(Math.toRadians(yaw));

            double xX = -Math.sin(Math.toRadians(yaw + 90));
            double zX = Math.cos(Math.toRadians(yaw + 90));

            Vector3d addMotion = new Vector3d(
                    xY * incline2.getValue() / 50
                            +
                            xX * particlesModule.incline.getValue() / 50,

                    0,

                    zY * incline2.getValue() / 50
                            +
                            zX * particlesModule.incline.getValue() / 50
            );

            motion = motion.add(
                    addMotion.x * MathUtil.deltaTime() * motionMultiplier,
                    (particlesModule.gravity.getValue() / 80) * MathUtil.deltaTime() * motionMultiplier,
                    addMotion.z * MathUtil.deltaTime() * motionMultiplier
            );
            AxisAlignedBB alignedBB = new AxisAlignedBB(
                    pos.add(-0.5, -0.5, -0.5),
                    pos.add(0.5, 0.5, 0.5)
            );
            pos = pos.add(motion.mul(speed, speed, speed));
            motion = motion.add(0, -0.0002, 0);
            if (colission.getValue()) {
                if (!mc.world.isAirBlock(new BlockPos(pos))) {
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

            ParticlesModule particlesModule = Client.instance.moduleManager.particlesModule;
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
