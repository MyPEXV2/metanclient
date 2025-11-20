package relake.module.implement.combat;

import relake.Client;
import relake.event.EventHandler;
import relake.event.impl.player.AttackEvent;
import relake.module.Module;
import relake.module.ModuleCategory;

public class NoFriendDamageModule extends Module {

    public NoFriendDamageModule() {
        super("No Friend Damage", "Не даёт ударить игрока добавленного в список друзей", "Prevents you from hitting a player added to your friends list", ModuleCategory.Combat);
    }

    @EventHandler
    public void attack(AttackEvent event) {
        if (Client.instance.friendManager.isFriend(event.getEntity().getNotHidedName().getString())) event.cancel();
    }
}
