package relake.command.implement;

import net.minecraft.util.text.TextFormatting;
import relake.Client;
import relake.command.Command;
import relake.common.util.ChatUtil;

public class CacaoCommand extends Command {
    public CacaoCommand() {
        super("cacao", "cacao command");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            ChatUtil.send(TextFormatting.RED + "Использование: .cacao <pos1 | pos2> [x] [y] [z]");
            return;
        }

        String subCommand = args[1].toLowerCase();
        
        if (subCommand.equals("pos1")) {
            if (args.length >= 5) {
                try {
                    int x = Integer.parseInt(args[2]);
                    int y = Integer.parseInt(args[3]);
                    int z = Integer.parseInt(args[4]);
                    Client.instance.moduleManager.autoCashModule.setPos1(x, y, z);
                    ChatUtil.send(TextFormatting.GREEN + "Pos1 установлена: " + x + ", " + y + ", " + z);
                } catch (NumberFormatException e) {
                    ChatUtil.send(TextFormatting.RED + "Ошибка: неверный формат координат");
                }
            } else {
                if (mc.player != null) {
                    Client.instance.moduleManager.autoCashModule.setPos1FromPlayer();
                    ChatUtil.send(TextFormatting.GREEN + "Pos1 установлена на вашу позицию");
                }
            }
        } else if (subCommand.equals("pos2")) {
            if (args.length >= 5) {
                try {
                    int x = Integer.parseInt(args[2]);
                    int y = Integer.parseInt(args[3]);
                    int z = Integer.parseInt(args[4]);
                    Client.instance.moduleManager.autoCashModule.setPos2(x, y, z);
                    ChatUtil.send(TextFormatting.GREEN + "Pos2 установлена: " + x + ", " + y + ", " + z);
                } catch (NumberFormatException e) {
                    ChatUtil.send(TextFormatting.RED + "Ошибка: неверный формат координат");
                }
            } else {
                if (mc.player != null) {
                    Client.instance.moduleManager.autoCashModule.setPos2FromPlayer();
                    ChatUtil.send(TextFormatting.GREEN + "Pos2 установлена на вашу позицию");
                }
            }
        } else {
            ChatUtil.send(TextFormatting.RED + "Использование: .cacao <pos1 | pos2> [x] [y] [z]");
        }
    }
}

