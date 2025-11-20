package relake.module.implement.misc;

import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.RandomStringUtils;
import relake.common.util.ChatUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.PacketEvent;
import relake.event.impl.misc.TickEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.StringSetting;

public class AutoAuthModule extends Module {

    private final Setting<String> password = new StringSetting("Пароль")
            .setValue(RandomStringUtils.randomAlphabetic(8));

    public AutoAuthModule() {
        super("Auto Auth", "Автоматически авторизовывает на сервере по мере надобности", "Automatically authorizes on the server as needed", ModuleCategory.Misc);
        registerComponent(password);
    }

    @EventHandler
    public void tick(TickEvent event) {
        if (password.getValue() == null || password.getValue().isEmpty()) {
            ChatUtil.send("Укажите ваш пароль!");
            switchState();
        }
    }

    @EventHandler
    public void packet(PacketEvent.Receive event) {
        IPacket<?> packet = event.getPacket();

        if (packet instanceof SChatPacket wrapper) {
            String message = TextFormatting.getTextWithoutFormattingCodes(wrapper.getChatComponent().getString());

            if (message == null) return;

            if (message.contains("/login")) {
                mc.player.sendChatMessage("/login " + password.getValue());
            } else if (message.contains("/reg") || message.contains("/register")) {
                mc.player.sendChatMessage("/register " + password.getValue() + " " + password.getValue());
                mc.player.sendChatMessage("/register " + password.getValue());
                ChatUtil.sendHoverText("Успешно зарегистрировался!", TextFormatting.GREEN + "[Ваш пароль]", password.getValue());
            }
        }
    }
}
