package relake.event.impl.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import relake.event.Event;

@Getter
@AllArgsConstructor
public class RenderHandEvent extends Event {
	private final boolean pre;
}
