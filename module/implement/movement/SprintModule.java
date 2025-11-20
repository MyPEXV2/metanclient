package relake.module.implement.movement;

import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.potion.Effects;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.event.impl.player.AttackEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;

public class SprintModule extends Module {

    public final Setting<Boolean> ignoreHunger = new BooleanSetting("Игнорировать голод")
            .setValue(false);

    public SprintModule() {
        super("Sprint", "Автоматически переходит в режим бега при ходьбе", "Automatically switches to running mode when walking", ModuleCategory.Movement);
        registerComponent(ignoreHunger);
    }

    @EventHandler
    public void onAttack(AttackEvent event) {

    }
}
