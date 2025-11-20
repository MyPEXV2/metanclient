package relake.command.implement;

import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.TextFormatting;
import relake.Client;
import relake.command.Command;
import relake.common.util.ChatUtil;
import relake.common.util.StringUtil;
import relake.module.Module;
import relake.module.ModuleManager;

public class BindCommand extends Command {

    public BindCommand() {
        super("bind", "bind command");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            ChatUtil.send(TextFormatting.RED + "Использование: .bind <add | remove | list | clear> [аргумент]");
            return;
        }

        String subCommand = args[1].toLowerCase();
        ModuleManager moduleManager = Client.instance.moduleManager;

        switch (subCommand) {
            case "add":
                handleAdd(args, moduleManager);
                break;
            case "remove":
                handleRemove(args, moduleManager);
                break;
            case "list":
                handleList(moduleManager);
                break;
            case "clear":
                handleClear(moduleManager);
                break;
            default:
                ChatUtil.send(TextFormatting.RED + "Неизвестная подкоманда. Используйте: add, remove, list, clear.");
        }
    }

    private void handleAdd(String[] args, ModuleManager moduleManager) {
        if (args.length < 4) {
            ChatUtil.send(TextFormatting.RED + "Использование: .bind add <module> <key>");
            return;
        }

        String name = args[2];
        String key = args[3].toLowerCase();

        for (Module module : moduleManager.modules) {
            if (name.equalsIgnoreCase(module.getName().replace(" ", ""))) {
                name = module.getName();
            }
        }


        if (moduleManager.getModule(name) == null) {
            ChatUtil.send(TextFormatting.RED + "Модуль не найден!");
            return;
        }

        if (moduleManager.getModule(name).getKey() != -1) {
            ChatUtil.send(TextFormatting.YELLOW + "Бинд \"" + name + "\" уже в списке.");
        } else {
            moduleManager.getModule(name).setKey(InputMappings.getInputByName("key.keyboard." + key).getKeyCode());
            ChatUtil.send(TextFormatting.GREEN + "Бинд \"" + name + "\" добавлен в список.");
        }
    }

    private void handleRemove(String[] args, ModuleManager moduleManager) {
        if (args.length < 3) {
            ChatUtil.send(TextFormatting.RED + "Использование: .bind remove <module>");
            return;
        }

        String name = args[2];

        for (Module module : moduleManager.modules) {
            if (name.equalsIgnoreCase(module.getName().replace(" ", ""))) {
                name = module.getName();
            }
        }

        if (moduleManager.getModule(name) == null) {
            ChatUtil.send(TextFormatting.RED + "Модуль не найден!");
            return;
        }

        if (moduleManager.getModule(name).getKey() != -1) {
            moduleManager.getModule(name).setKey(-1);
            ChatUtil.send(TextFormatting.GREEN + "Бинд \"" + name + "\" удалён из списка.");
        } else {
            ChatUtil.send(TextFormatting.YELLOW + "Бинд \"" + name + "\" отсутствует в списке.");
        }
    }

    private void handleList(ModuleManager moduleManager) {
        if (moduleManager.modules.stream().filter(module -> module.getKey() != -1).toList().isEmpty()) {
            ChatUtil.send(TextFormatting.YELLOW + "Список биндов пуст.");
            return;
        }

        StringBuilder list = new StringBuilder(TextFormatting.GRAY + "Список биндов:\n");
        for (int i = 0; i < moduleManager.modules.size(); i++) {
            Module module = moduleManager.modules.get(i);
            if (module.getKey() != -1) {
                list.append(TextFormatting.WHITE).append(module.getName() + " : " + StringUtil.getKeyName(module.getKey()));
                if (i < moduleManager.modules.size() - 1) {
                    list.append(TextFormatting.GRAY).append(", \n");
                }
            }
        }
        ChatUtil.send(list.toString());
    }

    private void handleClear(ModuleManager moduleManager) {
        for (int i = 0; i < moduleManager.modules.size(); i++) {
            Module module = moduleManager.modules.get(i);
            module.setKey(-1);
        }

        ChatUtil.send(TextFormatting.GREEN + "Список биндов очищен.");
    }
}
