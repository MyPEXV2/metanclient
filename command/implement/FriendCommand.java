package relake.command.implement;

import net.minecraft.util.text.TextFormatting;
import relake.Client;
import relake.command.Command;
import relake.common.util.ChatUtil;
import relake.common.util.SoundUtil;
import relake.friend.Friend;
import relake.friend.FriendManager;
import relake.module.implement.render.ClientSoundsModule;
import relake.notification.NotificationManager;
import relake.notification.NotificationType;

public class FriendCommand extends Command {
    public FriendCommand() {
        super("friend", "friend command");
    }


    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            if (Client.instance.moduleManager.clientSoundsModule.friends.getValue()) SoundUtil.playSound("usecommand.wav", 0.1f);
            NotificationManager.send("Friend Manager",  "Использование: .friend <add | remove | list | clear> [аргумент]", NotificationType.ERROR, 5000);

            return;
        }

        String subCommand = args[1].toLowerCase();
        FriendManager friendManager = Client.instance.friendManager;

        switch (subCommand) {
            case "add":
                handleAdd(args, friendManager);
                break;
            case "remove":
                handleRemove(args, friendManager);
                break;
            case "list":
                handleList(friendManager);
                break;
            case "clear":
                handleClear(friendManager);
                break;
            default:
                if (Client.instance.moduleManager.clientSoundsModule.friends.getValue()) SoundUtil.playSound("unknowncommand.wav", 0.1f);
                NotificationManager.send("Error",  "Неизвестная подкоманда. Используйте: add, remove, list, clear.", NotificationType.ERROR, 5000);

        }
    }

    private void handleAdd(String[] args, FriendManager friendManager) {
        if (args.length < 3) {
            if (Client.instance.moduleManager.clientSoundsModule.friends.getValue()) SoundUtil.playSound("usecommand.wav", 0.1f);
            NotificationManager.send("Friend Manager", "Использование: .friend add <name>", NotificationType.INFO, 2000);

            return;
        }

        String friendName = args[2];

        if (friendManager.isFriend(friendName)) {
            if (Client.instance.moduleManager.clientSoundsModule.friends.getValue()) SoundUtil.playSound("unknowncommand.wav", 0.1f);
            NotificationManager.send("Error",  "Игрок \"" + friendName + "\" уже в списке.", NotificationType.ERROR, 5000);

        } else {
            friendManager.addFriend(friendName);
            if (Client.instance.moduleManager.clientSoundsModule.friends.getValue()) SoundUtil.playSound("addfriend.wav", 0.1f);
            NotificationManager.send("Friend Manager", "Игрок \"" + friendName + "\" добавлен в список друзей.", NotificationType.INFO, 1000);
        }
    }

    private void handleRemove(String[] args, FriendManager friendManager) {
        if (args.length < 3) {
            if (Client.instance.moduleManager.clientSoundsModule.friends.getValue()) SoundUtil.playSound("usecommand.wav", 0.1f);
            NotificationManager.send("Friend Manager", "Использование: .friend remove <name>", NotificationType.INFO, 3500);

            return;
        }

        String friendName = args[2];

        if (!friendManager.isFriend(friendName)) {

            if (Client.instance.moduleManager.clientSoundsModule.friends.getValue()) SoundUtil.playSound("unknowncommand.wav", 0.1f);
            NotificationManager.send("Error",  "Игрок \"" + friendName + "\" отсутствует в списке.", NotificationType.ERROR, 5000);

        } else {
            friendManager.removeFriend(friendName);
            if (Client.instance.moduleManager.clientSoundsModule.friends.getValue()) SoundUtil.playSound("removefriend.wav", 0.1f);
            NotificationManager.send("Friend Manager", "Игрок \"" + friendName + "\" удалён из списка друзей.", NotificationType.INFO, 2500);

        }
    }

    private void handleList(FriendManager friendManager) {
        if (friendManager.friends.isEmpty()) {
            if (Client.instance.moduleManager.clientSoundsModule.friends.getValue()) SoundUtil.playSound("unknowncommand.wav", 0.1f);
            NotificationManager.send("Error",  "Список друзей пуст.", NotificationType.ERROR, 2500);

            return;
        }
        if (Client.instance.moduleManager.clientSoundsModule.friends.getValue()) SoundUtil.playSound("successfully.wav", 0.1f);
        NotificationManager.send("Friend Manager", "Список друзей отобразился в чате.", NotificationType.INFO, 8750);
        StringBuilder list = new StringBuilder(TextFormatting.GRAY + "Список друзей:\n");
        for (int i = 0; i < friendManager.friends.size(); i++) {
            Friend friend = friendManager.friends.get(i);
            list.append(TextFormatting.WHITE).append(friend.getName());
            if (i < friendManager.friends.size() - 1) {
                list.append(TextFormatting.GRAY).append(", ");
            }
        }
        ChatUtil.send(list.toString());
    }

    private void handleClear(FriendManager friendManager) {
        friendManager.clearFriends();
        if (Client.instance.moduleManager.clientSoundsModule.friends.getValue()) SoundUtil.playSound("successfully.wav", 0.1f);
        NotificationManager.send("Friend Manager", "Список друзей очищен.", NotificationType.INFO, 2000);

    }
}
