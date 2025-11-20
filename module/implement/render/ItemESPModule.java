package relake.module.implement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.IFormattableTextComponent;
import relake.Client;
import relake.common.util.ProjectionUtil;
import relake.event.EventHandler;
import relake.event.impl.render.ScreenRenderEvent;
import relake.event.impl.render.WorldRenderEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;
import relake.render.display.shape.Side;

public class ItemESPModule extends Module {
    private Matrix4f matrix = new Matrix4f();

    public ItemESPModule() {
        super("Item ESP", "Выделяет лежащие на земле предметы упрощая их восприятие", "Highlights objects lying on the ground, simplifying their perception", ModuleCategory.Render);
    }

    @EventHandler
    public void worldRender(WorldRenderEvent worldRenderEvent) {
        matrix = worldRenderEvent.getMatrix().copy();
        matrix.mul(worldRenderEvent.getStack().getLast()
                .getMatrix()
        );
    }

    @EventHandler
    public void screenRender(ScreenRenderEvent screenRenderEvent) {
        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof ItemEntity itemEntity) {
                MatrixStack matrixStack = screenRenderEvent.getMatrixStack();
                int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

                Vector4f position = ProjectionUtil.getEntity2DPosition(matrix, itemEntity);

                int padding = (int) ((position.getZ() - position.getX()) / 50);

                float x = position.getX() + padding,
                        y = position.getY(),
                        w = position.getZ() - x - padding,
                        h = position.getW() - y;

                ShapeRenderer box = Render2D.box(matrixStack, x, y, w, h);
                box.outline(1, 4, 0xFF000000);
                box.expand(Side.ALL, -1);
                box.outline(1, 2, rgb);

                IFormattableTextComponent displayName = itemEntity.getDisplayName().deepCopy();

                float width = Render2D.size(FontRegister.Type.BOLD, 12).getWidth(StringUtils.stripControlCodes(displayName.getString())) + 5;

                ShapeRenderer nametags = Render2D.box(matrixStack, x + w / 2 - width / 2, y - 21, width, 17);
                nametags.expand(Side.ALL, 1);
                nametags.quad(0, 0x50000000);

                Render2D.size(FontRegister.Type.BOLD, 12).centeredString(matrixStack, displayName, x + w / 2, y - 20.5f, -1);
            }
        }
    }
}

