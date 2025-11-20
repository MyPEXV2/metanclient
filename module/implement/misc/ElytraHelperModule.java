package relake.module.implement.misc;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import relake.common.util.ChatUtil;
import relake.common.util.InventoryUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.KeyboardEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.KeySetting;

import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.StreamSupport;

public class ElytraHelperModule extends Module {

    public ElytraHelperModule() {

        super("Elytra Helper", "Облегчает процесс полёта на элитрах", "Facilitates the process of flying on eliters", ModuleCategory.Misc);
registerComponent(swapKey,fireworkKey);
    }

    private final Queue<Runnable> actionQueue = new LinkedList<>();

    public Setting<Integer> swapKey = new KeySetting("Кнопка свапа").setValue(-1);
    public Setting<Integer> fireworkKey = new KeySetting("Кнопка фейерверков").setValue(-1);

    @EventHandler
    public void onKey(KeyboardEvent event) {

        if (event.getKey() == swapKey.getValue() && event.getAction() == GLFW.GLFW_PRESS) {

            swapElytra();
        }

        if (event.getKey() == fireworkKey.getValue()  && event.getAction() == GLFW.GLFW_PRESS) {
            useFirework();
        }
    }
    private boolean isElytraEquipped() {
        return StreamSupport.stream(mc.player.getArmorInventoryList().spliterator(), false)
                .anyMatch(stack -> stack.getItem() == Items.ELYTRA);
    }

    private void swapElytra() {
        if (isElytraEquipped()) {
            Optional<Integer> chestplateSlotOpt = InventoryUtil.findItem(
                    stack -> stack.getItem() instanceof ArmorItem &&
                            ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlotType.CHEST &&
                            stack.getItem() != Items.ELYTRA,
                    true,
                    true
            );
            if (chestplateSlotOpt.isPresent()) {
                int chestplateSlot = chestplateSlotOpt.get();
                mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, chestplateSlot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player);
            } else {
                Optional<Integer> emptySlot = InventoryUtil.findItem(ItemStack::isEmpty, true, true);
                if (emptySlot.isPresent()) {
                    mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, emptySlot.get(), 0, ClickType.PICKUP, mc.player);
                }
            }
        } else {

            Optional<Integer> elytraSlotOpt = InventoryUtil.findItem(
                    stack -> stack.getItem() == Items.ELYTRA,
                    true,
                    true
            );
            if (elytraSlotOpt.isPresent()) {
                int elytraSlot = elytraSlotOpt.get();
                mc.playerController.windowClick(0, elytraSlot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(0, elytraSlot, 0, ClickType.PICKUP, mc.player);
            }
        }
    }

    private void useFirework() {
        if (!mc.player.isElytraFlying()) return;

        int fireworkSlot = getSlotInInventory(Items.FIREWORK_ROCKET);

        if (fireworkSlot == -1) {
            return;
        }

        if (fireworkSlot < 9) {
            mc.player.connection.sendPacket(new CHeldItemChangePacket(fireworkSlot));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
        } else {


            mc.playerController.pickItem(fireworkSlot);
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.playerController.pickItem(fireworkSlot);
        }
    }
    public int getSlotInInventory(Item item) {
        for (int i = 0; i < mc.player.inventory.mainInventory.size(); i++) {
            ItemStack stack = mc.player.inventory.mainInventory.get(i);
            if (stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    private void enqueueAction(Runnable action) {
        actionQueue.add(action);
    }
}
