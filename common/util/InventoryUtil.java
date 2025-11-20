package relake.common.util;

import lombok.experimental.UtilityClass;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.client.CClickWindowPacket;
import org.apache.commons.lang3.StringUtils;
import relake.common.InstanceAccess;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

@UtilityClass
public class InventoryUtil implements InstanceAccess {
    public final int EMPTY = -1;

    public int getHotBarItemSlot(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                return i;
            }
        }

        return EMPTY;
    }

    public void moveItem(int from, int to, boolean air) {
        if (from == to)
            return;

        pickupItem(from, 0);
        pickupItem(to, 0);

        if (air)
            pickupItem(from, 0);
    }

    public void pickupItem(int slot, int button) {
        short short1 = mc.player.openContainer.getNextTransactionID(mc.player.inventory);
        mc.getConnection().sendPacket(new CClickWindowPacket(0, slot, button, ClickType.PICKUP, mc.player.openContainer.getSlot(slot).getStack(), short1));
    }

    public int findAnyBlockInHotBar() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.TORCH) {
                continue;
            }

            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof BlockItem) {
                return i;
            }
        }
        return EMPTY;
    }

    public int findIceInHotBar() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof BlockItem) {
                Block block = ((BlockItem) mc.player.inventory.getStackInSlot(i).getItem()).getBlock();

                if (block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE) {
                    return i;
                }
            }
        }
        return EMPTY;
    }

    public Optional<Integer> findItem(Predicate<ItemStack> stackPredicate, Comparator<ItemStack> comparator, boolean add36) {
        List<Integer> matchingIndexes = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stackPredicate.test(stack)) {
                matchingIndexes.add(i < 9 ? (add36 ? 36 : 0) + i : i);
            }
        }
        matchingIndexes.sort(Comparator.comparing(mc.player.inventory::getStackInSlot, comparator));
        return matchingIndexes.isEmpty() ? Optional.empty() : Optional.of(matchingIndexes.get(0));
    }

    public Optional<Integer> findItem(Predicate<ItemStack> stackPredicate, Comparator<ItemStack> comparator,boolean searchEntireInventory, boolean add36) {
        int inventorySize = searchEntireInventory ? mc.player.inventory.getSizeInventory() : 9;

        List<Integer> matchingIndexes = new ArrayList<>();
        for (int i = 0; i < inventorySize; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stackPredicate.test(stack)) {
                matchingIndexes.add(i < 9 ? (add36 ? 36 : 0) + i : i);
            }
        }
        matchingIndexes.sort(Comparator.comparing(mc.player.inventory::getStackInSlot, comparator));
        return matchingIndexes.isEmpty() ? Optional.empty() : Optional.of(matchingIndexes.get(0));
    }

    public Optional<Integer> findItem(Predicate<ItemStack> stackPredicate, boolean searchEntireInventory, boolean add36) {
        int inventorySize = searchEntireInventory ? mc.player.inventory.getSizeInventory() : 9;

        for (int i = 0; i < inventorySize; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stackPredicate.test(stack)) {

                return Optional.of(i < 9 ? (add36 ? 36 : 0) + i : i);
            }
        }

        return Optional.empty();
    }

    public Slot getInventorySlot(Item item) {
        return mc.player.openContainer.inventorySlots.stream().filter(s -> s.getStack().getItem().equals(item) && s.slotNumber >= mc.player.openContainer.inventorySlots.size() - 36).findFirst().orElse(null);
    }

    public Slot getInventorySlot(List<Item> item) {
        return mc.player.openContainer.inventorySlots.stream().filter(s -> item.contains(s.getStack().getItem()) && s.slotNumber >= mc.player.openContainer.inventorySlots.size() - 36).findFirst().orElse(null);
    }

    public Slot getFoodMaxSaturationSlot() {
        return mc.player.openContainer.inventorySlots.stream().filter(s -> s.getStack().getItem().getFood() != null && !s.getStack().getItem().getFood().canEatWhenFull()).max(Comparator.comparingDouble(s -> s.getStack().getItem().getFood().getSaturation())).orElse(null);
    }

    public int getInventoryCount(Item item) {
        return IntStream.range(0, 45).filter(i -> mc.player.inventory.getStackInSlot(i).getItem().equals(item)).map(i -> mc.player.inventory.getStackInSlot(i).getCount()).sum();
    }

    public void clickSlot(Slot slot, int button, ClickType clickType, boolean packet) {
        if (slot != null) clickSlotId(slot.slotNumber, button, clickType, packet);
    }

    public void clickSlotId(int slot, int button, ClickType clickType, boolean packet) {
        if (packet) {
            mc.player.connection.sendPacket(new CClickWindowPacket(mc.player.openContainer.windowId, slot, button, clickType, ItemStack.EMPTY, mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
        } else {
            mc.playerController.windowClick(mc.player.openContainer.windowId, slot, button, clickType, mc.player);
        }
    }

    public int getPrice(ItemStack itemStack) {
        CompoundNBT tag = itemStack.getTag();
        if (tag == null) return -1;
        String price = StringUtils.substringBetween(tag.toString(), "\"text\":\" $", "\"}]");
        if (price == null || price.isEmpty()) return -1;
        price = price.replaceAll(" ", "").replaceAll(",", "");
        return Integer.parseInt(price);
    }
}
