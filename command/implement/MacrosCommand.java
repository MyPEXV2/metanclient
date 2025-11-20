package relake.command.implement;

import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.TextFormatting;
import relake.Client;
import relake.command.Command;
import relake.common.util.ChatUtil;
import relake.common.util.StringUtil;
import relake.macros.Macros;
import relake.macros.MacrosManager;

public class MacrosCommand extends Command {
    public MacrosCommand() {
        super("macros", "macros command", "macro");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            ChatUtil.send(TextFormatting.RED + "Использование: .macros <add | remove | list | clear> [аргумент]");
            return;
        }

        String subCommand = args[1].toLowerCase();
        MacrosManager macrosManager = Client.instance.macrosManager;

        switch (subCommand) {
            case "add":
                handleAdd(args, macrosManager);
                break;
            case "remove":
                handleRemove(args, macrosManager);
                break;
            case "list":
                handleList(macrosManager);
                break;
            case "clear":
                handleClear(macrosManager);
                break;
            default:
                ChatUtil.send(TextFormatting.RED + "Неизвестная подкоманда. Используйте: add, remove, list, clear.");
        }
    }

    private void handleAdd(String[] args, MacrosManager macrosManager) {
        if (args.length < 5) {
            ChatUtil.send(TextFormatting.RED + "Использование: .macros add <name> <key> <message>");
            return;
        }

        String name = args[2];
        String key = args[3].toLowerCase();
        String message = args[4];

        if (macrosManager.contains(name)) {
            ChatUtil.send(TextFormatting.YELLOW + "Макрос \"" + name + "\" уже в списке.");
        } else {
            macrosManager.addMacros(name, InputMappings.getInputByName("key.keyboard." + key).getKeyCode(), message);
            ChatUtil.send(TextFormatting.GREEN + "Макрос \"" + name + "\" добавлен в список.");
        }
    }

    private void handleRemove(String[] args, MacrosManager macrosManager) {
        if (args.length < 3) {
            ChatUtil.send(TextFormatting.RED + "Использование: .macros remove <name>");
            return;
        }

        String name = args[2];

        if (macrosManager.contains(name)) {
            macrosManager.removeMacros(name);
            ChatUtil.send(TextFormatting.GREEN + "Макрос \"" + name + "\" удалён из списка.");
        } else {
            ChatUtil.send(TextFormatting.YELLOW + "Макрос \"" + name + "\" отсутствует в списке.");
        }
    }

    private void handleList(MacrosManager macrosManager) {
        if (macrosManager.macros.isEmpty()) {
            ChatUtil.send(TextFormatting.YELLOW + "Список макросов пуст.");
            return;
        }

        StringBuilder list = new StringBuilder(TextFormatting.GRAY + "Список макросов:\n");
        for (int i = 0; i < macrosManager.macros.size(); i++) {
            Macros macro = macrosManager.macros.get(i);
            list.append(TextFormatting.WHITE).append(macro.getName() + " : " + StringUtil.getKeyName(macro.getKey()) + " : " + macro.getMessage());
            if (i < macrosManager.macros.size() - 1) {
                list.append(TextFormatting.GRAY).append(", \n");
            }
        }
        ChatUtil.send(list.toString());
    }

    private void handleClear(MacrosManager macrosManager) {
        macrosManager.clearMacros();
        ChatUtil.send(TextFormatting.GREEN + "Список макросов очищен.");
    }
}
