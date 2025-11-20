package relake.event.impl.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import relake.event.Event;

@Getter
@Setter
@AllArgsConstructor
public class MovementInputEvent extends Event {
    private float forward, strafe;
    private boolean jump, sneak;
    public boolean hasMovement() {
        return forward != 0 || strafe != 0;
    }
}
