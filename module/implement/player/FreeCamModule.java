package relake.module.implement.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import relake.common.util.MoveUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.PacketEvent;
import relake.event.impl.misc.TickEvent;
import relake.event.impl.player.PlayerEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.FloatSetting;

import java.util.UUID;

public class FreeCamModule extends Module {
    public static Setting<Boolean> bypass = new BooleanSetting("Обход (FT)").setValue(true);
    public static Setting<Boolean> disableOnDamage = new BooleanSetting("Выключать при дамаге").setValue(true);
    public static Setting<Float> speed = new FloatSetting("Скорость по X/Z").range(0.1f, 5.0f, 0.05f).setValue(1.0f);
    public static Setting<Float> speedY = new FloatSetting("Скорость по Y").range(0.1f, 1.0f, 0.05f).setValue(0.5F);
    public RemoteClientPlayerEntity fakePlayer;

    public FreeCamModule() {
        super("Free Cam", "Активирует виртуальную камеру для обзора местности", "Activates a virtual camera to view the area", ModuleCategory.Player);
        registerComponent(bypass, disableOnDamage, speed, speedY);
    }

    @EventHandler
    public void onMotion(PlayerEvent e) {
        if (mc.player == null || mc.world == null) {
            switchState(false, true);

            return;
        }

        if (mc.player.hurtTime > 0 && disableOnDamage.getValue()) {
            switchState(false, true);

            return;
        }

        if (fakePlayer != null) {
            e.setPos(fakePlayer.getPositionVec());
            e.setRotate(new Vector2f(fakePlayer.rotationYaw, fakePlayer.rotationPitch));
            e.setOnGround(fakePlayer.isOnGround());
        }
    }

    @EventHandler
    public void onUpdate(TickEvent.Post e) {
        if (mc.player == null || mc.world == null) {
            switchState(false, true);

            return;
        }

        if (mc.player != null) {
            if (mc.player.getHealth() <= 0)
                switchState(false, true);

            MoveUtil.setSpeed(speed.getValue());

            mc.player.getMotion().y = 0;
            mc.player.noClip = true;

            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.getMotion().y = speedY.getValue();
            }

            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                mc.player.getMotion().y = -speedY.getValue();
            }
        }
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (mc.player == null || mc.world == null) {
            switchState(false, true);

            return;
        }

        if (fakePlayer != null) {
            if (!bypass.getValue())
                return;

            if (mc.player != null && mc.world != null) {
                if (e.getPacket() instanceof CEntityActionPacket
                        || e.getPacket() instanceof CPlayerTryUseItemOnBlockPacket
                        || e.getPacket() instanceof CPlayerTryUseItemPacket
                        || e.getPacket() instanceof CAnimateHandPacket) {

                    e.cancel();
                }

                if (e.getPacket() instanceof SPlayerPositionLookPacket eve) {
                    if (fakePlayer != null) {
                        fakePlayer.rotationYaw = eve.getYaw();
                        fakePlayer.rotationPitch = eve.getPitch();

                        fakePlayer.setPosition(eve.getX(), eve.getY(), eve.getZ());
                    }

                    e.cancel();
                }

                if (e.getPacket() instanceof SRespawnPacket) {
                    switchState(false, true);
                }
            }
        } else {
            switchState(false, true);
        }
    }


    @Override
    public void enable() {
        if (mc.player != null) {
            spawn();
        }

        super.enable();
    }

    @Override
    public void disable() {
        if (mc.player != null) {
            if (fakePlayer != null) {
                mc.player.setMotion(Vector3d.ZERO);
                mc.player.setPosition(fakePlayer.getPosX(), fakePlayer.getPosY(), fakePlayer.getPosZ());
                mc.player.rotationYaw = fakePlayer.rotationYaw;
                mc.player.rotationPitch = fakePlayer.rotationPitch;
                mc.player.setOnGround(fakePlayer.isOnGround());

                remove();
            }
        }

        super.disable();
    }

    public void spawn() {
        fakePlayer = new RemoteClientPlayerEntity(mc.world, new GameProfile(UUID.randomUUID(), mc.player.getGameProfile().getName()));
        fakePlayer.setMotion(mc.player.getMotion());
        fakePlayer.setPosition(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ());
        fakePlayer.rotationYaw = mc.player.rotationYaw;
        fakePlayer.rotationPitch = mc.player.rotationPitch;
        fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        fakePlayer.renderYawOffset = mc.player.renderYawOffset;
        fakePlayer.setOnGround(mc.player.isOnGround());
        fakePlayer.setInvisible(false);

        mc.world.addEntity(1488691337, fakePlayer);
    }

    public void remove() {
        mc.world.removeEntityFromWorld(1488691337);

        fakePlayer = null;
    }
}
