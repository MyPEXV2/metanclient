package relake.module.implement.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.*;
import org.apache.logging.log4j.spi.CopyOnWrite;
import org.joml.Vector2d;
import org.lwjgl.opengl.GL11;
import relake.Client;
import relake.animation.apelcin4ik.AnimationDirection;
import relake.animation.apelcin4ik.impl.EaseAnimation;
import relake.animation.excellent.Animation;
import relake.animation.excellent.util.Easing;
import relake.animation.excellent.util.Easings;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.common.util.ProjectionUtil;
import relake.common.util.StopWatch;
import relake.event.EventHandler;
import relake.event.impl.render.ScreenRenderEvent;
import relake.event.impl.render.WorldRenderEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.render.display.Render2D;
import relake.render.display.shape.ShapeRenderer;
import relake.render.world.Render3D;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.FloatSetting;
import relake.settings.implement.SelectSetting;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

    public class TargetESPModule extends Module {

    public static final SelectSetting mode = new SelectSetting("Мод")
            .setValue("Квадрат",
                    "Круг",
                    "Призраки");


    private Matrix4f matrix = new Matrix4f();

    private final Animation animation = new Animation();
    private final relake.animation.apelcin4ik.Animation redAnimation = new EaseAnimation(150, 0);

    public final Setting<Boolean> onAim = new BooleanSetting("При наведении")
            .setValue(false);

    public final Setting<Float> sizeVal = new FloatSetting("Размер")
            .range(50f, 125f, 5f)
            .setValue(125F).setVisible(() -> !mode.isSelected("Призраки") && mode.isSelected("Квадрат"));

    
    public static final Setting<Float> ghostCount = new FloatSetting("Количество призраков").range(2f, 5f, 1f).setValue(3f).setVisible(() -> mode.isSelected("Призраки"));

    public static final Setting<Float> ghostTimer = new FloatSetting("Время существования").range(150f, 500f, 25f).setValue(350f).setVisible(() -> mode.isSelected("Призраки"));

    public static final Setting<Float> strengthXZ = new FloatSetting("Время цикла по XZ").range(1000f, 5000f, 100f).setValue(2000f).setVisible(() -> mode.isSelected("Призраки"));

    public static final Setting<Float> strengthY = new FloatSetting("Время цикла по Y").range(1000f, 5000f, 100f).setValue(1700f).setVisible(() -> mode.isSelected("Призраки"));


    public TargetESPModule() {
        super("Target ESP", "Подсвечивает главного врага на текущий момент если он есть", "Highlights the main enemy at the current moment if there is one", ModuleCategory.Render);
        registerComponent(onAim, sizeVal, ghostCount, ghostTimer, strengthXZ, strengthY, mode);
    }

    private LivingEntity target, lastHandledTarget;

    @EventHandler
    public void worldRender(WorldRenderEvent worldRenderEvent) {
        if (mode.isSelected("Круг") && target != null) {
            float radius = target.getWidth() * 1f;

            final double x = MathUtil.interpolate(target.getPosX(), target.lastTickPosX, mc.getRenderPartialTicks()) - mc.getRenderManager().renderPosX();
            final double y = MathUtil.interpolate(target.getPosY(), target.lastTickPosY, mc.getRenderPartialTicks()) - mc.getRenderManager().renderPosY();
            final double z = MathUtil.interpolate(target.getPosZ(), target.lastTickPosZ, mc.getRenderPartialTicks()) - mc.getRenderManager().renderPosZ();

            int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

            Vector3d vec = new Vector3d(x,y,z);

            double duration = 3000;
            double elapsed = System.currentTimeMillis() % duration;
            boolean side = elapsed > (duration / 2);
            double progress = elapsed / (duration / 2);
            progress = side ? progress - 1 : 1 - progress;
            progress = (progress < 0.5) ? 2 * progress * progress : 1 - Math.pow(-2 * progress + 2, 2) / 2;
            double eased = (target.getHeight() / 2) * ((progress > 0.5) ? 1 - progress : progress) * (side ? -1 : 1);

            BufferBuilder buffer = Tessellator.getInstance().getBuffer();

            worldRenderEvent.getStack().push();
            Vector3d scale = mc.getRenderManager().info.getProjectedView().scale(-1);
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableAlphaTest();
            RenderSystem.shadeModel(GL11.GL_SMOOTH);
            RenderSystem.disableCull();
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            RenderSystem.lineWidth(2f);
            RenderSystem.color4f(1f, 1f, 1f, 1f);

            buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR);

            for (int i = 0; i <= 360; i++) {
                int color = ColorUtil.applyOpacity(rgb, animation.get() / 2);
                double cosRad = Math.cos(Math.toRadians(i));
                double sinRad = Math.sin(Math.toRadians(i));
                buffer.pos(worldRenderEvent.getStack().getLast().getMatrix(), (float) (vec.x + cosRad * radius), (float) (vec.y + (target.getHeight() * progress) + 0.025f), (float) (vec.z + sinRad * radius)).color(color).endVertex();
                buffer.pos(worldRenderEvent.getStack().getLast().getMatrix(), (float) (vec.x + cosRad * radius), (float) (vec.y + (target.getHeight() * progress) + eased), (float) (vec.z + sinRad * radius)).color(ColorUtil.applyOpacity(rgb, 0)).endVertex();
            }
            buffer.finishDrawing();
            WorldVertexBufferUploader.draw(buffer);
            RenderSystem.color4f(1f, 1f, 1f, 1f);

            buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i <= 360; i++) {
                double cosRad = Math.cos(Math.toRadians(i));
                double sinRad = Math.sin(Math.toRadians(i));
                buffer.pos(worldRenderEvent.getStack().getLast().getMatrix(), (float) (vec.x + cosRad * radius), (float) (vec.y + (target.getHeight() * progress) + 0.025f), (float) (vec.z + sinRad * radius)).color(ColorUtil.applyOpacity(rgb, animation.get())).endVertex();
            }
            buffer.finishDrawing();
            WorldVertexBufferUploader.draw(buffer);

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.enableTexture();
            RenderSystem.enableAlphaTest();
            RenderSystem.shadeModel(GL11.GL_FLAT);
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            worldRenderEvent.getStack().pop();
        } else if (mode.isSelected("Призраки")) {
            if (points.isEmpty()) lastHandledTarget = null;
            if (target != null) lastHandledTarget = target;
            if (lastHandledTarget != null) {
                //1 это колво призраков, 2 это время жизни
                if (target != null) addGhosts(lastHandledTarget, ghostCount.getValue().intValue(), worldRenderEvent.getTicks(), ghostTimer.getValue().intValue(), Client.instance.moduleManager.hudModule.color.getValue().getRGB());
                oldGhostsRemoverPreRender();
                drawAllGhosts(worldRenderEvent, 0.5f);
            }
        }

        matrix = worldRenderEvent.getMatrix().copy();
        matrix.mul(worldRenderEvent.getStack().getLast()
                .getMatrix()
        );
    }

    @EventHandler
    public void worldRender(ScreenRenderEvent worldRenderEvent) {
        findTarget();

        if (target == null)
            return;

        if(mode.isSelected("Квадрат")) {

            double sin = MathUtil.clamp(0, 360, (float) ((Math.sin(System.currentTimeMillis() / 1000D) + 1F) / 2));
            float size = sizeVal.getValue();

            int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

            // redAnimation

            redAnimation.setDirection(target.hurtTime > 2);

            int[] color = {
                    ColorUtil.applyOpacity(ColorUtil.darker(rgb, 35), animation.get()),
                    ColorUtil.applyOpacity(ColorUtil.darker(rgb, 35), animation.get()),
                    ColorUtil.applyOpacity(ColorUtil.darker(rgb, 75), animation.get()),
                    ColorUtil.applyOpacity(ColorUtil.darker(rgb, 75), animation.get())
            };

            for (int i = 0; i < color.length; i++) {
                color[i] = ColorUtil.interpolateColor(color[i], Color.RED.getRGB(), redAnimation.get() * 75F);
            }

            final double x = MathUtil.interpolate(target.getPosX(), target.lastTickPosX, worldRenderEvent.getPartialTicks()) - mc.getRenderManager().renderPosX();
            final double y = MathUtil.interpolate(target.getPosY(), target.lastTickPosY, worldRenderEvent.getPartialTicks()) - mc.getRenderManager().renderPosY();
            final double z = MathUtil.interpolate(target.getPosZ(), target.lastTickPosZ, worldRenderEvent.getPartialTicks()) - mc.getRenderManager().renderPosZ();

            Vector2f marker = Vector2f.ZERO;

            boolean render = ProjectionUtil.toScreen(matrix, new Vector3f((float) x, (float) (y + ((target.getEyeHeight() + 0.4F) * 0.5F)), (float) z), marker);

            if (!render)
                return;

            if (marker == null)
                return;


            float scale = (float) (1F / mc.getMainWindow().getGuiScaleFactor());
            marker.set(marker.x * scale, marker.y * scale);

            GlStateManager.pushMatrix();
            GlStateManager.translatef((float) marker.x, (float) marker.y, 0);
            GlStateManager.rotatef((float) sin * 360, 0, 0, 1);
            GlStateManager.scalef(animation.get() / 255F, animation.get() / 255F, 0F);
            GlStateManager.translatef((float) -marker.x, (float) -marker.y, 0);

            ShapeRenderer box = Render2D.box(worldRenderEvent.getMatrixStack(), (float) marker.x - size / 2f, (float) marker.y - size / 2f, size, size);
            int[] colorsWithOpacity = new int[color.length];

            for (int i = 0; i < color.length; i++) {
                colorsWithOpacity[i] = ColorUtil.applyOpacity(color[i], animation.get());
            }

            box.texture(new ResourceLocation("relake/marker.png"), colorsWithOpacity);
            GlStateManager.popMatrix();
        }
    }

    private void findTarget() {
        animation.update();

        LivingEntity auraTarget = Client.instance.moduleManager.attackAuraModule.getTarget();

        boolean condition = auraTarget == null && (!onAim.getValue() || mc.pointedEntity == null);

        LivingEntity target = null;

        if (auraTarget != null) {
            target = auraTarget;
        } else if (onAim.getValue() && mc.pointedEntity != null) {
            target = (LivingEntity) mc.pointedEntity;
        }

        float speed = 0.25f;
        Easing easing = Easings.LINEAR;

        if (target == null && this.target != null) {
            if (animation.getToValue() != 0)
                animation.run(0, speed, easing, false);

            if (animation.isFinished()) {
                this.target = target;
            }
        } else if (target != this.target) {
            if (animation.getToValue() != 0)
                animation.run(0, speed, easing, false);

            if (animation.isFinished()) {
                this.target = target;

                if (animation.getToValue() != 255)
                    animation.run(255, speed, easing, false);
            }
        } else if (this.target != null) {
            if (animation.getToValue() != 255)
                animation.run(255, speed, easing, false);
        }
    }

    private CopyOnWriteArrayList<GlowPoint> points = new CopyOnWriteArrayList<>();
    private class GlowPoint {
        private final float x, y, z;
        private final StopWatch stopWatch;
        private int maxTime, baseColor;
        private float currentTimePC;
        public GlowPoint(float x, float y, float z, int maxTime, int color) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.stopWatch = new StopWatch();
            this.maxTime = maxTime;
            this.baseColor = color;
        }

        public float getTimePC() {return Math.min(this.stopWatch.elapsedTime() / (float) maxTime, 1F);}
        public float getTimePCUpdated() {return this.currentTimePC;}
        public int getColor(float timePC) {
            timePC = (timePC > .5F ? 1.F - timePC : timePC) * 2.F;
            return ColorUtil.multAlpha(this.baseColor, timePC);
        }
        public float getX() {return this.x;}
        public float getY() {return this.y;}
        public float getZ() {return this.z;}
        public boolean removeIfUpdateValueTimePC() {
            final float timePC = this.getTimePC();
            this.currentTimePC = timePC;
            return timePC == 1.F;
        }
    }

    public void oldGhostsRemoverPreRender() {
        this.points.removeIf(GlowPoint::removeIfUpdateValueTimePC);
    }

    private final ResourceLocation texture = new ResourceLocation("relake/bloom.png");

    public void drawAllGhosts(WorldRenderEvent event, float scale) {
        if (this.points.isEmpty()) return;
        this.points.forEach(point -> {
            final float timePC = point.getTimePCUpdated(), scaleOfTime = scale * (1.F - timePC);
            event.getStack().push();
            event.getStack().translate(point.getX() - mc.getRenderManager().renderPosX(), point.getY() - mc.getRenderManager().renderPosY(), point.getZ() - mc.getRenderManager().renderPosZ());
            event.getStack().rotate(mc.getRenderManager().getCameraOrientation());
            ShapeRenderer box = Render2D.box(event.getStack(), -scaleOfTime / 2.F, -scaleOfTime / 2.F, scaleOfTime, scaleOfTime);
            box.texture(texture, point.getColor(timePC));
            event.getStack().pop();
        });
    }

    public void addGhosts(LivingEntity entity, int cornersCount, float partialTicks, int maxTime, int colorBase) {
        final float x = (float) (entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * partialTicks),
                y = (float) (entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * partialTicks),
                z = (float) (entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * partialTicks);
        final float xzRange = entity.getWidth(), yRange = entity.getHeight();


        int delayXZ = strengthXZ.getValue().intValue(), delayY = strengthY.getValue().intValue();
        long time = System.currentTimeMillis();
        for (int corner = 0; corner < cornersCount; corner++) {
            float cornersPC = corner / (float) cornersCount;
            float xzRotate = ((time + (int) (delayXZ * cornersPC)) % delayXZ) / (float) delayXZ * 360.F;
            float yLrpPC = ((time + (int) (delayY * cornersPC)) % delayY) / (float)delayY;
            yLrpPC = (yLrpPC > .5F ? 1.F - yLrpPC : yLrpPC) * 2.F;
            yLrpPC = (float) Easings.QUAD_IN_OUT.ease(yLrpPC);
            double yawRad = Math.toRadians(MathHelper.wrapDegrees(cornersPC * 360.F + xzRotate));
            final float xPos = x - (float) Math.sin(yawRad) * xzRange,
                    yPos = y + yRange * yLrpPC,
                    zPos = z + (float) Math.cos(yawRad) * xzRange;
            this.points.add(new GlowPoint(xPos, yPos, zPos, maxTime, colorBase));
        }
    }
}