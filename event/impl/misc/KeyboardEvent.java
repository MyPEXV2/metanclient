package relake.event.impl.misc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import relake.event.Event;

@Getter
@RequiredArgsConstructor
public class KeyboardEvent extends Event {
    private final int key, action;
}