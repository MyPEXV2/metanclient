package relake.module.implement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.joml.Vector2d;
import org.lwjgl.opengl.GL11;
import relake.Client;
import relake.animation.tenacity.Animation;
import relake.animation.tenacity.Direction;
import relake.common.component.rotation.FreeLookComponent;

import relake.common.component.rotation.Rotation;
import relake.common.component.rotation.RotationComponent;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.common.util.ProjectionUtil;
import relake.event.EventHandler;
import relake.event.impl.render.ScreenRenderEvent;
import relake.event.impl.render.WorldRenderEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.render.display.Render2D;
import relake.render.display.shape.ShapeRenderer;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;

import java.awt.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.lwjgl.opengl.GL11.*;

public class PearlPredictionModule extends Module {
    private Matrix4f matrix = new Matrix4f();
    public final Setting<Boolean> assist = new BooleanSetting("Помощник").setValue(true);

    private final Map<Entity, Vector3d> positions = new HashMap<>();
    private final Animation rotation = new Animation(1, Duration.ofMillis(2000))
            .setDirection(Direction.BACKWARD);

    public PearlPredictionModule() {
        super("Pearl Prediction", "Показывает траекторию полёта эндер-жемчугов", "Shows the flight path of the ender pearls", ModuleCategory.Render);
        registerComponent(assist);
    }

    @EventHandler
    public void test(ScreenRenderEvent event) {
        if (assist.getValue()) {
            try {
                AtomicReference<Entity> toRemove = new AtomicReference<>();

                this.positions.forEach((entity, pos3d) -> {
                    if (entity instanceof EnderPearlEntity e && (mc.player.getHeldItemMainhand().getItem() == Items.ENDER_PEARL || mc.player.getHeldItemMainhand().getItem() == Items.TRIDENT)) {
                        if (!mc.world.getAllEntities().contains(entity))
                            toRemove.set(entity);

                        float pTicks = mc.getRenderPartialTicks();

                        float pitch = (float) -Math.toDegrees(calcTrajectory(new BlockPos(pos3d)));
                        float yaw = get(pos3d, pTicks).x;

                        if (mc.player.getHeldItemMainhand().getItem() == Items.TRIDENT) {
                            Vector2f rot = get(pos3d, pTicks);
                            yaw = rot.x;
                            pitch = (float) (rot.y - mc.player.getDistanceToVec(pos3d) * 0.22f + mc.player.getMotion().y * mc.player.getDistanceToVec(pos3d) * (mc.player.getMotion().y > 0 ? 0.5f : 1));
                        }

                        float shortestYawPath = (float) Math.abs(((((yaw - FreeLookComponent.getFreeYaw()) % 360) + 540) % 360) - 180);


                        if (shortestYawPath < 10 && Math.abs(FreeLookComponent.getFreePitch() - pitch) < 10) {
                            RotationComponent.update(new Rotation(
                                            yaw,
                                            pitch),
                                    360, 360, 1, 6);
                        }

                        Vector3d mark3dPos = mc.player.getEyePosition(event.getPartialTicks()).add(mc.player.getVectorForRotation(pitch, yaw).mul(15, 15, 15));

                        if (isInPlayerView(mark3dPos) && mc.player.getDistanceToVec(pos3d) > 7) {
                            float size = 45;

                            if (rotation.isDone())
                                rotation.switchDirection(false);
                            else if (rotation.isDone(Direction.BACKWARD))
                                rotation.switchDirection(true);

                            Vector2f wts = Vector2f.ZERO;

                            boolean render = ProjectionUtil.toScreen(matrix, new Vector3f((float) (mark3dPos.getX() - mc.getRenderManager().renderPosX()), (float) (mark3dPos.getY() - mc.getRenderManager().renderPosY()), (float) (mark3dPos.getZ() - mc.getRenderManager().renderPosZ())), wts);

                            float scale = (float) (1F / mc.getMainWindow().getGuiScaleFactor());

                            wts.set(wts.x * scale, wts.y * scale);

                            if (render) {
                                Render2D.initRotate((float) wts.x, (float) wts.y, rotation.get() * 360F);

                                ShapeRenderer box = Render2D.box(event.getMatrixStack(), (float) (wts.x - size / 2), (float) (wts.y - size / 2), size, size);
                                box.texture(new ResourceLocation("relake/pearl-mark.png"), ((shortestYawPath < 10 && Math.abs(FreeLookComponent.getFreePitch() - pitch) < 10) ? Color.GREEN : Color.WHITE).getRGB());

                                Render2D.endRotate();
                            }
                        }
                    }
                });

                if (toRemove.get() != null)
                    positions.remove(toRemove.get());
            } catch (Exception e2) {
            }
        }
    }

    private boolean isInPlayerView(Vector3d pos) {
        Vector3d playerViewVec = mc.player.getLookVec();
        Vector3d playerToParticle = pos.subtract(mc.player.getPositionVec()).normalize();

        return playerViewVec.dotProduct(playerToParticle) > 0.1;
    }

