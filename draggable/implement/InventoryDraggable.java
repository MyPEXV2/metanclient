package relake.draggable.implement;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AirItem;
import net.minecraft.item.ItemStack;
import relake.Client;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.common.util.SkinUtil;
import relake.draggable.Draggable;
import relake.module.ModuleManager;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;

public class InventoryDraggable extends Draggable {

    public InventoryDraggable() {
        super("Inventory", 500, 100, 356, 116);
    }

    @Override
    public boolean visible() {
        ModuleManager moduleManager = Client.instance.moduleManager;
        return (!isEmpty() || mc.currentScreen instanceof ChatScreen) && Client.instance.moduleManager.hudModule.selectComponent.isSelected("InventoryHud") && Client.instance.moduleManager.hudModule.isEnabled();
    }

    @Override
    public void tick() {}
    @Override
    public void update() {}
    @Override
    public void render(MatrixStack matrixStack, float partialTicks) {
        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();
        width = isEmpty() ? 125 : 198;

        int[] filledSlotsInRows = getFilledSlotsPerRow();
        int rows = 0;
        boolean showFullInventory = false;

        for (int filled : filledSlotsInRows) {
            if (filled > 0) {
                rows++;
            }
        }

        boolean slots27to35Empty = true;
        for (int i = 27; i < 36; i++) {
            ItemStack slot = mc.player.inventory.getStackInSlot(i);
            if (!(slot.getItem() instanceof AirItem)) {
                slots27to35Empty = false;
                break;
            }
        }

        if (!slots27to35Empty) {
            showFullInventory = true;
        }

        boolean showInventoryDueToRows18to26 = true;
        for (int i = 9; i < 18; i++) {
            ItemStack slot = mc.player.inventory.getStackInSlot(i);
            if (!(slot.getItem() instanceof AirItem)) {
                showInventoryDueToRows18to26 = false;
                break;
            }
        }
        if (showInventoryDueToRows18to26) {
            for (int i = 18; i < 27; i++) {
                ItemStack slot = mc.player.inventory.getStackInSlot(i);
                if (!(slot.getItem() instanceof AirItem)) {
                    rows++;
                    break;
                }
            }
        }

        int calculatedHeight = 35 + (showFullInventory ? 3 : Math.max(rows, 1)) * 20 + 5;
        height += (calculatedHeight - height) * 0.2f;

        int round = 10;

        ShapeRenderer box = Render2D.box(matrixStack, x, y, width, height);

        box.quad(round, 0xB70E0E0F);
        box.quad(round, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f));

        Render2D.size(FontRegister.Type.BOLD, 14).string(matrixStack, name, x + 5, y + 5, ColorUtil.applyOpacity(ColorUtil.getColor(235, 235, 235), 255));
        Render2D.size(FontRegister.Type.ICONS, 20).string(matrixStack, "e", x + width - 5 - Render2D.size(FontRegister.Type.ICONS, 20).getWidth("l"), y + 4, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.35f));

        ShapeRenderer box1 = Render2D.box(matrixStack, x, y + 28.f, width, Math.max(60, height) - 29);
        box1.corner(new float[]{0, round, 0, round}, 0x590E0E0F);


        ShapeRenderer boxOutLine = Render2D.box(matrixStack, x - 2, y - 2, width +4, Math.max(60, height) + 4);
        boxOutLine.outlineHud(round, 2, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f));

        if (isEmpty()) {
            Render2D.size(FontRegister.Type.BOLD, 12).string(
                    matrixStack,
                    "Предметов нет.",
                    x + (width / 2) - (Render2D.size(FontRegister.Type.BOLD, 12).getWidth("Предметов нет.") / 2) - 3,
                    y + (height / 2) + 5,
                    ColorUtil.applyOpacity(ColorUtil.getColor(200, 200, 200), 215)
            );

            return;
        }

        float factor = (float) mc.getMainWindow().getGuiScaleFactor();
        float scale = (1 / factor);

        GlStateManager.pushMatrix();

        GlStateManager.translated((x + width / 2) / factor, (y + height / 2) / factor, 0);
        GlStateManager.scalef(animation.get(), animation.get(), animation.get());
        GlStateManager.translated(-(x + width / 2) / factor, -(y + height / 2) / factor, 0);

        GlStateManager.scalef(scale, scale, scale);

        for (int i = 0; i < 27; i++) {
            ItemStack item_stack = mc.player.inventory.mainInventory.get(i + 9);

            int item_position_x = (int) x + 10 + (i % 9) * 20;
            int item_position_y = (int) y + 35 + (i / 9) * 20;

            mc.getItemRenderer().renderItemAndEffectIntoGUI(item_stack, ((item_position_x / scale) / factor), (item_position_y / scale) / factor);
            mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, item_stack, (item_position_x / scale) / factor, (item_position_y / scale) / factor, null);
        }

        GlStateManager.popMatrix();
    }


    private int[] getFilledSlotsPerRow() {
        int[] filledSlots = new int[3];
        for (int i = 9; i < 36; ++i) {
            ItemStack slot = mc.player.inventory.getStackInSlot(i);
            if (!(slot.getItem() instanceof AirItem)) {
                int row = (i - 9) / 9;
                filledSlots[row]++;
            }
        }
        return filledSlots;
    }

    private boolean isEmpty() {
        for (int i = 9; i < 36; ++i) {
            ItemStack slot = mc.player.inventory.getStackInSlot(i);
            if (!(slot.getItem() instanceof AirItem)) {
                return false;
            }
        }

        return true;
    }

}
