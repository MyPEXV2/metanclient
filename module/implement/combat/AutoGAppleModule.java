package relake.module.implement.combat;

import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.FloatSetting;

public class AutoGAppleModule extends Module {

    private final Setting<Float> health = new FloatSetting("Здоровье")
            .range(4.F, 20.F)
            .setValue(12.F);

    public AutoGAppleModule() {
        super("Auto GApple", "Пополняет здоровье золотыми яблоками если они есть в второй руке", "Replenishes health with golden apples if they are in the off hand", ModuleCategory.Combat);
        registerComponent(health);
    }

    private boolean active;

    @EventHandler
    public void tick(TickEvent event) {
        if (mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && mc.player.getHealth() <= health.getValue()) {
            active = true;
            if (!mc.player.isHandActive()) {
                mc.playerController.processRightClick(mc.player, mc.world, Hand.OFF_HAND);
                mc.gameSettings.keyBindUseItem.setPressed(true);
                mc.player.setActiveHand(Hand.OFF_HAND);
            }
        } else if (active && mc.player.isHandActive()) {
            mc.playerController.onStoppedUsingItem(mc.player);
            if (!(mc.mouseHelper.isRightDown() && mc.currentScreen == null)) mc.gameSettings.keyBindUseItem.setPressed(false);
            active = false;
        }
    }
}
