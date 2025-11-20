package relake.command.implement;

import net.minecraft.util.text.TextFormatting;
import relake.Client;
import relake.command.Command;
import relake.common.util.ChatUtil;

public class TransferCommand extends Command {
    public TransferCommand() {
        super("transfer", "lol");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 2) {
            ChatUtil.send(TextFormatting.RED + "Использование: .transfer <Номер анархии>");
            return;
        }

        String anarchyIdStr = args[1];
        int anarchyId;

        try {
            anarchyId = Integer.parseInt(anarchyIdStr);
        } catch (NumberFormatException e) {
            ChatUtil.send(TextFormatting.RED + "Ошибка: введён недопустимый номер анархии.");
            return;
        }

        if (isValidAnarchy(anarchyId)) {
            Client.instance.moduleManager.autoTransferModule.target = anarchyId;
            ChatUtil.send(TextFormatting.GREEN + "Установленный номер анархии: " + anarchyId);
        } else {
            ChatUtil.send(TextFormatting.RED + "Ошибка: введён недопустимый номер анархии.");
        }
    }

    private boolean isValidAnarchy(int id) {
        return (id >= 101 && id <= 111) ||
                (id >= 201 && id <= 234) ||
                (id >= 301 && id <= 322) ||
                (id >= 501 && id <= 512) ||
                (id >= 601 && id <= 607);
    }
}
