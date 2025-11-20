package relake.module.implement.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;
import relake.Client;
import relake.common.util.ChatUtil;
import relake.common.util.SoundUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.KeyboardEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.module.implement.render.ClientSoundsModule;
import relake.notification.NotificationManager;
import relake.notification.NotificationType;
import relake.settings.Setting;
import relake.settings.implement.KeySetting;

public class ClickFriendModule extends Module {


    private final Setting<Integer> key = new KeySetting("Клавиша для добавления")
            .setValue(GLFW.GLFW_MOUSE_BUTTON_MIDDLE);

    public ClickFriendModule() {
        super("Click Friend", "Позволяет добавить или удалить друга из списка нажатием на 1 кнопку, нужно навестись на игрока", "Allows you to add or remove a friend from the list by pressing 1 button, you need to target the player", ModuleCategory.Misc);
        registerComponent(key);
    }

    @EventHandler
    public void keyboard(KeyboardEvent event) {
        if (event.getKey() == key.getValue() && event.getAction() == GLFW.GLFW_PRESS && mc.pointedEntity instanceof PlayerEntity) {
            String name = mc.pointedEntity.getNotHidedName().getString();
            if (Client.instance.friendManager.isFriend(name)) {
                Client.instance.friendManager.removeFriend(name);
                if (Client.instance.moduleManager.clientSoundsModule.friends.getValue()) SoundUtil.playSound("removefriend.wav", 0.1f);
                NotificationManager.send("Friend Manager", "Игрок \"" + name + "\" удалён из списка друзей.", NotificationType.INFO, 1000);

            } else {
                Client.instance.friendManager.addFriend(name);
                if (Client.instance.moduleManager.clientSoundsModule.friends.getValue()) SoundUtil.playSound("addfriend.wav", 0.1f);
                NotificationManager.send("Friend Manager", "Игрок \"" + name + "\" добавлен в список друзей.", NotificationType.INFO, 1000);

            }
        }
    }
}
