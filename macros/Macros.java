package relake.macros;

import lombok.AllArgsConstructor;
import lombok.Getter;
import relake.common.InstanceAccess;

@Getter
@AllArgsConstructor
public class Macros implements InstanceAccess {
    private final String name;
    private final int key;
    private final String message;

    public void sendMessage() {
        mc.player.sendChatMessage(message);
    }
}
