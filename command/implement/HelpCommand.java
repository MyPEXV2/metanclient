package relake.command.implement;

import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;
import relake.Client;
import relake.command.Command;
import relake.common.util.ChatUtil;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "help command");
    }

    @Override
    public void execute(String[] args) {
        Client.instance.commandManager.commands.forEach(command -> ChatUtil.send(TextFormatting.YELLOW + StringUtils.capitalize(command.getName()) + " - " + command.getDescription()));
    }
}
