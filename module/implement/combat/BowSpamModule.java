package relake.module.implement.combat;

import net.minecraft.item.BowItem;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import relake.event.EventHandler;
import relake.event.impl.player.PlayerEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.FloatSetting;

import static net.minecraft.network.play.client.CPlayerDiggingPacket.Action.RELEASE_USE_ITEM;
import static net.minecraft.util.math.BlockPos.ZERO;

public class BowSpamModule extends Module {

    private final Setting<Float> shootSpeed = new FloatSetting("Скорость стрельбы")
            .range(2.5F, 5)
            .setValue(3f);

    public BowSpamModule() {
        super("Bow Spam", "Быстро стреляет или же спамит из лука при зажатой правой клавиши мыши", "He shoots fast or spams arrows with the right mouse button pressed", ModuleCategory.Combat);
        registerComponent(shootSpeed);
    }

    @EventHandler
    public void player(PlayerEvent playerEvent) {
        if (mc.player.inventory.getCurrentItem().getItem() instanceof BowItem
                && mc.player.isHandActive()
                && mc.player.getItemInUseMaxCount() >= shootSpeed.getValue()) {

            mc.player.connection.sendPacket(new CPlayerDiggingPacket(RELEASE_USE_ITEM, ZERO, mc.player.getHorizontalFacing()));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.stopActiveHand();
        }
    }
}
