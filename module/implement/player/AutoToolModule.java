package relake.module.implement.player;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockRayTraceResult;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.module.Module;
import relake.module.ModuleCategory;

public class AutoToolModule extends Module {
    public int itemIndex = -1, oldSlot = -1;
    boolean status;

    public AutoToolModule() {
        super("Auto Tool", "Автоматически переключается на наиболее эффективный инструмент", "Automatically switches to the most effective tool", ModuleCategory.Player);
    }

    @EventHandler
    public void onUpdate(TickEvent e) {
        if (mc.player == null || mc.player.isCreative()) {
            itemIndex = -1;
            return;
        }

        if (isMousePressed()) {
            itemIndex = findBestToolSlotInHotBar();

            if (itemIndex != -1) {
                status = true;

                if (oldSlot == -1) {
                    oldSlot = mc.player.inventory.currentItem;
                }

                mc.player.inventory.currentItem = itemIndex;
            }
        } else if (status && oldSlot != -1) {
            mc.player.inventory.currentItem = oldSlot;

            itemIndex = oldSlot;
            status = false;
            oldSlot = -1;
        }
    }

    @Override
    public void disable() {
        status = false;
        itemIndex = -1;
        oldSlot = -1;

        super.disable();
    }

    private int findBestToolSlotInHotBar() {
        if (mc.objectMouseOver instanceof BlockRayTraceResult blockRayTraceResult) {
            Block block = mc.world.getBlockState(blockRayTraceResult.getPos()).getBlock();

            int bestSlot = -1;
            float bestSpeed = 1.0f;

            for (int slot = 0; slot < 9; slot++) {
                float speed = mc.player.inventory.getStackInSlot(slot)
                        .getDestroySpeed(block.getDefaultState());

                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = slot;
                }
            }
            return bestSlot;
        }
        return -1;
    }


    private boolean isMousePressed() {
        return mc.objectMouseOver != null && mc.gameSettings.keyBindAttack.isKeyDown();
    }
}
