package relake.module.implement.misc;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.glfw.GLFW;
import relake.common.util.StopWatch;
import relake.event.EventHandler;
import relake.event.impl.misc.KeyboardEvent;
import relake.event.impl.misc.TickEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.FloatSetting;
import relake.settings.implement.KeySetting;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class ChestStealerModule extends Module {
    private final Setting<Float> delay = new FloatSetting("Задержка")
            .range(1F, 100f)
            .setValue(15f);

    private final Setting<Boolean> lootOnlyExpensive = new BooleanSetting("Лутать только ценное").setValue(true);
    private final Setting<Boolean> missSlot = new BooleanSetting("Промахиваться").setValue(true);

    public Setting<Integer> openChest = new KeySetting("Кнопка открытия").setValue(-1);

    public ChestStealerModule() {
        super("Chest Stealer", "Забирает всё содержимое сундука при его открытии", "Takes all the contents of the chest when it is opened", ModuleCategory.Misc);
        registerComponent(delay, lootOnlyExpensive, missSlot, openChest);
    }
    private final StopWatch stopWatch = new StopWatch();

    private final List<Item> ingotItemList = List.of(Items.IRON_INGOT,
            Items.GOLD_INGOT,
            Items.NETHERITE_INGOT,
            Items.DIAMOND,
            Items.NAUTILUS_SHELL,
            Items.NETHERITE_SCRAP);

    private final Set<Block> acceptableBlocks = Set.of(
            Blocks.CHEST,
            Blocks.ENDER_CHEST,
            Blocks.BARREL,
            Blocks.SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX,
            Blocks.LIGHT_GRAY_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX,
            Blocks.ORANGE_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.PURPLE_SHULKER_BOX,
            Blocks.RED_SHULKER_BOX,
            Blocks.WHITE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX,
            Blocks.RESPAWN_ANCHOR
    );

    @EventHandler
    public void tick(TickEvent tickEvent) {
        if (mc.player.openContainer instanceof ChestContainer container) {
            IInventory lowerChestInventory = container.getLowerChestInventory();
            for (int index = 0; index < lowerChestInventory.getSizeInventory(); ++index) {
                ItemStack stack = lowerChestInventory.getStackInSlot(index);
                if (!shouldMoveItem(container, index)) {
                    continue;
                }

                if (!isWhiteListItem(stack) && lootOnlyExpensive.getValue()) {
                    return;
                }

                if (stopWatch.finished(Duration.ofMillis(delay.getValue().longValue()))) {
                    mc.playerController.windowClick(container.windowId, index, 0, ClickType.QUICK_MOVE, mc.player);
                    stopWatch.reset();
                }
                if (missSlot.getValue()) {
                    missSlots(container);
                }
            }
        }
    }
    private void missSlots(ChestContainer container) {
        int containerSize = container.getLowerChestInventory().getSizeInventory();

        for (int index = 0; index < containerSize; ++index) {
            if (container.getLowerChestInventory().getStackInSlot(index).isEmpty()) {
                if (ThreadLocalRandom.current().nextDouble() < 0.1 && mc.player.ticksExisted % 30 == 0) {
                    mc.playerController.windowClick(container.windowId, index, 0, ClickType.PICKUP, mc.player);
                    return;
                }
            }
        }
    }

    private boolean shouldMoveItem(ChestContainer container, int index) {
        ItemStack itemStack = container.getLowerChestInventory().getStackInSlot(index);
        return itemStack.getItem() != Item.getItemById(0);
    }

    public boolean isWhiteListItem(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return (itemStack.isFood()
                || itemStack.isEnchanted()
                || ingotItemList.contains(item)
                || item == Items.PLAYER_HEAD
                || item instanceof ArmorItem
                || item instanceof EnderPearlItem
                || item instanceof SwordItem
                || item instanceof ToolItem
                || item instanceof PotionItem
                || item instanceof ArrowItem
                || item instanceof SkullItem
                || item instanceof DyeItem
                || item.getGroup() == ItemGroup.COMBAT
        );
    }

    private BlockPos findNearestChest() {
        int radius = 5;
        BlockPos playerPos = mc.player.getPosition();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (acceptableBlocks.contains(mc.world.getBlockState(pos).getBlock())) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private void openNearestChest() {
        BlockPos chestPos = findNearestChest();
        if (chestPos != null) {
            mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockRayTraceResult(new Vector3d(0.5, 0.5, 0.5), Direction.UP, chestPos, false));
        }
    }
    @EventHandler
    public void keyboard(KeyboardEvent event) {
        if (event.getKey() == openChest.getValue() && event.getAction() == GLFW.GLFW_PRESS) {
         openNearestChest();
        }
    }
    @Override
    public void enable() {
        super.enable();
    }
}
