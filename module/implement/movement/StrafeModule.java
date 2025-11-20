package relake.module.implement.movement;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import relake.Client;
import relake.common.component.rotation.FreeLookComponent;
import relake.common.component.rotation.Rotation;
import relake.common.component.rotation.RotationComponent;
import relake.common.util.*;
import relake.event.EventHandler;
import relake.event.impl.misc.PacketEvent;
import relake.event.impl.misc.TickEvent;
import relake.event.impl.player.*;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.FloatSetting;
import relake.settings.implement.SelectSetting;

import java.util.Arrays;

public class StrafeModule extends Module {

    public final SelectSetting strafeModeImpl = new SelectSetting("Обход")
            .setValue("Ванилла/матрикс",
                    "Современные ач", 
                    "StormHvH");

    
    private final Setting<Boolean> damageBoost = new BooleanSetting("Буст от урона").setValue(false).setVisible(() -> strafeModeImpl.isSelected("Ванилла/матрикс"));
    private final Setting<Float> damageSpeed = new FloatSetting("Скорость буста").range(.1F, 5.F).setValue(.5F).setVisible(() -> strafeModeImpl.isSelected("Ванилла/матрикс"));
    private final Setting<Boolean> collisionBoost = new BooleanSetting("Буст от столкновений").setValue(false).setVisible(() -> strafeModeImpl.isSelected("Современные ач"));
    private final Setting<Boolean> canInterception = new BooleanSetting("Позволить прервать ротацию").setValue(true).setVisible(() -> strafeModeImpl.isSelected("Современные ач"));

    public StrafeModule() {
        super("Strafe", "Ускоряет смену направления движения при ходьбе", "Accelerates the change of direction of movement when walking", ModuleCategory.Movement);
        registerComponent(strafeModeImpl, damageBoost, damageSpeed, collisionBoost, canInterception);
        strafeModeImpl.isSelected("StormHvH");
    }

    private final StrafeMovement strafeMovement = new StrafeMovement();
    private final DamageUtil damageUtil = new DamageUtil();
    
    @Override
    public void enable() {
        super.enable();
        strafeMovement.setOldSpeed(0);
    }

    @Override
    public void disable() {
        super.disable();
        strafeMovement.setOldSpeed(0);
    }

    @EventHandler
    public void action(ActionEvent event) {
        if (strafeModeImpl.isSelected("Ванилла/матрикс")) handleActionEvent(event);
    }

    @EventHandler
    public void move(MoveEvent event) {
        if (strafeModeImpl.isSelected("Ванилла/матрикс")) handleMoveEvent(event);
        if (strafeModeImpl.isSelected("StormHvH")) {
                if (mc.player.isOnGround()) {
                    MoveUtil.setSpeed(MoveUtil.getSpeed());
                }
                if (MoveUtil.isMoving()) {
                        if (mc.player.fallDistance > 0.67) MoveUtil.setSpeed(.35f);
                        else if (mc.player.fallDistance > 0.17) MoveUtil.setSpeed(0.4f);
                            else MoveUtil.setSpeed(0.39f);

            }
        }
    }

    @EventHandler
    public void postMove(PostMoveEvent event) {
        if (strafeModeImpl.isSelected("Ванилла/матрикс")) strafeMovement.postMove(event.getHorizontalMove());
    }

    @EventHandler
    public void packet(PacketEvent.Receive event) {
        if (strafeModeImpl.isSelected("Ванилла/матрикс")) handlePacketEvent(event);
    }

    @EventHandler
    public void damage(DamageEvent event) {
        if (strafeModeImpl.isSelected("Ванилла/матрикс")) handleDamageEvent(event);
    }

    private void handleMoveEvent(MoveEvent event) {
        if (allowStrafe()) {
            if (damageBoost.getValue()) damageUtil.time(1300L);
            final double speed = strafeMovement.calcSpeed(event, damageBoost.getValue(), damageUtil.isNormalDamage(), false, damageSpeed.getValue());
            if (MoveUtil.isMoving()) MoveUtil.setSpeed(event, speed);
        } else {
            strafeMovement.setOldSpeed(0);
        }
    }

