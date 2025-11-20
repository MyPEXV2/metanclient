package relake.module.implement.player;

import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextFormatting;
import relake.Client;
import relake.event.EventHandler;
import relake.event.impl.misc.PacketEvent;
import relake.friend.Friend;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;

import java.util.Arrays;

public class AutoAcceptModule extends Module {
    private final Setting<Boolean> friend = new BooleanSetting("Принимать только друзей")
            .setValue(false);

    private final String[] acceptMessages = {
            " ╔ Запрос на телепортацию от ",
            " просит телепортироваться к Вам. [✔] [✗]",
            " хочет телепортироваться к вам."
    };

    public AutoAcceptModule() {
        super("Auto Accept", "Автоматически принимает запросы на телепорт", "Automatically accepts teleport requests", ModuleCategory.Player);
        registerComponent(friend);
    }

    @EventHandler
    public void packetEvent(PacketEvent.Receive packetEvent) {
        if (packetEvent.getPacket() instanceof SChatPacket sChatPacket) {
            String message = StringUtils.stripControlCodes(sChatPacket.getChatComponent().getString());
            if (isTeleporting(message)) {
                if (friend.getValue()) {
                    handleFriendTeleport(message);
                    return;
                }

                mc.player.sendChatMessage("/tpaccept");
            }
        }
    }

    private void handleFriendTeleport(String message) {
        for (Friend friend : Client.instance.friendManager.friends) {
            if (message.contains(StringUtils.stripControlCodes(friend.getName()))) {
                mc.player.sendChatMessage("/tpaccept");
                break;
            }
        }
    }

    private boolean isTeleporting(String message) {
        return Arrays.stream(acceptMessages)
                .map(String::toLowerCase)
                .anyMatch(message::contains);
    }
}
