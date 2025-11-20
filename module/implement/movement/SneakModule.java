package relake.module.implement.movement;

import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.Hand;
import relake.common.util.ChatUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.PacketEvent;
import relake.event.impl.misc.TickEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.SelectSetting;

public class SneakModule extends Module {

    public final SelectSetting selectComponent = new SelectSetting("Режимы")
            .setValue("Пакетный",
                    "Обход");


    public SneakModule() {
        super("Sneak", "Зажимает приседание, может делать это как виртуально так и натурально", "Clamps squats, can do it both virtually and naturally", ModuleCategory.Movement);
        registerComponent(selectComponent);
        selectComponent.setSelected("Пакетный");
    }

    @Override
    public void enable() {
        super.enable();

        if (mc.player != null) {
            if (selectComponent.isSelected("Пакетный")) {
                for (Entity entity : mc.world.getAllEntities()) {
                    if (entity == mc.player || mc.player.getDistance(entity) > 3) {
                        continue;
                    }
                    mc.getConnection().sendPacket(new CUseEntityPacket(entity, Hand.MAIN_HAND, true));
                    ChatUtil.send("Отключил от " + entity.getName().getString());
                    return;
                }
                ChatUtil.send("Не удалось найти энтити для активации");
                switchState();
            }
        }
    }

    @Override
    public void disable() {
        super.disable();

        if (mc.player != null) {
            mc.gameSettings.keyBindSneak.setPressed(false);
            mc.getConnection().sendPacketWOEvent(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
        }
    }

    @EventHandler
    public void tick(TickEvent tickEvent) {
        mc.gameSettings.keyBindSneak.setPressed(true);
        if (selectComponent.isSelected("Обход")) {
            mc.getConnection().sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.PRESS_SHIFT_KEY));
        }
    }

    @EventHandler
    public void packetEvent(PacketEvent.Send packetEvent) {
        if (!selectComponent.isSelected("Пакетный")) return;

        if (packetEvent.getPacket() instanceof CEntityActionPacket action) {
            if (action.getAction() == CEntityActionPacket.Action.PRESS_SHIFT_KEY || action.getAction() == CEntityActionPacket.Action.RELEASE_SHIFT_KEY)
                packetEvent.setCancelled(true);
        }
        if (packetEvent.getPacket() instanceof CUseEntityPacket packet) {
            packet.field_241791_e_ = true;
        }
    }
}
