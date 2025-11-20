package relake.common.component;

import lombok.RequiredArgsConstructor;
import relake.Client;
import relake.common.InstanceAccess;
import relake.common.util.StopWatch;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;

public class ClickPearlComponent implements InstanceAccess {
    public static final StopWatch delayTimer = new StopWatch();
    public static ProcessState currentState = ProcessState.IDLE;
    public static int previousSlot;
    public static int pearlSlot;

    public ClickPearlComponent() {
        Client.instance.eventManager.register(this);
    }

    public static void startLegitPearlThrow(int slot) {
        if (currentState != ProcessState.IDLE) {
            return;
        }

        pearlSlot = slot;
        previousSlot = mc.player.inventory.currentItem;

        if (slot < 9) {
            mc.player.inventory.currentItem = slot;
            mc.playerController.syncCurrentPlayItem();
            currentState = ProcessState.THROWING;
        } else {
            mc.playerController.pickItem(slot);
            currentState = ProcessState.SWITCHING;
        }

        delayTimer.reset();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        switch (currentState) {
            case SWITCHING -> {
                if (delayTimer.finished(70)) {
                    mc.rightClickMouse();
                    currentState = ProcessState.THROWING;
                    delayTimer.reset();
                }
            }
            case THROWING -> {
                if (delayTimer.finished(30)) {
                    if (pearlSlot >= 9) {
                        mc.playerController.pickItem(pearlSlot);
                    } else {
                        mc.rightClickMouse();
                        mc.player.inventory.currentItem = previousSlot;
                        mc.playerController.syncCurrentPlayItem();
                    }
                    currentState = ProcessState.IDLE;
                    Client.instance.moduleManager.clickPearlModule.reset();
                }
            }
        }
    }

    @RequiredArgsConstructor
    public enum ProcessState {
        IDLE,
        SWITCHING,
        THROWING
    }
}
