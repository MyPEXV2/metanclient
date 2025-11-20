package relake.module.implement.misc;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.text.TextFormatting;
import relake.common.util.ChatUtil;
import relake.common.util.StopWatch;
import relake.event.EventHandler;
import relake.event.impl.misc.PacketEvent;
import relake.event.impl.misc.TickEvent;
import relake.module.Module;
import relake.module.ModuleCategory;

public class AutoTransferModule extends Module {
    private final StopWatch timeUtils = new StopWatch();
    private boolean waiting;
    private boolean isSell;
    private Action action;

    public int from, target = -1;

    public AutoTransferModule() {
        super("Auto Transfer", "Работает только на сервере FunTime, позволяет быстро перемещаться между анархиями", "Works only on the Fun Time server, allows you to quickly navigate between anarchies", ModuleCategory.Misc);
    }

    @Override
    public void enable() {
        super.enable();

        reset();

        if (mc.getCurrentServerData() == null || !mc.getCurrentServerData().serverIP.contains("funtime")) {
            ChatUtil.send("AutoTransfer работает только на FunTime!");
            switchState();
            return;
        }

        int number = getAnarchyNumber();

        if (number == -1) {
            ChatUtil.send("Зайди на анархию!");
            switchState();
            return;
        }

        from = number;

        if (target == -1) {
            ChatUtil.send("Укажите номер анархии! .transfer <номер>");
            switchState();
            return;
        }

        if (from == target) {
            ChatUtil.send("Вы уже на этой анархии!");
            switchState();
        }
    }

    @Override
    public void disable() {
        super.disable();
        reset();
    }

    @EventHandler
    public void packetEvent(PacketEvent.Receive packetEvent) {
        if (packetEvent.getPacket() instanceof SChatPacket sChatPacket) {
            String rawText = TextFormatting.getTextWithoutFormattingCodes(sChatPacket.getChatComponent().getString());

            if (rawText == null) {
                return;
            }

            if (rawText.contains("выставлен на продажу!")) {
                waiting = false;
                return;
            }

            if (rawText.contains("Освободите хранилище или уберите предметы с продажи")) {
                waiting = false;
                action = Action.SWITCH;
                return;
            }

            if (rawText.contains("После входа на режим необходимо немного подождать перед использованием аукциона. Подождите")) {
                switchState();
            }
        }
    }

    @EventHandler
    public void tick(TickEvent tickEvent) {
        int currentAnarchy = getAnarchyNumber();

        if (action == null) {
            action = Action.SELL;
        }

        switch (action) {
            case SELL -> {
                if (waiting || !timeUtils.finished(150) || currentAnarchy != from) {
                    return;
                }

                for (int i = 0; i < 9; i++) {
                    ItemStack itemStack = mc.player.inventory.mainInventory.get(i);

                    if (!itemStack.isEmpty()) {
                        mc.player.inventory.currentItem = i;
                        mc.playerController.syncCurrentPlayItem();
                        mc.player.sendChatMessage("/ah dsell 10");
                        isSell = true;
                        timeUtils.reset();
                        waiting = true;
                        break;
                    }
                }

                if (!waiting && isSell) {
                    action = Action.SWITCH;
                    isSell = false;
                }
            }
            case SWITCH -> {
                if (!waiting && currentAnarchy == from) {
                    mc.player.sendChatMessage("/an" + target);
                    action = Action.BUY;
                }
            }
            case BUY -> {
                if (!waiting && currentAnarchy == target) {
                    mc.player.sendChatMessage("/ah " + mc.getSession().getUsername());
                    timeUtils.reset();
                    waiting = true;
                    return;
                }

                if (mc.player.openContainer instanceof ChestContainer chestContainer) {
                    if (chestContainer.getSlot(0).getHasStack()) {
                        mc.playerController.windowClick(
                                ((ContainerScreen<?>) mc.currentScreen).getContainer().windowId,
                                0,
                                0,
                                ClickType.QUICK_MOVE,
                                mc.player
                        );
                    }
                }

                if (timeUtils.finished(1_000)) {
                    mc.player.closeScreen();
                    timeUtils.reset();
                    switchState();
                }
            }
        }

    }

    private int getAnarchyNumber() {
        ScoreObjective objective = mc.world.getScoreboard().getObjective("TAB-Scoreboard");

        if (objective != null) {
            String rawTitle = objective.getDisplayName().getString();
            String clearTitle = TextFormatting.getTextWithoutFormattingCodes(rawTitle);

            if (clearTitle != null && clearTitle.contains("Анархия-")) {
                return Integer.parseInt(clearTitle.substring(clearTitle.lastIndexOf('-') + 1));
            }
        }

        return -1;
    }

    private void reset() {
        waiting = false;
        isSell = false;
        action = null;
    }

    private enum Action {
        SELL, SWITCH, BUY;
    }
}
