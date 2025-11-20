//package relake.draggable.implement;
//
//import com.mojang.blaze3d.matrix.MatrixStack;
//import com.mojang.blaze3d.platform.GlStateManager;
//import net.minecraft.client.gui.screen.ChatScreen;
//import net.minecraft.client.renderer.model.ItemCameraTransforms;
//import net.minecraft.item.ItemStack;
//import org.lwjgl.glfw.GLFW;
//import relake.Client;
//import relake.animation.tenacity.Direction;
//import relake.common.util.StopWatch;
//import relake.draggable.Draggable;
//import relake.module.ModuleManager;
//import relake.render.display.Render2D;
//import relake.render.display.shape.ShapeRenderer;
//
//public class ArmorHudDraggable extends Draggable {
//    public ArmorHudDraggable() {
//        super("ArmorHud", 300, 100, 160, 30);
//    }
//
//    @Override
//    public boolean visible() {
//        ModuleManager moduleManager = Client.instance.moduleManager;
//
//        return mc.currentScreen instanceof ChatScreen && moduleManager.hudModule.armorHud.getValue() && moduleManager.hudModule.isEnabled();
//    }
//
//    @Override
//    public void tick() {}
//    @Override
//    public void update() {}
//    @Override
//    public void render(MatrixStack matrixStack, float partialTicks) {
//        ShapeRenderer box = Render2D.box(matrixStack, x, y, width, height);
//
//        boolean ver = mc.getMainWindow().getScaledWidth() / 4F < x || (mc.getMainWindow().getScaledWidth() - mc.getMainWindow().getScaledWidth() / 4F) > x;
//
//        if (ver) {
//            // Рендер вертикально брони
//
//            float x = this.x + 5F;
//            float y = this.y + 5F;
//
//            for (ItemStack itemStack : mc.player.inventory.armorInventory) {
//                GlStateManager.translatef(x, y, 0F);
//                mc.getItemRenderer().renderItemIntoGUI(itemStack, 0, 0);
//                GlStateManager.translatef(-x, -y, 0F);
//
//                y += 15F;
//            }
//
//            width = 45F;
//            height = 100F;
//        } else {
//            // Рендер горизонтально брони
//
//            float x = this.x + 5F;
//            float y = this.y + 5F;
//
//            for (ItemStack itemStack : mc.player.inventory.armorInventory) {
//                GlStateManager.translatef(x, y, 0F);
//                mc.getItemRenderer().renderItemIntoGUI(itemStack, 0, 0);
//                GlStateManager.translatef(-x, -y, 0F);
//
//                x += 15F;
//            }
//
//            width = 140F;
//            height = 45F;
//        }
//    }
//}
