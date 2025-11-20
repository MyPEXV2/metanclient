package relake.module.implement.movement;

import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import relake.Client;
import relake.common.util.MoveUtil;
import relake.common.util.StrafeMovement;
import relake.event.EventHandler;
import relake.event.impl.misc.PacketEvent;
import relake.event.impl.misc.TickEvent;
import relake.event.impl.player.*;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.FloatSetting;

public class TargetStrafeModule extends Module {

    public final Setting<Float> speedAddSetting = new FloatSetting("Добавить к скорости").range(.0F, 1.F, .05F).setValue(0.F);
    public final Setting<Float> distanceSetting = new FloatSetting("Дистанция").range(.1F, 5.F, .1F).setValue(1.F);
    public final Setting<Boolean> saveTarget = new BooleanSetting("Сохранять цель").setValue(true);
    public final Setting<Boolean> autoJump = new BooleanSetting("Авто прыжок").setValue(true);
    private byte side = 1;
    private LivingEntity target = null;
    private final StrafeMovement strafeMovement;

    public TargetStrafeModule() {
        super("Target Strafe", "Позволяет летать как с ракетой в заднице", "Allows you to fly like a rocket in the ass", ModuleCategory.Movement);
        registerComponent(speedAddSetting, distanceSetting, saveTarget, autoJump);
        strafeMovement = new StrafeMovement();
    }

    @EventHandler
    public void onActionEvent(ActionEvent event) {
        if (mc.player == null || !targetIsValid()) return;
        if (CEntityActionPacket.lastUpdatedSprint != strafeMovement.isNeedSwap()) event.setSprintState(!CEntityActionPacket.lastUpdatedSprint);
        if (strafeMovement.isNeedSwap()) {
            event.setSprintState(!mc.player.isServerSprintState());
            strafeMovement.setNeedSprintState(false);
        }
    }

    @EventHandler
    public void onTickEvent(TickEvent event) {
        if (!canBeStrafe()) {
            target = null;
            strafeMovement.setOldSpeed(0.D);
            return;
        }
        final LivingEntity closestTarget = Client.instance.moduleManager.attackAuraModule.getTarget();
        boolean saveTarget = this.saveTarget.getValue() && Client.instance.moduleManager.attackAuraModule.isEnabled();
        if (!saveTarget || target == null || mc.world.getEntityByID(target.getEntityId()) == null) target = closestTarget;
        if (!targetIsValid()) return;
        if (mc.player.collidedHorizontally) side *= -1;
    }

    @EventHandler
    public void onMoveEvent(MoveEvent event) {
        if (!targetIsValid()) return;
        double strafeTargetDistance = distanceSetting.getValue();
        double angle = Math.atan2(mc.player.getPosZ() - target.getPosZ(), mc.player.getPosX() - target.getPosX());
        angle += MoveUtil.getSpeed() / Math.max(mc.player.getDistance(target), .1F) * side;
        final double x = target.getPosX() + Math.cos(angle) * strafeTargetDistance,z = target.getPosZ() + Math.sin(angle) * strafeTargetDistance, yawRadian = Math.toRadians(getYawToLiving(mc.player, x, z));
        final double calcStrafeMotion = strafeMovement.calcSpeed(event, false, false, autoJump.getValue(), 0.F) + speedAddSetting.getValue();
        //movement
        event.getMotion().x = (calcStrafeMotion * -Math.sin(yawRadian));
        event.getMotion().z = (calcStrafeMotion * Math.cos(yawRadian));
    }

    @EventHandler
    public void onPostMoveEvent(PostMoveEvent e) {
        if (mc.player == null || mc.world == null || !targetIsValid()) return;
        strafeMovement.postMove(e.getHorizontalMove() - this.speedAddSetting.getValue());
    }

    @EventHandler
    public void onPacketEvent(PacketEvent.Receive e) {
        if (e.getPacket() instanceof SPlayerPositionLookPacket packet && mc.player != null && mc.player.getDistanceToCoord(packet.getX(), packet.getY(), packet.getZ()) < 64.D && mc.player.getDistanceToCoord(packet.getX(), packet.getY(), packet.getZ()) >= 1E-3D && targetIsValid()) strafeMovement.setOldSpeed(0.D);
    }

    @EventHandler
    public void onInputKeysEvent(MovementInputKeysEvent event) {
        if (!targetIsValid()) return;
        if (autoJump.getValue()) event.setSpace(true);
        if (event.isA()) side = 1;
        else if (event.isD()) side = -1;
    }

    @Override
    public void onDisable() {
        strafeMovement.setOldSpeed(0.D);
        target = null;
        super.onDisable();
    }

    @Override
    public void onEnable() {
        strafeMovement.setOldSpeed(0.D);
        target = null;
        super.onEnable();
    }

    private double getYawToLiving(LivingEntity entity, double x, double z) {return Math.toDegrees(Math.atan2(z - entity.getPosZ(), x - entity.getPosX())) - 90F;}

    public boolean canBeStrafe() {
        if (
            mc.player == null ||
            mc.world == null ||
            mc.player.isElytraFlying() ||
            mc.player.isMaterialInBB(Material.WATER) ||
            mc.player.isMaterialInBB(Material.LAVA) ||
            mc.player.isMaterialInBB(Material.WEB) ||
            mc.world.getBlockState(mc.player.getPosition().down()).getBlock() instanceof SoulSandBlock
        ) return false;
        return !mc.player.abilities.isFlying && !mc.player.isPotionActive(Effects.LEVITATION) && !mc.player.isPotionActive(Effects.SLOW_FALLING);
    }

    private boolean targetIsValid() {return target != null && target.isAlive() && target.getHealth() > 0;}
}
