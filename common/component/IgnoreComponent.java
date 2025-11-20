package relake.common.component;

import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.TextFormatting;
import relake.Client;
import relake.common.InstanceAccess;
import relake.event.EventHandler;
import relake.event.impl.misc.PacketEvent;

public class IgnoreComponent implements InstanceAccess {

    public IgnoreComponent() {
        Client.instance.eventManager.register(this);
    }

    @EventHandler
    public void packet(PacketEvent.Receive event) {
        IPacket<?> packet = event.getPacket();
        if (packet instanceof SChatPacket wrapper) {
            String message = TextFormatting.getTextWithoutFormattingCodes(wrapper.getChatComponent().getString());
            if (message == null) return;
            String[] words = message.split("\\s+");
            for (String word : words) {
                if (Client.instance.commandManager.ignoreCommand.ignored.contains(word)) {
                    event.cancel();
                    break;
                }
            }
        }
    }
}
