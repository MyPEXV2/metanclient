package relake.module.implement.combat;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.AirItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import relake.common.util.ColorUtil;
import relake.common.util.InventoryUtil;
import relake.common.util.StopWatch;
import relake.event.EventHandler;
import relake.event.impl.misc.EntitySpawnEvent;
import relake.event.impl.misc.InventoryCloseEvent;
import relake.event.impl.misc.PacketEvent;
import relake.event.impl.misc.TickEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.FloatSetting;
import relake.settings.implement.MultiSelectSetting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class AutoTotemModule extends Module {


    private final Setting<Float> health = new FloatSetting("Здоровье").range(1F, 20F, 1F).setValue(1F);

    public final Setting<Boolean> counter = new BooleanSetting("Счётчик").setValue(true);
    private final Setting<Boolean> swapBack = new BooleanSetting("Возвращать предмет").setValue(true);
    private final Setting<Boolean> saveEnchanted = new BooleanSetting("Сейвить зачарованные").setValue(true);
    private final Setting<Boolean> noBallSwitch = new BooleanSetting("Не брать при шаре").setValue(false);
    private final Setting<Boolean> absorption = new BooleanSetting("Поглощение").setValue(true);

    public final MultiSelectSetting modeReaction = new MultiSelectSetting("Брать если:")
            .setValue("Кристалл",
                    "Якорь");

    private int nonEnchantedTotems;
    private int oldItem = -1;
    public boolean isActive;
    private final StopWatch stopWatch = new StopWatch();

    private Item backItem = Items.AIR;
    private ItemStack backItemStack;

    private int itemInMouse = -1;
    private int totemCount = 0;
    private boolean totemIsUsed;


    public AutoTotemModule() {
        super("AutoTotem", "Спасает от смерти путём использования тотема бессмертия второй рукой в опасных ситуациях", "Saves from death by using the totem of immortality with the second hand in dangerous situations", ModuleCategory.Combat);
        registerComponent(health);
        registerComponent(counter);
        registerComponent(swapBack);
        registerComponent(saveEnchanted);
        registerComponent(noBallSwitch);
        registerComponent(absorption);
        registerComponent(modeReaction);
        modeReaction.getSelected().add("Кристалл");
        modeReaction.getSelected().add("Якорь");
    }

    public boolean drawCenteredIndicator(MatrixStack stackIn, float x, float y, boolean silent) {
        if (this.isEnabled() && counter.getValue()) {
            if (!silent) {
                int totems = 0;
                ItemStack temp;
                for (int i = 0; i < mc.player.inventory.getSizeInventory(); i++) {
                    temp = mc.player.inventory.getStackInSlot(i);
                    if (temp.getItem() == Items.TOTEM_OF_UNDYING) totems += temp.getCount();
                }
                final ItemRenderer itemRenderer = mc.getItemRenderer();
                x -= 8.5f;
                y -= 20;
                float offsetX = 14f;
                GL11.glTranslated(x, y, 0.D);
                ItemStack drawStackDOO = new ItemStack(Items.TOTEM_OF_UNDYING, 1);
                itemRenderer.renderItemAndEffectIntoGUI(drawStackDOO, 0, 0);
                itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, drawStackDOO, 0, 0, "");
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                mc.fontRenderer.drawStringWithShadow(stackIn, totems + "", offsetX - mc.fontRenderer.getStringWidth(totems + ""), 18.5f - 8, totems == 0 ? ColorUtil.getColor(255, 40, 40) : -1);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glTranslated(-x, -y, 0.D);
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onSpawnEntity(EntitySpawnEvent spawnEntity) {
        if (spawnEntity.getEntity() instanceof EnderCrystalEntity entity && modeReaction.isSelected("Кристалл") && !noBallSwitch.getValue()) {
            if (entity.getDistance(mc.player) <= 6.0F) {
                this.swapToTotem();
            }
        }
    }

    @EventHandler
    public void onUpdate(TickEvent event) {
        totemCount = countTotems(true);
        this.nonEnchantedTotems = (int) IntStream.range(0, 36).mapToObj((i) -> mc.player.inventory.getStackInSlot(i)).filter((s) -> s.getItem() == Items.TOTEM_OF_UNDYING && !s.isEnchanted()).count();

        int slot = getSlotInInventory(Items.TOTEM_OF_UNDYING);

        boolean handNotNull = !(mc.player.getHeldItemOffhand().getItem() instanceof AirItem);

        if (shouldToSwapTotem()) {
            if (slot != -1 && !isTotemInHands()) {
                InventoryUtil.moveItem(slot, 45, handNotNull);
                if (handNotNull && oldItem == -1) {
                    oldItem = slot;
                }
            }
        } else if (oldItem != -1 && swapBack.getValue() && !(mc.player.getActiveHand() == Hand.MAIN_HAND && mc.player.getActiveItemStack().getItem().isFood())) {
            InventoryUtil.moveItem(oldItem, 45, handNotNull);
            oldItem = -1;
        }
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SEntityStatusPacket statusPacket
                && statusPacket.getOpCode() == 35 && statusPacket.getEntity(mc.world) == mc.player) {
            this.totemIsUsed = true;
        }
    }

    private void swapBack() {
        if (this.stopWatch.finished(400) && this.itemIsBack()) {
            this.itemInMouse = -1;
            this.backItem = Items.AIR;
            this.backItemStack = null;
            this.stopWatch.reset();
        }
    }

    private boolean itemIsBack() {
        if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING && this.itemInMouse != -1 && this.backItem != Items.AIR) {
            ItemStack itemStack = mc.player.container.getSlot(this.itemInMouse).getStack();
            boolean offHandAreEqual = itemStack != ItemStack.EMPTY && !ItemStack.areItemStacksEqual(itemStack, this.backItemStack);
            int oldItem = findItemSlotIndex(backItemStack, backItem);

            if (oldItem < 9 && oldItem != -1) {
                oldItem = oldItem + 36;
            }


            int containerId = mc.player.container.windowId;

            if (mc.player.inventory.getItemStack().getItem() != Items.AIR) {
                mc.playerController.windowClick(containerId, 45, 0, ClickType.PICKUP, mc.player);
                this.backItemInMouse();
                return false;
            }

            if (oldItem == -1) {
                return false;
            }

            mc.playerController.windowClick(containerId, oldItem, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(containerId, 45, 0, ClickType.PICKUP, mc.player);
            if (this.itemInMouse != -1) {
                if (!offHandAreEqual) {
                    mc.playerController.windowClick(containerId, this.itemInMouse, 0, ClickType.PICKUP, mc.player);
                } else {
                    int emptySlot = getEmptySlot(false);
                    if (emptySlot != -1) {
                        mc.playerController.windowClick(containerId, emptySlot, 0, ClickType.PICKUP, mc.player);
                    }
                }
            }
        }
        return true;
    }

    public static int getEmptySlot(boolean hotBar) {
        for (int i = hotBar ? 0 : 9; i < (hotBar ? 9 : 45); ++i) {
            if (!mc.player.inventory.getStackInSlot(i).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    public int findItemSlotIndex(ItemStack targetItemStack, Item targetItem) {
        if (targetItemStack == null) {
            return -1;
        }

        for (int i = 0; i < 45; ++i) {
            ItemStack currentStack = mc.player.inventory.getStackInSlot(i);

            if (ItemStack.areItemStacksEqual(currentStack, targetItemStack) && currentStack.getItem() == targetItem) {
                return i;
            }
        }

        return -1;
    }


    public boolean itemIsHand(Item item) {
        for (Hand enumHand : Hand.values()) {
            if (mc.player.getHeldItem(enumHand).getItem() != item) continue;
            return true;
        }
        return false;
    }

    private void swapToTotem() {
        int totemSlot = getSlotInInventory(Items.TOTEM_OF_UNDYING);
        this.stopWatch.reset();
        Item mainHandItem = mc.player.getHeldItemOffhand().getItem();

        if (mainHandItem == Items.TOTEM_OF_UNDYING) {
            return;
        }

        if (totemSlot == -1 && !isCurrentItem(Items.TOTEM_OF_UNDYING)) {
            return;
        }

        if (this.itemInMouse == -1) {
            this.itemInMouse = totemSlot;
            this.backItem = mainHandItem;
            this.backItemStack = mc.player.getHeldItemOffhand().copy();
        }

        short short1 = mc.player.openContainer.getNextTransactionID(mc.player.inventory);
        ItemStack itemstack = mc.player.openContainer.slotClick(totemSlot, 1, ClickType.PICKUP, mc.player);

        short short2 = mc.player.openContainer.getNextTransactionID(mc.player.inventory);
        ItemStack itemstack1 = mc.player.openContainer.slotClick(45, 1, ClickType.PICKUP, mc.player);

        final List<KeyBinding> binds = List.of(
                mc.gameSettings.keyBindForward,
                mc.gameSettings.keyBindBack,
                mc.gameSettings.keyBindLeft,
                mc.gameSettings.keyBindRight,
                mc.gameSettings.keyBindJump,
                mc.gameSettings.keyBindSprint
        );

        for (KeyBinding binding : binds) {
            binding.setPressed(InputMappings.isKeyDown(mw.getHandle(), binding.keyCode.getKeyCode()));
        }

        if (packet.isEmpty()) {
            packet.add(new CClickWindowPacket(mc.player.container.windowId, totemSlot, 1, ClickType.PICKUP, itemstack, short1));
            packet.add(new CClickWindowPacket(mc.player.container.windowId, 45, 1, ClickType.PICKUP, itemstack1, short2));
        }
    }

    private final List<IPacket<?>> packet = new ArrayList<>();
    private final StopWatch stopWatch2 = new StopWatch();

    @EventHandler
    public void inventory(InventoryCloseEvent event) {
        if (!packet.isEmpty()) {
            new Thread(() -> {
                stopWatch2.reset();

                try {
                    Thread.sleep(300);
                } catch (Exception ignored) {
                }

                mc.displayGuiScreen(new InventoryScreen(mc.player));

                for (IPacket<?> p : packet) {
                    mc.player.connection.sendPacketWOEvent(p);
                }

                if (this.totemCount > 1 && this.totemIsUsed) {
                    this.backItemInMouse();

                    this.totemIsUsed = false;
                }

                this.backItemInMouse();

                mc.player.closeScreen();

                packet.clear();
            }).start();
            event.cancel();
        }
    }

    public int countTotems(boolean includeEnchanted) {
        long totemCount = 0L;
        int inventorySize = mc.player.inventory.getSizeInventory();

        for (int slotIndex = 0; slotIndex < inventorySize; ++slotIndex) {
            ItemStack slotStack = mc.player.inventory.getStackInSlot(slotIndex);

            if (slotStack.getItem() == Items.TOTEM_OF_UNDYING && (includeEnchanted || !slotStack.isEnchanted())) {
                ++totemCount;
            }
        }

        return (int) totemCount;
    }

    private void backItemInMouse() {
        if (this.itemInMouse != -1) {
            mc.playerController.windowClick(mc.player.container.windowId, this.itemInMouse, 0, ClickType.PICKUP, mc.player);
        }
    }

    public static boolean isCurrentItem(Item item) {
        return mc.player.inventory.getItemStack().getItem() == item;
    }

    private boolean isTotemInHands() {
        Hand[] hands = Hand.values();

        for (Hand hand : hands) {
            ItemStack heldItem = mc.player.getHeldItem(hand);
            if (heldItem.getItem() == Items.TOTEM_OF_UNDYING && !this.isSaveEnchanted(heldItem)) {
                return true;
            }
        }

        return false;
    }

    private boolean isSaveEnchanted(ItemStack itemStack) {
        return this.saveEnchanted.getValue() && itemStack.isEnchanted() && this.nonEnchantedTotems > 0;
    }

    private boolean shouldToSwapTotem() {
        final float absorptionAmount = mc.player.isPotionActive(Effects.ABSORPTION) ? mc.player.getAbsorptionAmount() : 0.0f;
        float currentHealth = mc.player.getHealth();

        if (absorption.getValue()) {
            currentHealth += absorptionAmount;
        }

        if (!isOffhandItemBall()) {
            if (isInDangerousSituation()) {
                return true;
            }
        }

        return currentHealth <= this.health.getValue();
    }

    private boolean isInDangerousSituation() {
        return checkCrystal() || checkAnchor();
    }

    private boolean checkAnchor() {
        if (!modeReaction.isSelected("Якорь"))
            return false;

        return getBlock(6.0F, Blocks.RESPAWN_ANCHOR) != null;
    }


    private boolean checkCrystal() {
        if (!modeReaction.isSelected("Кристалл")) {
            return false;
        }

        for (Entity entity : mc.world.getAllEntities()) {
            if (isDangerousEntityNearPlayer(entity)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOffhandItemBall() {
        return this.noBallSwitch.getValue() && mc.player.getHeldItemOffhand().getItem() == Items.PLAYER_HEAD;
    }

    private boolean isDangerousEntityNearPlayer(Entity entity) {
        return (entity instanceof TNTEntity || entity instanceof TNTMinecartEntity || entity instanceof EnderCrystalEntity) && mc.player.getDistance(entity) <= 6.0F;
    }

    private BlockPos getBlock(float distance, Block block) {
        return getSphere(getPlayerPosLocal(), distance, 6, false, true, 0).stream().filter(position -> mc.world.getBlockState(position).getBlock() == block).min(Comparator.comparing(blockPos -> getDistanceOfEntityToBlock(mc.player, blockPos))).orElse(null);
    }

    private List<BlockPos> getSphere(final BlockPos center, final float radius, final int height, final boolean hollow, final boolean fromBottom, final int yOffset) {
        List<BlockPos> positions = new ArrayList<>();
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();

        for (int x = centerX - (int) radius; x <= centerX + radius; x++) {
            for (int z = centerZ - (int) radius; z <= centerZ + radius; z++) {
                int yStart = fromBottom ? (centerY - (int) radius) : centerY;
                int yEnd = fromBottom ? (centerY + (int) radius) : (centerY + height);

                for (int y = yStart; y < yEnd; y++) {
                    if (isPositionWithinSphere(centerX, centerY, centerZ, x, y, z, radius, hollow)) {
                        positions.add(new BlockPos(x, y + yOffset, z));
                    }
                }
            }
        }

        return positions;
    }

    private BlockPos getPlayerPosLocal() {
        if (mc.player == null) {
            return BlockPos.ZERO;
        }
        return new BlockPos(Math.floor(mc.player.getPosX()), Math.floor(mc.player.getPosY()), Math.floor(mc.player.getPosZ()));
    }

    private double getDistanceOfEntityToBlock(final Entity entity, final BlockPos blockPos) {
        return getDistance(entity.getPosX(), entity.getPosY(), entity.getPosZ(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }


    private double getDistance(final double n, final double n2, final double n3, final double n4, final double n5, final double n6) {
        final double n7 = n - n4;
        final double n8 = n2 - n5;
        final double n9 = n3 - n6;
        return MathHelper.sqrt(n7 * n7 + n8 * n8 + n9 * n9);
    }

    private static boolean isPositionWithinSphere(int centerX, int centerY, int centerZ, int x, int y, int z, float radius, boolean hollow) {
        double distanceSq = Math.pow(centerX - x, 2) + Math.pow(centerZ - z, 2) + Math.pow(centerY - y, 2);
        return distanceSq < Math.pow(radius, 2) && (!hollow || distanceSq >= Math.pow(radius - 1.0f, 2));
    }

    public int getSlotInInventory(Item item) {
        int slot = -1;

        for (int i = 0; i < 36; ++i) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (itemStack.getItem() == Items.TOTEM_OF_UNDYING && !this.isSaveEnchanted(itemStack)) {
                slot = adjustSlotNumber(i);
                break;
            }
        }

        return slot;
    }

    private int adjustSlotNumber(int slot) {
        return slot < 9 ? slot + 36 : slot;
    }

    private void reset() {
        this.oldItem = -1;
    }

    @Override
    public void disable() {
        super.disable();
        reset();
    }
}
