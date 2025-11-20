package relake.module.implement.misc;

import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.server.SSendResourcePackPacket;
import relake.event.EventHandler;
import relake.event.impl.misc.PacketEvent;
import relake.module.Module;
import relake.module.ModuleCategory;

public class RPSpooferModule extends Module {
    public RPSpooferModule() {
        super("RP Spoofer", "Обманывает сервер что вы установили его собственный ресурспак", "The server is tricking you into installing its own resource pack", ModuleCategory.Misc);
    }

    @EventHandler
    public void packetEvent(PacketEvent.Receive packetEvent) {
        if (packetEvent.getPacket() instanceof SSendResourcePackPacket) {
            mc.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.ACCEPTED));
            mc.player.connection.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.SUCCESSFULLY_LOADED));

            if (mc.currentScreen != null) {
                mc.player.closeScreen();
            }
            packetEvent.setCancelled(true);
        }
    }
}
