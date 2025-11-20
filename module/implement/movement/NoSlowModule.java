package relake.module.implement.movement;

import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.*;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import relake.common.util.MoveUtil;
import relake.common.util.ServerUtil;
import relake.common.util.StopWatch;
import relake.event.EventHandler;
import relake.event.impl.misc.SlowItemEvent;
import relake.event.impl.player.PlayerEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.SelectSetting;

public class NoSlowModule extends Module {

    public final SelectSetting mode = new SelectSetting("Обход")
            .setValue("Ванильный",
                    "Grim",
                    "FunTime в Воде",
                    "FunTime на Земле",
                    "FT на Земле и в Воде",
                    "ReallyWorld");

    public NoSlowModule() {
        super("No Slow", "Убирает или ослабляет замедление при поедании или зажатии в щите", "Removes or weakens deceleration when eating or trapped in a shield", ModuleCategory.Movement);
        registerComponent(mode);
        mode.setSelected("FunTime на Земле");
    }
    private boolean pressShift;
    private final StopWatch stopWatch = new StopWatch();

    @EventHandler
    public void onItemUse(SlowItemEvent event) {
        if (mode.isSelected("Ванильный")) event.cancel();
        if (mode.isSelected("Grim")) handleGrim(event);
        if (mode.isSelected("FunTime в Воде") || mode.isSelected("FT на Земле и в Воде")) handleFunTimeWater(event);
        if (mode.isSelected("FunTime на Земле") || mode.isSelected("FT на Земле и в Воде")) handleFunTimeGround(event);
        if (mode.isSelected("ReallyWorld")) handleReallyWorld(event);
    }

    @EventHandler
    public void onUpdate(PlayerEvent event) {
        if (mode.isSelected("FunTime на Земле") || mode.isSelected("FT на Земле и в Воде")) {
            handleFunTimeGroundUpdate(event);
        }
    }

    private void handleGrim(SlowItemEvent event) {
        if (mc.player.getHeldItemOffhand().getUseAction() == UseAction.BLOCK && mc.player.getActiveHand() == Hand.MAIN_HAND || mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT && mc.player.getActiveHand() == Hand.MAIN_HAND) {
            return;
        }
        mc.playerController.syncCurrentPlayItem();

        if (mc.player.getActiveHand() == Hand.MAIN_HAND) {
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            event.cancel();
            return;
        }

        event.cancel();

        swapItems();
    }

    private void handleFunTimeWater(SlowItemEvent event) {
        if (mc.player.isSwimming()) {
            event.cancel();
        }
    }

    private void handleFunTimeGround(SlowItemEvent event) {
        if (pressShift && !isBlockUnderWithMotion() && !mc.player.movementInput.jump && !mc.player.isPotionActive(Effects.SLOWNESS)) {
            float speed = mc.player.isPotionActive(Effects.SPEED) ? 0.3F : 0.255F;
            MoveUtil.setSpeed(speed);
        }
    }
    private void handleFunTimeGroundUpdate(PlayerEvent event) {
        if (mc.player.isHandActive() && mc.player.isOnGround() && !mc.player.movementInput.jump && !isBlockUnderWithMotion() && (ServerUtil.isFT() || ServerUtil.isHW())) {
                if (ServerUtil.isHW()) new CPlayerPacket.RotationPacket(event.getRotate().x, 90, event.isOnGround());
                final AxisAlignedBB boundingBox = mc.player.getBoundingBox().expand(0.5, -0.01, 0.5);
                int minX = MathHelper.floor(boundingBox.minX), minY = MathHelper.floor(boundingBox.minY), minZ = MathHelper.floor(boundingBox.minZ);
                int maxX = MathHelper.floor(boundingBox.maxX), maxY = MathHelper.floor(boundingBox.maxY), maxZ = MathHelper.floor(boundingBox.maxZ);
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        for (int z = minZ; z <= maxZ; z++) {
                            BlockPos pos = new BlockPos(x, y, z);
                            if (!mc.world.getBlockState(pos).isAir()) new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP).send();
                        }
                    }
                }
            new CEntityActionPacket(mc.player, CEntityActionPacket.Action.PRESS_SHIFT_KEY).send();
            pressShift = true;
        } else if (pressShift) {
            new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY).send();
            pressShift = false;
        }
    }

    public boolean isBlockUnderWithMotion() {
        AxisAlignedBB aab = mc.player.getBoundingBox().offset(mc.player.getMotion().x / 2, -0.1, mc.player.getMotion().z / 2);
        return mc.world.getCollisionShapes(mc.player, aab).toList().isEmpty();
    }

    private void handleReallyWorld(SlowItemEvent event) {
        if (!(mc.player.getItemInUseCount() < 30 && mc.player.getItemInUseCount() > 5) && (mc.player.getFoodStats().getFoodLevel() > 6.0F) && mc.player.getHeldItemOffhand().getItem() != Items.SHIELD) {
            return;
        }
        if (stopWatch.finished(60)) {
            if (mc.player.isHandActive() && !mc.player.isPassenger()) {
                mc.playerController.syncCurrentPlayItem();
                if (mc.player.activeHand == Hand.OFF_HAND && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemOffhand().getItem())) {
                    swapItems();
                    event.cancel();
                }
                if (mc.player.activeHand == Hand.MAIN_HAND && !mc.player.getCooldownTracker().hasCooldown(mc.player.getHeldItemMainhand().getItem())) {
                    if (mc.player.getHeldItemOffhand().getUseAction().equals(UseAction.NONE)) {
                        event.cancel();
                    }
                }
                mc.playerController.syncCurrentPlayItem();
            }
            stopWatch.reset();
        }

    }

    private void swapItems() {
        if (MoveUtil.isMoving()) {
            mc.player.connection.sendPacket(new CHeldItemChangePacket((mc.player.inventory.currentItem + 1) % 9));
            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
        }
    }
}
