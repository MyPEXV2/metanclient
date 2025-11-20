package relake.module.implement.combat;

import net.minecraft.item.AirItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;
import relake.common.util.InventoryUtil;
import relake.common.util.StopWatch;
import relake.event.EventHandler;
import relake.event.impl.misc.KeyboardEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.KeySetting;
import relake.settings.implement.MultiSelectSetting;
import relake.settings.implement.SelectSetting;

public class AutoSwapModule extends Module {

    public final SelectSetting item = new SelectSetting("Предмет")
            .setValue("Шар",
                    "Тотем",
                    "Щит",
                    "Гепл");

    public final SelectSetting swap = new SelectSetting("Свапать на")
            .setValue("На шар",
                    "На тотем",
                    "На щит",
                    "На гепл");

    private final Setting<Integer> key = new KeySetting("Клавиша для свапа")
            .setValue(-1);

    public AutoSwapModule() {
        super("Auto Swap", "Меняет местами 2 предмета по нажатию одной кнопки", "Swaps 2 items at the touch of a button", ModuleCategory.Combat);
        registerComponent(item, swap, key);
        item.setSelected("Щит");
        item.setSelected("На гепл");
    }

    private final StopWatch stopWatch = new StopWatch();

    @EventHandler
    public void keyboard(KeyboardEvent event) {
        ItemStack offhandItemStack = mc.player.getHeldItemOffhand();
        boolean isOffhandNotEmpty = !(offhandItemStack.getItem() instanceof AirItem);

        if (event.getKey() == key.getValue() && event.getAction() == GLFW.GLFW_PRESS && stopWatch.finished(200)) {
            Item currentItem = offhandItemStack.getItem();
            boolean isHoldingSwapItem = currentItem == getSwapItem();
            boolean isHoldingSelectedItem = currentItem == getSelectedItem();
            int selectedItemSlot = getSlot(getSelectedItem());
            int swapItemSlot = getSlot(getSwapItem());

            if (selectedItemSlot >= 0) {
                if (!isHoldingSelectedItem) {
                    InventoryUtil.moveItem(selectedItemSlot, 45, isOffhandNotEmpty);
                    stopWatch.reset();
                    return;
                }
            }
            if (swapItemSlot >= 0) {
                if (!isHoldingSwapItem) {
                    InventoryUtil.moveItem(swapItemSlot, 45, isOffhandNotEmpty);
                    stopWatch.reset();
                }
            }
        }
    }

    private Item getSwapItem() {
        if (swap.isSelected("На шар")) return Items.PLAYER_HEAD;
        if (swap.isSelected("На тотем")) return Items.TOTEM_OF_UNDYING;
        if (swap.isSelected("На щит")) return Items.SHIELD;
        if (swap.isSelected("На гепл")) return Items.GOLDEN_APPLE;
        return Items.AIR;
    }

    private Item getSelectedItem() {
        if (item.isSelected("Шар")) return Items.PLAYER_HEAD;
        if (item.isSelected("Тотем")) return Items.TOTEM_OF_UNDYING;
        if (item.isSelected("Щит")) return Items.SHIELD;
        if (item.isSelected("Гепл")) return Items.GOLDEN_APPLE;
        return Items.AIR;
    }

    private int getSlot(Item item) {
        int finalSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                if (mc.player.inventory.getStackInSlot(i).isEnchanted()) {
                    finalSlot = i;
                    break;
                } else {
                    finalSlot = i;
                }
            }
        }
        if (finalSlot < 9 && finalSlot != -1) {
            finalSlot = finalSlot + 36;
        }
        return finalSlot;
    }
}
