package relake.module.implement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;
import relake.Client;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.lwjgl.opengl.GL11.*;

public class VulcanPredictionModule extends Module {
    private Matrix4f matrix = new Matrix4f();
    private final Map<Entity, Vector3d> positions = new HashMap<>();

    public VulcanPredictionModule() {
        super("Vulcan Prediction", "Показывает траекторию полёта предметов пока они летят из вулкана", "Shows the trajectory of objects as they fly out of the volcano", ModuleCategory.Render);
    }

    @EventHandler
    public void test(ScreenRenderEvent event) {
        try {
            AtomicReference<Entity> toRemove = new AtomicReference<>();

            this.positions.forEach((entity, pos3d) -> {
                if (entity instanceof ItemEntity itemEntity) {
                    if (!mc.world.getAllEntities().contains(entity))
                        toRemove.set(entity);

                    Vector2f wts = Vector2f.ZERO;

                    boolean render = ProjectionUtil.toScreen(matrix, new Vector3f((float) (pos3d.getX() - mc.getRenderManager().renderPosX()), (float) (pos3d.getY() - mc.getRenderManager().renderPosY()), (float) (pos3d.getZ() - mc.getRenderManager().renderPosZ())), wts);

                    if (render) {
                        MatrixStack matrixStack = event.getMatrixStack();
                        ShapeRenderer box = Render2D.box(matrixStack, wts.x - mc.fontRenderer.getStringPropertyWidth(itemEntity.getItem().getDisplayName()) / 2F - 2F, wts.y, mc.fontRenderer.getStringPropertyWidth(itemEntity.getItem().getDisplayName()) + 7F, 18F);
                        box.quad(0, 0x50000000);
                        matrixStack.push();
                        matrixStack.scaleFix(1, 1, 1);
                        mc.fontRenderer.func_243246_a(matrixStack, itemEntity.getItem().getDisplayName(), wts.x - mc.fontRenderer.getStringPropertyWidth(itemEntity.getItem().getDisplayName()) / 2F + 2F, wts.y + 5, -1);
                        matrixStack.pop();
                    }
                }
            });

            if (toRemove.get() != null)
                positions.remove(toRemove.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        float partialTicks = mc.getRenderPartialTicks();

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof ItemEntity itemEntity) {
                String name = ((ItemEntity) entity).getItem().getDisplayName().getString().toLowerCase();

                if (name.contains("★")) {
                    double x = itemEntity.getPosX(),
                            y = itemEntity.getPosY(),
                            z = itemEntity.getPosZ();

                            double prevX = itemEntity.prevPosX,
                            prevY = itemEntity.prevPosY,
                            prevZ = itemEntity.prevPosZ;

                    double motionX = itemEntity.getMotion().x,
                            motionY = itemEntity.getMotion().y,
                            motionZ = itemEntity.getMotion().z;

                    for (int i = 0; i < 100; i++) {
                        Vector3d lastRenderPos = new Vector3d(
                                MathHelper.lerp(partialTicks, prevX, x),
                                MathHelper.lerp(partialTicks, prevY, y),
                                MathHelper.lerp(partialTicks, prevZ, z)
                        );

                        x += motionX;
                        y += motionY;
                        z += motionZ;

                        prevX = x;
                        prevY = y;
                        prevZ = z;

                        motionY -= 0.04F;

                        if (mc.world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock() == Blocks.WATER) {
                            motionX *= 0.99F;
                            motionY *= 0.99F;
                            motionZ *= 0.99F;
                        } else {
                            float f1 = 0.98F;

                            if (itemEntity.isOnGround()) {
                                f1 = mc.world.getBlockState(new BlockPos(x, y - 1.0D, z)).getBlock().getSlipperiness() * 0.98F;
                            }

                            motionX *= f1;
                            motionY *= 0.98D;
                            motionZ *= f1;
                        }

                        Vector3d renderPos = new Vector3d(
                                MathHelper.lerp(partialTicks, prevX, x),
                                MathHelper.lerp(partialTicks, prevY, y),
                                MathHelper.lerp(partialTicks, prevZ, z)
                        );

                        BlockRayTraceResult rayTraceResult = mc.world.rayTraceBlocks(
                                new RayTraceContext(
                                        lastRenderPos,
                                        renderPos,
                                        RayTraceContext.BlockMode.OUTLINE,
                                        RayTraceContext.FluidMode.NONE,
                                        entity)
                        );

                        boolean isLast = rayTraceResult.getType() == RayTraceResult.Type.BLOCK;

                        if (isLast) {
                            positions.put(entity, rayTraceResult.getHitVec());
                        }

                        if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY || rayTraceResult.getType() == RayTraceResult.Type.BLOCK || y <= 0) {
                            break;
                        }

                        float alpha = i / 10F;
                        int color = ColorUtil.applyOpacity(rgb, MathHelper.clamp((int) (255 * (alpha)), 0, 255));

                        BufferBuilder builder = Tessellator.getInstance().getBuffer();

                        builder.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
                        builder.pos(matrix, (float) lastRenderPos.x, (float) lastRenderPos.y, (float) lastRenderPos.z).color(color).endVertex();
                        builder.pos(matrix, (float) renderPos.x, (float) renderPos.y, (float) renderPos.z).color(color).endVertex();
                        builder.finishDrawing();

                        WorldVertexBufferUploader.draw(builder);

                        prevX = x;
                        prevY = y;
                        prevZ = z;
                    }
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
