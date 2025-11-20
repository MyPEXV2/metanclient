package relake.module.implement.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.*;

public class AutoLeaveModule extends Module {

    private final Setting<Float> range = new FloatSetting("Радиус срабатывания")
            .range(1, 32, 1)
            .setValue(15F);

    private final SelectSetting mode = new SelectSetting("Выбор").setValue("kick","custom");

    private final Setting<String> text = new StringSetting("Команда").setVisible(() -> mode.isSelected("custom"));

    public AutoLeaveModule() {
        super("Auto Leave", "Избегает нахождения рядом с игроками", "Avoid being near the players", ModuleCategory.Player);
        registerComponent(range, mode, text);
        mode.setSelected("kick");
    }

    @EventHandler
    public void tick(TickEvent tickEvent) {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player) {
                if (mc.player.getDistance(player) <= range.getValue()) {
                    if (mode.isSelected("kick"))
                        mc.player.connection.getNetworkManager().closeChannel(new StringTextComponent("[AutoLeave] Вы были отключены от сервера \n Имя игрока: " + player.getGameProfile().getName()));
                    if (mode.isSelected("custom")) mc.player.sendChatMessage("/" + text.getValue());
                    switchState();
                    break;
                }
            }
        }
    }
}
