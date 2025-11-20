package relake.module.implement.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.glfw.GLFW;
import relake.Client;
import relake.common.component.rotation.Rotation;
import relake.common.component.rotation.RotationComponent;
import relake.common.util.InventoryUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.FloatSetting;
import relake.settings.implement.KeySetting;
import relake.settings.implement.SelectSetting;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.IntStream;

public class ElytraTargetModule extends Module {

    public final Setting<Float> detectionRange = new FloatSetting("Диапазон обнаружения").range(10.0f, 100.0f, 1.0f).setValue(50.0f);

    private final Setting<Boolean> autoFireworkUse = new BooleanSetting("Авто фейерверк").setValue(true);

    private final Setting<Float> fireworkDelay = new FloatSetting("Задержка фейерверка").range(4.0f, 20.0f, 2.0f).setValue(6.0f).setVisible(autoFireworkUse::getValue);

    private final SelectSetting autoBounceMode = new SelectSetting("Отпрыгивать")
            .setValue("Всегда",
                    "При зажатии пробела",
                    "Не отпрыгивать");

    private final Setting<Boolean> autoDepart = new BooleanSetting("Авто отлёт").setValue(true);

    public Setting<Integer> departKey = new KeySetting("Клавиша отлёта").setValue(-1).setVisible(autoDepart::getValue);

    private final Setting<Boolean> autoDepartOnLowHP = new BooleanSetting("При низком HP").setValue(false).setVisible(autoDepart::getValue);

    private final Setting<Float> lowHPThreshold = new FloatSetting("HP для отлёта").range(1.0f, 20.0f, 1.0f).setValue(4.0f).setVisible(autoDepartOnLowHP::getValue);

    private final Setting<Boolean> autoDepartOnLowTotems = new BooleanSetting("При малом количестве тотемов").setValue(false).setVisible(autoDepart::getValue);

    private final Setting<Float> totemThreshold = new FloatSetting("Количество тотемов").range(1.0f, 36.0f, 1.0f).setValue(3.0f).setVisible(() -> autoDepart.getValue() && autoDepartOnLowTotems.getValue());

    private final Setting<Float> distanceToNearestPlayer = new FloatSetting("Дист. к ближайшему игроку").range(10.0f, 100.0f, 1.0f).setValue(30.0f).setVisible(() -> autoDepart.getValue() && (autoDepartOnLowHP.getValue() || autoDepartOnLowTotems.getValue()));

    private final SelectSetting departPitchMode = new SelectSetting("Значение Pitch")
            .setValue("0°",
                    "45°",
                    "Кастомный");

    private final Setting<Float> customPitch = new FloatSetting("Кастомный Pitch").range(-90.0f, 90.0f, 1.0f).setValue(-45.0f).setVisible(() -> autoDepart.getValue() && departPitchMode.isSelected("Кастомный"));

    private final Setting<Boolean> fireworksRefill = new BooleanSetting("Пополнение фейерверков").setValue(true);

    public ElytraTargetModule() {
        super("Elytra Target", "Агрессивно преследует противника при полёте на элитрах", "Aggressively pursues the enemy while flying on elytra", ModuleCategory.Combat);

        registerComponent(detectionRange, autoFireworkUse, fireworkDelay, autoBounceMode, autoDepart, departKey, autoDepartOnLowHP, lowHPThreshold, autoDepartOnLowTotems, totemThreshold, distanceToNearestPlayer, departPitchMode, customPitch, fireworksRefill);
        autoBounceMode.setSelected("Всегда");
        departPitchMode.setSelected("0°");
    }

    @EventHandler
    public void onTick(TickEvent event) {
        ItemStack itemStack = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST);

        if (mc.player.isInWater() || mc.player.isPotionActive(Effects.LEVITATION) || (itemStack.getItem() != Items.ELYTRA)) {
            return;
        }

        if (!autoBounceMode.isSelected("Не отпрыгивать")) {
            if (autoBounceMode.isSelected("Всегда") && mc.player.isOnGround()) {
                mc.player.jump(true);
            }

            boolean canFly = itemStack.getItem() == Items.ELYTRA && ElytraItem.isUsable(itemStack) && !mc.player.isElytraFlying();
            boolean shouldFly = (autoBounceMode.isSelected("Всегда") && !mc.player.isOnGround()) || (autoBounceMode.isSelected("При зажатии пробела") && mc.player.movementInput.jump);

            if (canFly && shouldFly) {
                mc.player.startFallFlying();
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            }
        }

