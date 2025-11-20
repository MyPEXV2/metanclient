package relake.module.implement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.optifine.render.RenderStateManager;
import org.lwjgl.opengl.GL11;
import relake.Client;
import relake.common.util.ColorUtil;
import relake.event.EventHandler;
import relake.event.impl.render.WorldRenderEvent;
import relake.module.Module;
import relake.module.ModuleCategory;

public class ChinaHatModule extends Module {
    private final BufferBuilder cone = new BufferBuilder(256);
    private final BufferBuilder line = new BufferBuilder(256);
    public MatrixStack stack = new MatrixStack();

    public ChinaHatModule() {
        super("China Hat", "Рендерит яркую шляпу на персонаже в китайском стиле", "Renders a bright hat on a character in Chinese style", ModuleCategory.Render);
    }

    public Framebuffer framebuffer = null;

    @EventHandler
    public void render(WorldRenderEvent event) {
        if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON)
            return;

//        if (framebuffer == null
//                || framebuffer.framebufferTextureWidth != mc.getMainWindow().getFramebufferWidth()
//                || framebuffer.framebufferTextureHeight != mc.getMainWindow().getFramebufferHeight()) {
//            if (framebuffer == null)
//                framebuffer = new Framebuffer(mc.getMainWindow().getFramebufferWidth(), mc.getMainWindow().getFramebufferHeight(), true);
//            else
//                framebuffer.resize(mc.getMainWindow().getFramebufferWidth(), mc.getMainWindow().getFramebufferHeight(), true);
//        }
//
//        framebuffer.framebufferClear(false);
//        framebuffer.copyDepthFrom(mc.getFramebuffer());
//        framebuffer.bindFramebuffer(true);

//        glClearColor(0.0f, 0.0f, 0.0f, 0F);
//        glClear(GL_COLOR_BUFFER_BIT);

        Entity entity = mc.player;

        float yOffset = this.getYOffset(entity);

        stack.push();
        RenderStateManager.disableCache();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableSmoothShadeModel();
        RenderSystem.depthMask(true);

        stack.translate(0, yOffset, 0);
        stack.rotate(Vector3f.ZN.rotationDegrees(180));
        stack.rotate(Vector3f.YP.rotationDegrees(-180));
        Matrix4f matrix4f = stack.getLast().getMatrix();

        double pointSize = 13D;
        double playerFov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);
        double scaleFactor = pointSize / playerFov;

        GL11.glPointSize((float) (pointSize * scaleFactor));

        cone.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        line.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);

        float x, y, z;

        float radius = 0.60F,
                steps = 1440,
                steps2 = 361;

        double angleStep = Math.PI / (steps / 2F),
                angleStep2 = Math.PI / 180;

        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

        cone.pos(matrix4f, 0, 0.28F, 0).color(ColorUtil.applyOpacity(ColorUtil.darker(rgb, 35), 235)).endVertex();

        for (int i = 0; i < steps; i++) {
            x = (float) (Math.cos(i * angleStep) * radius);
            y = 0;
            z = (float) (Math.sin(i * angleStep) * radius);

            line.pos(matrix4f, x, y, z).color(rgb).endVertex();
        }

        for (int i = 0; i < steps2; i++) {
            x = (float) (Math.cos(i * angleStep2) * radius);
            y = 0;
            z = (float) (Math.sin(i * angleStep2) * radius);

            cone.pos(matrix4f, x, y, z).color(ColorUtil.applyOpacity(ColorUtil.darker(rgb, 35), 235)).endVertex();
        }

        cone.pos(matrix4f, 0, 0.28F, 0).color(rgb).endVertex();

        TESSELLATOR.draw(cone);
        TESSELLATOR.draw(line);

        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.disableSmoothShadeModel();

        stack.pop();

//        mc.getFramebuffer().bindFramebuffer(true);
//
//        GlStateManager.enableBlend();
//        RenderSystem.defaultBlendFunc();
//
//        framebuffer.framebufferRenderExtRaw(mc.getMainWindow().getFramebufferWidth(), mc.getMainWindow().getFramebufferHeight(), false, 255);
    }

    private float getYOffset(Entity entity) {
        float offset = -0.445F;

        if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() instanceof ArmorItem) {
                offset -= 0.065F;
            }
        }

        return offset;
    }
}
