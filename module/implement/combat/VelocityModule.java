package relake.module.implement.combat;

import com.sun.jna.platform.unix.X11;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector3d;
import relake.common.util.MathUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.PacketEvent;
import relake.event.impl.player.MoveEvent;
import relake.event.impl.player.MovementInputEvent;
import relake.event.impl.player.PlayerEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.SelectSetting;

public class VelocityModule extends Module {

    private final SelectSetting mode = new SelectSetting("Обход")
            .setValue(

                    "Ванильный",
                    "FunTime"

            );

    public VelocityModule() {


        super("Velocity", "Блокирует или уменьшает откидывание от всяческого урона", "Blocks or reduces recoil from all kinds of damage", ModuleCategory.Combat);
        registerComponent(mode);
        mode.setSelected("Ванильный");
    }

    @EventHandler
    public void packetEvent(PacketEvent.Receive packetEvent) {
        if (mode.isSelected("Ванильный")) {
            if (packetEvent.getPacket() instanceof SEntityVelocityPacket packet) {
                if (packet.getEntityID() == mc.player.getEntityId()) packetEvent.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void movement(MovementInputEvent event) {
        if (mode.isSelected("FunTime")) {
            boolean canVelocityDamage = mc.player.hurtTime != 0 && !mc.player.isInWater()
                    && !mc.player.isInLava() && !mc.player.isEating() && !mc.player.isElytraFlying() && !mc.player.isBurning() && !mc.player.isPotionActive(Effects.POISON);
            if (canVelocityDamage) {
                float prevJump = mc.player.jumpMovementFactor;
                mc.player.jumpMovementFactor = 1;
                mc.player.jumpTicks = 0;

                if (mc.player.isOnGround() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.player.jump();
                }

                boolean prevSprint = mc.player.isSprinting();
                mc.player.setSprinting(true);
                event.setForward(1F);
                mc.player.setSprinting(prevSprint);
                mc.player.jumpMovementFactor = prevJump;
            }
        }
    }
}