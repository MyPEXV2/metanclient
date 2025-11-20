package relake.command.implement;

import net.minecraft.util.Session;
import net.minecraft.util.text.TextFormatting;
import relake.Client;
import relake.command.Command;
import relake.common.util.ChatUtil;
import relake.config.Config;

import java.util.UUID;

public class LoginCommand extends Command {

    public LoginCommand() {
        super("login", "login command", "l");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            ChatUtil.send(TextFormatting.RED + "Использование: .login <name>");
            return;
        }

        String subCommand = args[1];

        mc.session = new Session(subCommand, String.valueOf(UUID.randomUUID()), "", "");
        Client.instance.accountManager.setLastLogin(subCommand);
        ChatUtil.send("Вы изменили имя на - " + subCommand);
    }
}
