package relake.event.impl.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import relake.event.Event;

@Getter
@Setter
@AllArgsConstructor
public class MovementInputKeysEvent extends Event {
    private boolean w, a, s, d, space, shift;
    public boolean[] getKeysState() {
        return new boolean[] {w, a, s, d, space, shift};
    }
    public void setKeysState(boolean[] set) {
        this.w = set[0];
        this.a = set[1];
        this.s = set[2];
        this.d = set[3];
        this.space = set[4];
        this.shift = set[5];
    }
    public void stopWASD() {
        this.w = false;
        this.a = false;
        this.s = false;
        this.d = false;
    }

    public void spoofAsKeyboard(boolean spoofSneak) {
        final long windowId = Minecraft.getInstance().getMainWindow().getHandle();
        final GameSettings gameSettings = Minecraft.getInstance().gameSettings;
        w = InputMappings.isKeyDown(windowId, gameSettings.keyBindForward.keyCode.getKeyCode());
        a = InputMappings.isKeyDown(windowId, gameSettings.keyBindLeft.keyCode.getKeyCode());
        s = InputMappings.isKeyDown(windowId, gameSettings.keyBindBack.keyCode.getKeyCode());
        d = InputMappings.isKeyDown(windowId, gameSettings.keyBindRight.keyCode.getKeyCode());
        space = InputMappings.isKeyDown(windowId, gameSettings.keyBindJump.keyCode.getKeyCode());
        if (spoofSneak) shift = InputMappings.isKeyDown(windowId, gameSettings.keyBindSneak.keyCode.getKeyCode());
    }

    public boolean hasMovementInputPressing() {
        final int ws = w == s ? 0 : w ? 1 : -1, da = d == a ? 0 : d ? 1 : -1;
        return ws != 0 || da != 0;
    }
}
