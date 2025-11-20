package relake.command.implement;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.registry.Registry;

import relake.Client;
import relake.command.Command;
import relake.common.util.ChatUtil;
import relake.module.implement.player.NukerModule;

public class NukerCommand extends Command {

    public NukerCommand() {
        super("Nuker", "nuker command");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            ChatUtil.send(TextFormatting.RED + "Использование: .nuker <add | remove | list | clear | all> [аргумент]");
            return;
        }

        String subCommand = args[1].toLowerCase();
        NukerModule nukerModule = Client.instance.moduleManager.nukerModule;

        if (nukerModule == null) {
            ChatUtil.send(TextFormatting.RED + "Модуль Nuker не найден.");
            return;
        }

        switch (subCommand) {
            case "add":
                handleAdd(args, nukerModule);
                break;
            case "remove":
                handleRemove(args, nukerModule);
                break;
            case "list":
                handleList(nukerModule);
                break;
            case "clear":
                handleClear(nukerModule);
                break;
            case "all":
                handleAll(nukerModule);
                break;
            default:
                ChatUtil.send(TextFormatting.RED + "Неизвестная подкоманда. Используйте: add, remove, list, clear, all.");
        }
    }

    private void handleAdd(String[] args, NukerModule nukerModule) {
        if (args.length < 3) {
            ChatUtil.send(TextFormatting.RED + "Использование: .nuker add <block>");
            return;
        }

        String blockName = args[2].toLowerCase();
        Block blockToAdd = Registry.BLOCK.getOptional(new ResourceLocation(blockName)).orElse(null);

        if (blockToAdd == null) {
            ChatUtil.send(TextFormatting.RED + "Блок \"" + blockName + "\" не найден.");
            return;
        }

        if (nukerModule.targetBlocks.contains(blockToAdd)) {
            ChatUtil.send(TextFormatting.YELLOW + "Блок \"" + blockToAdd.getTranslatedName().getString() + "\" уже в списке.");
        } else {
            nukerModule.targetBlocks.add(blockToAdd);
            ChatUtil.send(TextFormatting.GREEN + "Блок \"" + blockToAdd.getTranslatedName().getString() + "\" добавлен в список целевых блоков.");
        }
    }

    private void handleRemove(String[] args, NukerModule nukerModule) {
        if (args.length < 3) {
            ChatUtil.send(TextFormatting.RED + "Использование: .nuker remove <block>");
            return;
        }

        String blockName = args[2].toLowerCase();
        Block blockToRemove = Registry.BLOCK.getOptional(new ResourceLocation(blockName)).orElse(null);

        if (blockToRemove == null) {
            ChatUtil.send(TextFormatting.RED + "Блок \"" + blockName + "\" не найден.");
            return;
        }

        if (nukerModule.targetBlocks.remove(blockToRemove)) {
            ChatUtil.send(TextFormatting.GREEN + "Блок \"" + blockToRemove.getTranslatedName().getString() + "\" удалён из списка целевых блоков.");
        } else {
            ChatUtil.send(TextFormatting.YELLOW + "Блок \"" + blockToRemove.getTranslatedName().getString() + "\" отсутствует в списке.");
        }
    }

    private void handleList(NukerModule nukerModule) {
        if (nukerModule.targetBlocks.isEmpty()) {
            ChatUtil.send(TextFormatting.YELLOW + "Список целевых блоков пуст.");
            return;
        }

        StringBuilder list = new StringBuilder(TextFormatting.GRAY + "Список целевых блоков:\n");
        for (int i = 0; i < nukerModule.targetBlocks.size(); i++) {
            Block block = nukerModule.targetBlocks.get(i);
            list.append(TextFormatting.WHITE).append(block.getTranslatedName().getString());
            if (i < nukerModule.targetBlocks.size() - 1) {
                list.append(TextFormatting.GRAY).append(", ");
            }
        }
        ChatUtil.send(list.toString());
    }

    private void handleClear(NukerModule nukerModule) {
        nukerModule.targetBlocks.clear();
        ChatUtil.send(TextFormatting.GREEN + "Список целевых блоков очищен.");
    }

    private void handleAll(NukerModule nukerModule) {
        nukerModule.targetBlocks.clear();
        Registry.BLOCK.forEach(nukerModule.targetBlocks::add);
        ChatUtil.send(TextFormatting.GREEN + "Теперь Nuker копает все блоки.");
    }
}
