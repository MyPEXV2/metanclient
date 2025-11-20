package relake.command.implement;

import net.minecraft.util.text.TextFormatting;
import relake.command.Command;
import relake.common.util.ChatUtil;

import java.util.concurrent.CopyOnWriteArrayList;

public class IgnoreCommand extends Command {

    public CopyOnWriteArrayList<String> ignored = new CopyOnWriteArrayList<>();

    public IgnoreCommand() {
        super("ignore", "ignore command");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            ChatUtil.send(TextFormatting.RED + "Использование: .ignore <add | remove | list | clear> [аргумент]");
            return;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "add":
                handleAdd(args);
                break;
            case "remove":
                handleRemove(args);
                break;
            case "list":
                handleList();
                break;
            case "clear":
                handleClear();
                break;
            default:
                ChatUtil.send(TextFormatting.RED + "Неизвестная подкоманда. Используйте: add, remove, list, clear.");
        }
    }

    private void handleAdd(String[] args) {
        if (args.length < 3) {
            ChatUtil.send(TextFormatting.RED + "Использование: .ignore add <name>");
            return;
        }

        String name = args[2];

        if (contains(name)) {
            ChatUtil.send(TextFormatting.YELLOW + "Игрок \"" + name + "\" уже в списке.");
        } else {
            ignored.add(name);
            ChatUtil.send(TextFormatting.GREEN + "Игрок \"" + name + "\" добавлен в список игонрируемых.");
        }
    }

    private void handleRemove(String[] args) {
        if (args.length < 3) {
            ChatUtil.send(TextFormatting.RED + "Использование: .ignore remove <name>");
            return;
        }

        String name = args[2];

        if (!contains(name)) {
            ChatUtil.send(TextFormatting.YELLOW + "Игрок \"" + name + "\" отсутствует в списке.");
        } else {
            ignored.remove(name);
            ChatUtil.send(TextFormatting.GREEN + "Игрок \"" + name + "\" удалён из списка игнорируемых.");
        }
    }

    private void handleList() {
        if (ignored.isEmpty()) {
            ChatUtil.send(TextFormatting.YELLOW + "Список игнорируемых пуст.");
            return;
        }

        StringBuilder list = new StringBuilder(TextFormatting.GRAY + "Список игнорируемых:\n");
        for (int i = 0; i < ignored.size(); i++) {
            String name = ignored.get(i);
            list.append(TextFormatting.WHITE).append(name);
            if (i < ignored.size() - 1) {
                list.append(TextFormatting.GRAY).append(", ");
            }
        }
        ChatUtil.send(list.toString());
    }

    private void handleClear() {
        ignored.clear();
        ChatUtil.send(TextFormatting.GREEN + "Список игнорируемых очищен.");
    }

    public boolean contains(String name) {
        return ignored.stream().anyMatch(ignored -> ignored.equalsIgnoreCase(name));
    }
}
