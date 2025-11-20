package relake.common.component;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.joml.Math;
import org.joml.Vector2d;
import relake.Client;
import relake.common.util.ChatUtil;
import relake.point.PointTrace;
import relake.common.InstanceAccess;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.common.util.ProjectionUtil;
import relake.event.EventHandler;
import relake.event.impl.render.ScreenRenderEvent;
import relake.event.impl.render.WorldRenderEvent;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;

import java.util.concurrent.CopyOnWriteArrayList;

public class PointComponent implements InstanceAccess {
    public PointComponent() {
        Client.instance.eventManager.register(this);
    }

    private Matrix4f matrix = new Matrix4f();

    @EventHandler
    public void worldRender(WorldRenderEvent worldRenderEvent) {
        matrix = worldRenderEvent.getMatrix().copy();
        matrix.mul(worldRenderEvent.getStack().getLast().getMatrix());
    }

    @EventHandler
    public void screenRender(ScreenRenderEvent event2d) {
        CopyOnWriteArrayList<PointTrace> traces = Client.instance.pointsManager.traces;

        if (traces.isEmpty()
                || Minecraft.getInstance().player == null)
            return;

        traces.forEach(this::render);
    }

    private void render(PointTrace pointTrace) {
        MatrixStack stack = new MatrixStack();

        stack.push();

        Vector2d whScreen = getMouse(mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight());

        float wScreen = (float) whScreen.x;
        float hScreen = (float) whScreen.y;

        Vector3f pos = new Vector3f(pointTrace.x, pointTrace.y, pointTrace.z);
        pos.sub(new Vector3f((float) mc.getRenderManager().info.getProjectedView().x, (float) mc.getRenderManager().info.getProjectedView().y, (float) mc.getRenderManager().info.getProjectedView().z));

        Vector2f vec = Vector2f.ZERO;
        Vector2f vec2 = Vector2f.ZERO;
        boolean render = ProjectionUtil.toScreen(matrix, pos, vec);

        double guiScaleFactor = Minecraft.getInstance().getMainWindow().getGuiScaleFactor();
        float scale = (float) (1F / guiScaleFactor);

        vec2.set(vec.x * scale, vec.y * scale);

        if (render) {
            float x = vec2.x, y = vec2.y - 10F;
            float deltaPC = (System.currentTimeMillis() % 1000) / 1000.F;
            deltaPC = (deltaPC > .5F ? 1.F - deltaPC : deltaPC) * 2.F;
            y -= MathUtil.easeInOutCubic(Math.min(deltaPC * 1.5F, 1.F)) * 6F;

            int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

            float animation = Math.abs(Math.sin(System.currentTimeMillis() % 1000000000 / 250F));

            int[] color = {
                    ColorUtil.darker(rgb, (int) (222F - (222F * animation))),
                    ColorUtil.darker(rgb, (int) (222F - (222F * animation))),
                    ColorUtil.darker(rgb, (int) (222F * animation)),
                    ColorUtil.darker(rgb, (int) (222F * animation))
            };

            float imgSize = 30F * scale;

            ShapeRenderer box = Render2D.box(stack, x - imgSize / 2F, y, imgSize, imgSize);
            box.texture(new ResourceLocation("relake/point-marker.png"), color, false);

            boolean showCoords = Math.abs(wScreen - x * 2F) + Math.abs(hScreen - y * 2F) < 60F;

            x *= guiScaleFactor;
            y *= guiScaleFactor;

            if (showCoords) {
                String text = Client.instance.moduleManager.streamerModeModule.disguiseCoordsString((int) pointTrace.x + " " + (int) pointTrace.y + " " + (int) pointTrace.z);
                float width = Render2D.size(FontRegister.Type.BOLD, 12).getWidth(text);

                Render2D.size(FontRegister.Type.BOLD, 12).string(stack, text, x - width / 2F, y - 12F, -1);
            }

            String roundDistance = String.format("%.1f", Minecraft.getInstance().player.getDistanceToVec(new Vector3d(pointTrace.x, pointTrace.y, pointTrace.z))).replace("0,0", "0") + "m";

            y += 3.5F;

            Render2D.size(FontRegister.Type.BOLD, 12).string(stack, roundDistance, x + 15F, y, -1);
            Render2D.size(FontRegister.Type.BOLD, 12).string(stack, pointTrace.name, x - Render2D.size(FontRegister.Type.BOLD, 12).getWidth(pointTrace.name) - 17F, y, -1);
        }

        stack.pop();
    }

    private Vector2d getMouse(double mouseX, double mouseY) {
        return new Vector2d(mouseX, mouseY);
    }
}
