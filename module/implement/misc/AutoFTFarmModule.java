package relake.module.implement.misc;

import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import relake.Client;
import relake.common.util.*;
import relake.event.EventHandler;
import relake.event.impl.player.ChatEvent;
import relake.event.impl.player.PlayerEvent;
import relake.module.Module;
import relake.module.ModuleCategory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class AutoFTFarmModule extends Module {
    public AutoFTFarmModule() {
        super("Auto FT Farm", "Автоматически выращивает и продаёт картошку или морковь, сам чинит инструмены, предназначен для заработка на сервере FunTime", "It automatically grows and sells potatoes or carrots, repairs tools, and is designed to earn money on the FunTime server", ModuleCategory.Misc);
    }

    private final StopWatch stopWatchMain = new StopWatch();
    private final StopWatch stopWatch = new StopWatch();
    private boolean autoRepair, expValid;
    private final int MIN_PRICE = 99999;
    private final int MAX_PRICE = 297000;
    private final StopWatch slot49Timer = new StopWatch();
    public void toggle() {
        autoRepair = false;
        expValid = false;
    }



    @EventHandler
    public void onMessage(ChatEvent e) {
        if (e.getType() == ChatEvent.EventType.RECEIVE) {
            String msg = e.getMessage();
            if (msg.contains("[☃] У Вас не хватает денег!")) {
                stopWatch.reset();
            }
        }
    }

    @EventHandler
    public void onUpdate(PlayerEvent e) {
        if (ServerUtil.isFT()) {
            mc.player.rotationPitch = 90;
            List<Item> hoeItems = List.of(Items.NETHERITE_HOE, Items.DIAMOND_HOE);
            List<Item> plantsItems = List.of(Items.CARROT, Items.POTATO);
            Slot expSlot = InventoryUtil.getInventorySlot(Items.EXPERIENCE_BOTTLE);
            Slot plantSlot = InventoryUtil.getInventorySlot(plantsItems);
            Slot hoeSlot = InventoryUtil.getInventorySlot(hoeItems);

            int expCount = InventoryUtil.getInventoryCount(Items.EXPERIENCE_BOTTLE);

            Item mainHandItem = mc.player.getHeldItemMainhand().getItem();
            Item offHandItem = mc.player.getHeldItemOffhand().getItem();

            if (hoeSlot == null || MoveUtil.isMoving() || !stopWatchMain.finished(500)) return;

            if (Client.instance.moduleManager.autoEatModule.isEnabled() && Client.instance.moduleManager.autoEatModule.isEating) {
                return;
            }

            float itemStrength = 1 - MathUtil.clamp((float) hoeSlot.getStack().getDamage() / (float) hoeSlot.getStack().getMaxDamage(), 0, 1);

            if (itemStrength < 0.05) {
                autoRepair = true;
            } else if (itemStrength == 1 && autoRepair) {
                stopWatchMain.reset();
                autoRepair = false;
                expValid = false;
                return;
            }

            expValid = expCount >= 32 || expCount != 0 && expValid;

            if (mc.player.inventory.getFirstEmptyStack() == -1) {
                if (!plantsItems.contains(offHandItem)) {
                    InventoryUtil.clickSlot(plantSlot, 40, ClickType.SWAP, false);
                    return;
                }
                if (mc.currentScreen instanceof ContainerScreen<?> screen) {
                    if (screen.getTitle().getString().equals("● Выберите секцию")) {
                        InventoryUtil.clickSlotId(21, 0, ClickType.PICKUP, true);
                        return;
                    }

                    if (screen.getTitle().getString().equals("Скупщик еды")) {
                        InventoryUtil.clickSlotId(offHandItem.equals(Items.CARROT) ? 10 : 11, 0, ClickType.PICKUP, true);
                        mc.player.closeScreen();
                        return;
                    }
                }

                if (stopWatch.finished(1000)) {
                    mc.player.sendChatMessage("/buyer");
                    stopWatch.reset();
                }

            } else if (autoRepair) {
                if (expValid) {
                    if (mc.currentScreen instanceof ContainerScreen<?>) {
                        mc.player.closeScreen();
                        stopWatchMain.reset();
                        return;
                    }

                    if (!offHandItem.equals(Items.EXPERIENCE_BOTTLE)) {
                        InventoryUtil.clickSlot(expSlot, 40, ClickType.SWAP, false);
                    }

                    if (!hoeItems.contains(mainHandItem)) {
                        InventoryUtil.clickSlot(hoeSlot, mc.player.inventory.currentItem, ClickType.SWAP, false);
                    }

                    new CPlayerTryUseItemPacket(Hand.OFF_HAND).send();

                } else if (stopWatch.finished((800))) {
                    if (mc.currentScreen instanceof ContainerScreen<?> screen) {
                        if (screen.getTitle().getString().contains("Пузырек опыта")) {
                            mc.player.openContainer.inventorySlots.stream()
                                    .filter(s -> s.getStack().getTag() != null && s.slotNumber < 45 && s.getStack().getCount() >= 64)
                                    .filter(s -> {
                                        int price = InventoryUtil.getPrice(s.getStack());
                                        return price >= MIN_PRICE && price <= MAX_PRICE;
                                    })
                                    .min(Comparator.comparingInt(s -> InventoryUtil.getPrice(s.getStack()) / s.getStack().getCount()))
                                    .ifPresent(s -> InventoryUtil.clickSlot(s, 0, ClickType.QUICK_MOVE, true));
                            stopWatch.reset();
                            return;
                        }

                        else if (screen.getTitle().getString().contains("Подозрительная цена")) {
                            InventoryUtil.clickSlotId(0, 0, ClickType.QUICK_MOVE, true);
                            stopWatch.reset();
                            return;
                        }
                    }

                    mc.player.sendChatMessage("/ah search Пузырек опыта");
                    System.out.println("mc.player.sendChatMessage(\"/ah search опыт\");");
                    stopWatch.reset();
                }

                if (slot49Timer.finished(1500)) {
                    InventoryUtil.clickSlotId(49, 0, ClickType.PICKUP, true);
                    slot49Timer.reset();
                }
            } else {
                BlockPos pos = mc.player.getPosition();
                if (mc.world.getBlockState(pos).getBlock().equals(Blocks.FARMLAND)) {
                    if (hoeItems.contains(mainHandItem) && plantsItems.contains(offHandItem)) {
                        new CPlayerTryUseItemOnBlockPacket(Hand.OFF_HAND, new BlockRayTraceResult(mc.player.getPositionVec(), Direction.UP, pos, false)).send();
                        IntStream.range(0, 3).forEach(i -> new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(mc.player.getPositionVec(), Direction.UP, pos.up(), false)).send());
                        new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, pos.up(), Direction.UP).send();

                    } else {
                        if (mc.currentScreen instanceof ContainerScreen<?>) {
                            mc.player.closeScreen();
                            stopWatchMain.reset();
                            return;
                        }

                        if (!plantsItems.contains(offHandItem)) {
                            InventoryUtil.clickSlot(plantSlot, 40, ClickType.SWAP, false);
                        }

                        if (!hoeItems.contains(mainHandItem)) {
                            InventoryUtil.clickSlot(hoeSlot, mc.player.inventory.currentItem, ClickType.SWAP, false);
                        }

                    }
                }
            }
        }
    }
}