package relake.event.impl.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import relake.event.Event;

@Getter
@Setter
@AllArgsConstructor
public class ActionEvent extends Event {
    private boolean sprintState;
}
