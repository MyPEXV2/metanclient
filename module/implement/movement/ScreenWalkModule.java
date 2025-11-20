package relake.module.implement.movement;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.server.SCloseWindowPacket;
import relake.Client;
import relake.common.util.ChatUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.PacketEvent;
import relake.event.impl.player.MovementInputKeysEvent;
import relake.event.impl.player.PlayerEvent;
import relake.event.impl.player.SetSprintEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import java.util.LinkedList;
import java.util.Queue;

public class ScreenWalkModule extends Module {

    public final Setting<Boolean> canSneak = new BooleanSetting("Шифт в меню").setValue(false);
    public final Setting<Boolean> stoppingWinClick = new BooleanSetting("Обход античитов").setValue(true);

    public ScreenWalkModule() {
        super("Screen Walk", "Позволяет передвигаться находясь в любых меню", "Allows you to move around while in any menu", ModuleCategory.Movement);
        registerComponent(canSneak, stoppingWinClick);
    }
    private boolean canStoppingOnWindowClick() {
        if (Client.instance.moduleManager.speedModule.isEnabled() && Client.instance.moduleManager.speedModule.mode.isSelected("FunTime extra")) {
            return false;
        }
        return this.stoppingWinClick.getValue() && (mc.player.movementInput.moveForward != 0 || mc.player.movementInput.moveStrafe != 0) || this.stoppedStatus;
    }
    private void setStop(ClickType clickType) {
        if (this.isEnabled()) this.stopTicksOut = this.ticksWindowClickOffset(clickType == null ? ClickType.PICKUP : clickType);
    }
    public void setTempStop() {
        if (this.isEnabled()) this.stopTicksOut = 2;
    }
    private int stopTicksOut;
    private boolean stoppedStatus, previousStoppedStatus;
    private int ticksWindowClickOffset(ClickType clickType) {
        return clickType.equals(ClickType.PICKUP) ? 1 : this.stopTicksOut > 1 ? 2 : 3;
    }

    private final Queue<CClickWindowPacket> windowClickPacketQueue = new LinkedList<>();
    private void useAccumulatedPackets() {
        if (this.windowClickPacketQueue.isEmpty()) return;
        if (this.windowClickPacketQueue.removeIf(CClickWindowPacket::sendSilent));
    }
    private boolean rememberCClickWindowPacket(final CClickWindowPacket packetIn) {
        return !this.windowClickPacketQueue.contains(packetIn) && this.windowClickPacketQueue.add(packetIn);
    }

    private boolean getHasItemStackDragged() {
        return mc.currentScreen instanceof ContainerScreen screenContainer && this.canStoppingOnWindowClick() && screenContainer.getHasDraggingStack;
    }

    @EventHandler
    public void onKeyMove(final MovementInputKeysEvent event) {
        if (!(mc.currentScreen instanceof ChatScreen)) event.spoofAsKeyboard(this.canSneak.getValue());
        if (this.stoppedStatus) event.stopWASD();
    }

    @EventHandler
    public void onSprintAction(final SetSprintEvent event) {
        if (this.stoppedStatus) event.setState(false);
    }

    @EventHandler
    public void onPlayerUpdate(final PlayerEvent event) {
        if (this.previousStoppedStatus && this.stoppedStatus && this.stopTicksOut > 0) this.useAccumulatedPackets();
        this.previousStoppedStatus = this.stoppedStatus;
        if (this.getHasItemStackDragged()) this.setStop(null);
        else if (this.stopTicksOut > 0) --this.stopTicksOut;
        this.stoppedStatus = this.stopTicksOut > 0;
    }

    @EventHandler
    public void onSendPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof CClickWindowPacket toSend) {
            if (this.canStoppingOnWindowClick() && toSend.getSlotId() != -1) {
                this.setStop(toSend.getClickType());
                if (!this.stoppedStatus && this.rememberCClickWindowPacket(toSend)) event.cancel();
            }
        }
    }

    @EventHandler
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SCloseWindowPacket && this.stoppingWinClick.getValue()) {
            event.cancel();
        }
    }

    @Override
    public void onDisable() {
        this.useAccumulatedPackets();
    }

    @Override
    public void onEnable() {
        this.useAccumulatedPackets();
        this.windowClickPacketQueue.clear();
    }
}