    private Vector2f get(Vector3d target, float pTicks) {
        ClientPlayerEntity e = mc.player;
        double x = e.lastTickPosX + (e.getPosX() - e.lastTickPosX) * (double) pTicks;
        double y = e.lastTickPosY + (e.getPosY() - e.lastTickPosY) * (double) pTicks;
        double z = e.lastTickPosZ + (e.getPosZ() - e.lastTickPosZ) * (double) pTicks;

        Vector3d vec = target;
        double posX = vec.getX() - x;
        double posY = vec.getY() - (y + (double) mc.player.getEyeHeight());
        double posZ = vec.getZ() - z;
        double sqrt = MathHelper.sqrt(posX * posX + posZ * posZ);
        float yaw = (float) (Math.atan2(posZ, posX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(posY, sqrt) * 180.0 / Math.PI));
        float sens = (float) (Math.pow(mc.gameSettings.mouseSensitivity, 1.5) * 0.05f + 0.1f);
        float pow = sens * sens * sens * 1.2F;
        yaw -= yaw % pow;
        pitch -= pitch % (pow * sens);
        return new Vector2f(yaw, pitch);
    }

    public Vector2f correctRotation(float yaw, float pitch) {
        if ((yaw == -90 && pitch == 90) || yaw == -180)
            return new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);

        float gcd = getGCDValue();
        yaw -= yaw % gcd;
        pitch -= pitch % gcd;

        return new Vector2f(yaw, pitch);
    }

    private float getGCDValue() {
        return (float) (getGCD() * 0.15);
    }

    private float getGCD() {
        float f1;
        return (float) ((Math.pow(mc.gameSettings.mouseSensitivity * 0.6F + 0.2F, 2.0D) * 1.2F));
    }

    private float getDeltaMouse(float delta) {
        return Math.round(delta / getGCDValue());
    }

    private float calcTrajectory(BlockPos bp) {
        float pTicks = mc.getRenderPartialTicks();
        ClientPlayerEntity e = mc.player;
        double x = e.lastTickPosX + (e.getPosX() - e.lastTickPosX) * (double) pTicks;
        double y = e.lastTickPosY + (e.getPosY() - e.lastTickPosY) * (double) pTicks;
        double z = e.lastTickPosZ + (e.getPosZ() - e.lastTickPosZ) * (double) pTicks;
        
        double a = Math.hypot(bp.getX() + 0.5f - x, bp.getZ() + 0.5f - z);
        double y2 = 6.125 * ((bp.getY() + 1f) - (y + (double) mc.player.getEyeHeight(mc.player.getPose())));
        y2 = 0.05000000074505806 * ((0.05000000074505806 * (a * a)) + y2);
        y2 = Math.sqrt(Math.max(0, 9.37890625 - y2));
        double d = 3.0625 - y2;
        y2 = Math.atan2(d * d + y2, 0.05000000074505806 * a);
        d = Math.atan2(d, 0.05000000074505806 * a);
        return (float) Math.min(y2, d);
    }

    @EventHandler
    public void worldRender(WorldRenderEvent worldRenderEvent) {
        matrix = worldRenderEvent.getMatrix().copy();
        matrix.mul(worldRenderEvent.getStack().getLast()
                .getMatrix()
        );

        MatrixStack stack = worldRenderEvent.getStack();

        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();
        Vector3d entityPos = MathUtil.getEntityPos(mc.player);

        stack.push();
        stack.translate(-entityPos.x, -entityPos.y, -entityPos.z);

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1.5f);
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.disableCull();
        RenderSystem.blendFuncSeparate(GL_SRC_ALPHA, GL_ONE, GL_ZERO, GL_ONE);

        Matrix4f matrix = stack.getLast().getMatrix();

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof EnderPearlEntity pearl) {

                double x = pearl.getPosX(),
                        y = pearl.getPosY(),
                        z = pearl.getPosZ();

                double motionX = pearl.getMotion().x,
                        motionY = pearl.getMotion().y,
                        motionZ = pearl.getMotion().z;

                for (int i = 0; i < 100; i++) {
                    Vector3d lastPos = new Vector3d(x, y, z);

                    x += motionX;
                    y += motionY;
                    z += motionZ;

                    if (mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() == Blocks.WATER) {
                        motionX *= 0.8;
                        motionY *= 0.8;
                        motionZ *= 0.8;
                    } else {
                        motionX *= 0.99;
                        motionY *= 0.99;
                        motionZ *= 0.99;
                    }

                    motionY -= pearl.getGravityVelocity();

                    Vector3d pos = new Vector3d(x, y, z);

                    BlockRayTraceResult rayTraceResult = mc.world.rayTraceBlocks(
                            new RayTraceContext(
                                    lastPos,
                                    pos,
                                    RayTraceContext.BlockMode.OUTLINE,
                                    RayTraceContext.FluidMode.NONE,
                                    entity)
                    );

                    // Если это рил блок, то записываем в переменную что это последняя точка
                    boolean isLast = rayTraceResult.getType() == RayTraceResult.Type.BLOCK;

                    if (isLast) {
                        positions.put(entity, rayTraceResult.getHitVec());
                    }

                    if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY || rayTraceResult.getType() == RayTraceResult.Type.BLOCK || y <= 0) {
                        break;
                    }

                    float alpha = i / 40F;
                    int color = ColorUtil.applyOpacity(rgb, MathHelper.clamp((int) (255 * (alpha)), 0, 255));

                    BufferBuilder builder = Tessellator.getInstance().getBuffer();

                    builder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
                    builder.pos(matrix, (float) lastPos.x, (float) lastPos.y, (float) lastPos.z).color(color).endVertex();
                    builder.pos(matrix, (float) x, (float) y, (float) z).color(color).endVertex();
                    builder.finishDrawing();

                    WorldVertexBufferUploader.draw(builder);
                }
            }
        }

        RenderSystem.disableBlend();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.enableDepthTest();

        RenderSystem.enableCull();
        RenderSystem.enableTexture();

        stack.pop();
    }
}
