package relake.event.impl.player;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Pose;
import relake.event.Event;

@Getter
@Setter
public class PoseEvent extends Event {
    private Pose pose;
    
    public PoseEvent(Pose pose) {
        this.pose = pose;
    }
    
    public PoseEvent() {
        // Default constructor
    }
}