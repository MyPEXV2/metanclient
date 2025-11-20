package relake.event.impl.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import relake.event.Event;

@Getter
@AllArgsConstructor
public class MouseScrollEvent extends Event {
    private final double scrollDelta;
}
