package relake.module.implement.misc;

import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.module.Module;
import relake.module.ModuleCategory;

public class AutoEatModule extends Module {

    public AutoEatModule() {
        super("Auto Eat", "Поглащает пищу без вашего участия", "Consumes food without you", ModuleCategory.Misc);
    }
        public boolean isEating = false;

    @EventHandler
    public void tick(TickEvent event) {
        mc.gameSettings.keyBindUseItem.pressed = isEating;
        int previousSlot = mc.player.inventory.currentItem;
        if (mc.player == null || mc.world == null) return;
        if (mc.player.getFoodStats().getFoodLevel() < 18) {
            int slot = findEatSlot();
            if (slot == -1) return;
            mc.player.inventory.currentItem = slot;
            isEating = true;
        } else {
            int slot = findEatSlot();
            if (slot == -1) return;
            mc.player.inventory.currentItem = previousSlot;
            isEating = mc.player.getFoodStats().needFood();
        }
    }
        public int findEatSlot() {
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(slot);

                if (stack.getUseAction() == UseAction.EAT) {
                    return slot;
                }
            }
            return -1;
        }
}
