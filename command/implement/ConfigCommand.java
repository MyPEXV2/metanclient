package relake.command.implement;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import relake.Client;
import relake.command.Command;
import relake.common.util.ChatUtil;
import relake.common.util.SoundUtil;
import relake.module.implement.render.ClientSoundsModule;
import relake.module.implement.render.NotificationsModule;
import relake.notification.NotificationManager;
import relake.notification.NotificationType;

import java.io.IOException;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("Config", "config command", "cfg");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            if (Client.instance.moduleManager.clientSoundsModule.cfg.getValue()) SoundUtil.playSound("usecommand.wav", 0.1f);
            NotificationManager.send("Info", "Использование: .config <load | save | list | dir> [аргумент]", NotificationType.INFO, 10000);
            return;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "save":
                if (args.length < 3) {
                    ChatUtil.send(TextFormatting.RED + "Использование: .");
                    return;
                }
               if (Client.instance.moduleManager.clientSoundsModule.cfg.getValue()) SoundUtil.playSound("successfully.wav", 0.1f);NotificationManager.send("Config", "Успешно сохранил: " + args[2], NotificationType.INFO, 1000);
                Client.instance.configManager.moduleConfig.setName(args[2]).save();
                break;
            case "load":
                if (args.length < 3) {
                    ChatUtil.send(TextFormatting.RED + "Использование: .");
                    return;
                }
                if (Client.instance.configManager.moduleConfig.setName(args[2]).load()) {
                    if (Client.instance.moduleManager.clientSoundsModule.cfg.getValue()) SoundUtil.playSound("cfgload.wav", 0.1f);
                    NotificationManager.send("Config", "Успешно загрузил: " + args[2], NotificationType.INFO, 1000);
                } else {
                    if (Client.instance.moduleManager.clientSoundsModule.cfg.getValue()) SoundUtil.playSound("unknowncommand.wav", 0.1f);
                    NotificationManager.send("Config", "Конфига " + args[2] + " не существует.", NotificationType.ERROR, 1000);
                }
                break;
            case "list":
                for (String module : Client.instance.configManager.getConfigsInfo("module")) {
                    ChatUtil.sendClickText(module, TextFormatting.GREEN + "[Загрузить]", new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".cfg load " + module.replace(".module", "")));
                }
                break;
            case "dir":
                try {
                   if (Client.instance.moduleManager.clientSoundsModule.cfg.getValue()) SoundUtil.playSound("opendir.wav", 0.1f);NotificationManager.send("Directory", "Успешно открыл директорию", NotificationType.WARN, 1000);
                    Runtime.getRuntime().exec("explorer " + "C:\\relake\\module");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
               if (Client.instance.moduleManager.clientSoundsModule.cfg.getValue()) SoundUtil.playSound("unknowncommand.wav", 0.1f);NotificationManager.send("Error", "Неизвестная подкоманда. Используйте: load, save, list, dir.", NotificationType.ERROR, 5000);
        }
    }
}
