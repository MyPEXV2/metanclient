package relake.event.api;

import relake.event.Event;

public interface Invoker {
    void invoke(Event event);
}
