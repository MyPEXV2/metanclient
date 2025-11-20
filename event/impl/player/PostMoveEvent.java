package relake.event.impl.player;

import lombok.Getter;
import relake.event.Event;

@Getter
public class PostMoveEvent extends Event {
    private double horizontalMove;
    
    public PostMoveEvent(double horizontalMove) {
        this.horizontalMove = horizontalMove;
    }
    
    public PostMoveEvent() {
        // Default constructor
    }
}