    private void handleActionEvent(ActionEvent event) {
        if (allowStrafe()) {
            if (CEntityActionPacket.lastUpdatedSprint != strafeMovement.isNeedSprintState()) {
                event.setSprintState(!CEntityActionPacket.lastUpdatedSprint);
            }
        }
        if (strafeMovement.isNeedSwap()) {
            event.setSprintState(!mc.player.isServerSprintState());
            strafeMovement.setNeedSwap(false);
        }
    }

    private void handlePacketEvent(PacketEvent.Receive event) {
        if (damageBoost.getValue()) {
            damageUtil.processPacket(event);
        }
        if (event.getPacket() instanceof SPlayerPositionLookPacket) {
            strafeMovement.setOldSpeed(0);
        }
    }

    private void handleDamageEvent(DamageEvent event) {
        if (damageBoost.getValue()) {
            damageUtil.processDamage(event);
        }
    }

    public boolean allowStrafe() {
        if (isInvalidPlayerState()) {
            return false;
        }
        BlockPos playerPosition = new BlockPos(mc.player.getPositionVec());
        BlockPos abovePosition = playerPosition.up();
        BlockPos belowPosition = playerPosition.down();
        if (isSurfaceLiquid(abovePosition, belowPosition)) {
            return false;
        }
        if (isPlayerInWebOrSoulSand(playerPosition)) {
            return false;
        }
        return isPlayerAbleToStrafe();
    }

    private boolean isInvalidPlayerState() {
        return mc.player == null || mc.world == null
                || mc.player.isSneaking()
                || mc.player.isElytraFlying()
                || mc.player.isInWater()
                || mc.player.isInLava();
    }

    private boolean isSurfaceLiquid(BlockPos abovePosition, BlockPos belowPosition) {
        Block aboveBlock = mc.world.getBlockState(abovePosition).getBlock();
        Block belowBlock = mc.world.getBlockState(belowPosition).getBlock();

        return aboveBlock instanceof AirBlock && belowBlock == Blocks.WATER;
    }

    private boolean isPlayerInWebOrSoulSand(BlockPos playerPosition) {
        Material playerMaterial = mc.world.getBlockState(playerPosition).getMaterial();
        Block oneBelowBlock = mc.world.getBlockState(playerPosition.down()).getBlock();

        return playerMaterial == Material.WEB || oneBelowBlock instanceof SoulSandBlock;
    }

    private boolean isPlayerAbleToStrafe() {
        return !mc.player.abilities.isFlying && !mc.player.isPotionActive(Effects.LEVITATION);
    }


    private float getInputMoveYaw(float appendYaw) {
        float moveYaw = MoveUtil.moveYaw(appendYaw);
        return GCDUtil.getSensitivity(moveYaw);
    }

    private boolean canAroundRotateUpdated() {
        if (!(rageHead() && canInterception.getValue() && mc.player.movementInput.jump) && RotationComponent.currentTask != RotationComponent.RotationTask.IDLE || mc.currentScreen != null || Client.instance.moduleManager.freeCamModule.isEnabled()) return false;
        final Item[] throwableItems = new Item[]{Items.BOW, Items.TRIDENT, Items.CROSSBOW, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.ENDER_PEARL, Items.SNOWBALL, Items.FISHING_ROD, Items.EXPERIENCE_BOTTLE, Items.EGG};
        final ItemStack main = mc.player.getHeldItemMainhand(), off = mc.player.getHeldItemOffhand();
        final Item itemMain = main.getItem(), itemOff = off.getItem();
        return !mc.player.isSneaking() && Arrays.stream(throwableItems).noneMatch(item -> item == itemMain) && Arrays.stream(throwableItems).noneMatch(item -> item == itemOff);
    }