        if (autoFireworkUse.getValue() && mc.player.ticksExisted % fireworkDelay.getValue() == 2 && Client.instance.moduleManager.attackAuraModule.getTarget() != null) {
            useFirework();
        }

        if (fireworksRefill.getValue()) {
            Optional<Integer> emptySlot = Optional.empty();
            Optional<Integer> fireworkSlot = Optional.empty();
            int maxFireworkCount = -1;

            for (int slot = 0; slot < mc.player.inventory.getSizeInventory(); slot++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(slot);

                if (stack.isEmpty()) {
                    if (emptySlot.isEmpty()) {
                        emptySlot = Optional.of(slot);
                    }
                } else if (stack.getItem() == Items.FIREWORK_ROCKET) {
                    int count = stack.getCount();
                    if (count > maxFireworkCount) {
                        maxFireworkCount = count;
                        fireworkSlot = Optional.of(slot);
                    }
                }

                if (emptySlot.isPresent() && fireworkSlot.isPresent()) {
                    break;
                }
            }

            if (emptySlot.isPresent() && fireworkSlot.isPresent()) {
                mc.playerController.windowClick(0, fireworkSlot.get(), emptySlot.get(), ClickType.SWAP, mc.player);
            }
        }

        int key = departKey.getValue();
        if (key <= 0 || !autoDepart.getValue()) return;

        if (mc.player.isElytraFlying()) {
            Entity nearestPlayer = getNearestPlayer();
            boolean hpCondition = autoDepartOnLowHP.getValue() && mc.player.getHealth() < lowHPThreshold.getValue() && nearestPlayer != null && mc.player.getDistance(nearestPlayer) <= distanceToNearestPlayer.getValue();
            boolean totemsCondition = autoDepartOnLowTotems.getValue() && getTotemCount() < totemThreshold.getValue().intValue() && nearestPlayer != null && mc.player.getDistance(nearestPlayer) <= distanceToNearestPlayer.getValue();
            boolean keyPressed = GLFW.glfwGetKey(mc.getMainWindow().getHandle(), departKey.getValue()) == GLFW.GLFW_PRESS;

            if (hpCondition || totemsCondition || keyPressed) {
                float pitch;
                if (departPitchMode.isSelected("0°")) {
                    pitch = 0.0f;
                } else if (departPitchMode.isSelected("45°")) {
                    pitch = 45.0f;
                } else if (departPitchMode.isSelected("Кастомный")) {
                    pitch = customPitch.getValue();
                } else {
                    pitch = 0.0f;
                }

                RotationComponent.update(new Rotation(getYaw(), pitch), 360, 360, 1, 20);

                if (mc.player.ticksExisted % fireworkDelay.getValue() == 2) {
                    useFirework();
                }
            }
        }
    }

    private int getTotemCount() {
        return IntStream.range(0, mc.player.inventory.getSizeInventory()).mapToObj(slot -> mc.player.inventory.getStackInSlot(slot)).filter(stack -> stack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum();
    }

    private float getYaw() {
        Entity nearestPlayer = getNearestPlayer();
        if (nearestPlayer == null) {
            return mc.player.rotationYaw;
        }

        Vector3d playerPos = mc.player.getPositionVec();

        return getYawAway(nearestPlayer, playerPos);
    }

    private float getYawAway(Entity nearestPlayer, Vector3d playerPos) {
        Vector3d targetPos = nearestPlayer.getPositionVec();

        double deltaX = targetPos.x - playerPos.x;
        double deltaZ = targetPos.z - playerPos.z;

        float yawToPlayer = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90F;

        float yawAway = yawToPlayer + 180F;

        yawAway = MathHelper.wrapDegrees(yawAway);
        return yawAway;
    }

    private Entity getNearestPlayer() {
        Entity nearest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity == mc.player || !(entity instanceof PlayerEntity)) continue;

            String name = entity.getNotHidedName().getString();

            if (Client.instance.friendManager.isFriend(name)) {
                continue;
            }

            double distance = mc.player.getDistanceSq(entity);
            if (distance < closestDistance) {
                closestDistance = distance;
                nearest = entity;
            }
        }

        return nearest;
    }

    private void useFirework() {
        if (!mc.player.isElytraFlying() || mc.player.isHandActive()) return;

        Optional<Integer> fireworkSlot = InventoryUtil.findItem(stack -> stack.getItem() == Items.FIREWORK_ROCKET, false, false);

        if (fireworkSlot.isEmpty()) {
            return;
        }

        int slot = fireworkSlot.get();
        if (slot < 9) {
            mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
        }
    }
}
