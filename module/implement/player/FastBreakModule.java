package relake.module.implement.player;

import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import relake.event.EventHandler;
import relake.event.impl.misc.BlockInteractEvent;
import relake.event.impl.misc.TickEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.SelectSetting;

public class FastBreakModule extends Module {

    private final SelectSetting selectComponent = new SelectSetting("Режим")
            .setValue("Пакетный",
                    "Обычный");

    public FastBreakModule() {
        super("Fast Break", "Ускоряет ломание блоков", "Accelerates the breaking of blocks", ModuleCategory.Player);
        registerComponent(selectComponent);
        selectComponent.setSelected("Пакетный");
    }

    @Override
    public void disable() {
        super.disable();

        if (selectComponent.isSelected("Пакетный")) {
            mc.player.removeActivePotionEffect(Effects.HASTE);
        }
    }

    @EventHandler
    public void block(BlockInteractEvent event) {
        if (selectComponent.isSelected("Пакетный")) {
            mc.player.swingArm(Hand.MAIN_HAND);
            mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, event.getPos(), event.getDirection()));
            mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, event.getPos(), event.getDirection()));
            event.cancel();
        }
    }

    @EventHandler
    public void tick(TickEvent tickEvent) {
        if (selectComponent.isSelected("Обычный")) {
            if (mc.playerController != null) {
                mc.playerController.blockHitDelay = 0;
                if (mc.playerController.curBlockDamageMP > Math.random()) {
                    mc.playerController.curBlockDamageMP = 1.0F;
                }
            }
        }
    }


}