    public float moveYaw = -999;
    @EventHandler
    public void onMoveInput(MovementInputEvent event) {
        if (this.strafeModeImpl.isSelected("Современные ач") && event.hasMovement() && (callStopRotates || RotationComponent.currentTask == RotationComponent.RotationTask.IDLE) && !(Client.instance.moduleManager.speedModule.isEnabled() && Client.instance.moduleManager.speedModule.mode.isSelected("FunTime extra"))) {
            if (mc.currentScreen != null) return;
            event.setForward(1.F);
            event.setStrafe(0.F);
        }
    }

    @EventHandler
    public void onMoveKeys(MovementInputKeysEvent event) {
        /*
        if (this.moveYaw != -999 && event.hasMovementInputPressing()) {
            event.setW(true);
            event.setA(false);
            event.setS(false);
            event.setD(false);
        }
         */
    }

    @EventHandler
    public void onSprintLock(SprintLockEvent event) {
        if (this.moveYaw != -999) event.unlockSprint();
    }

    @EventHandler
    public void onMoveRotate(RotateMoveSideEvent event) {
        if (mc.currentScreen != null) return;
        if (this.moveYaw != -999 && (callStopRotates || !rageHead()) || !(Client.instance.moduleManager.speedModule.isEnabled() && Client.instance.moduleManager.speedModule.mode.isSelected("FunTime extra"))) event.setYaw(rageHead() ? mc.player.rotationYaw : this.getInputMoveYaw(FreeLookComponent.getFreeYaw()));
    }

    public boolean preGround, callStopRotates;

    private boolean rageHead() {
        return RotationComponent.currentTask != RotationComponent.RotationTask.IDLE;
    }

    @EventHandler
    public void onPreUpdatePlayer(PlayerEvent event) {
        this.callStopRotates = false;
        if (this.strafeModeImpl.isSelected("Современные ач")) {
            if (MoveUtil.isMoving() && this.canAroundRotateUpdated()) {
                this.moveYaw = this.getInputMoveYaw(FreeLookComponent.getFreeYaw());
                final boolean sendRotate = !rageHead() || mc.player.isOnGround();
                if (sendRotate) {
                    this.callStopRotates = mc.player.movementInput.jump && rageHead() && canInterception.getValue();
                    event.setYaw(this.moveYaw);
                    mc.player.rotationYawHead = this.moveYaw;
                    mc.player.renderYawOffset = this.moveYaw;
                    if (mc.player.ticksOnGround > 1) mc.player.renderYawOffset = this.moveYaw;
                }
                if (this.collisionBoost.getValue()) {
                    final double selfSpeed = MoveUtil.getSpeed();
                    final AxisAlignedBB selfAABB;
                    int collisions;
                    if (selfSpeed >= .08D && (collisions = Math.min(mc.world.getEntitiesWithinAABB(LivingEntity.class, selfAABB = mc.player.getBoundingBox().grow(.1F)).size() - mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, selfAABB).size() - 1, 5)) > 0) {
                        final double speedAppend = MathUtil.clamp(1.D - MoveUtil.getSpeed() / 1.8D, 0.D, 1.D) * (mc.player.isSprinting() ? .2F : .3F), yawRad = Math.toRadians(this.moveYaw);
                        float deltaMax = .5F * (int) (collisions * 1.999999F);
                        mc.player.addVelocity(-Math.sin(yawRad) * speedAppend * deltaMax, 0.D, Math.cos(yawRad) * speedAppend * deltaMax);
                    }
                }
                this.preGround = mc.player.isOnGround();
                return;
            }
            if (MoveUtil.isMoving()) return;
        }
        this.preGround = mc.player.isOnGround();
        this.moveYaw = -999;
    }

    @Override
    public void onDisable() {
        this.callStopRotates = false;
    }

    @Override
    public void onEnable() {
        this.callStopRotates = false;
    }
}
