package relake.event.impl.player;

import lombok.Getter;
import lombok.Setter;
import relake.event.Event;

@Getter
@Setter
public class SpeedFactorEvent extends Event {
    private float speed;
    
    public SpeedFactorEvent(float speed) {
        this.speed = speed;
    }
    
    public SpeedFactorEvent() {
        // Default constructor
    }
}