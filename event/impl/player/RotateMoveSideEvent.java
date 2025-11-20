package relake.event.impl.player;

import lombok.Getter;
import lombok.Setter;
import relake.event.Event;

@Getter
@Setter
public class RotateMoveSideEvent extends Event {
    private float yaw;
    
    public RotateMoveSideEvent(float yaw) {
        this.yaw = yaw;
    }
    
    public RotateMoveSideEvent() {
        // Default constructor
    }
}