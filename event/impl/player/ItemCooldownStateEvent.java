package relake.event.impl.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.Item;
import relake.event.Event;

@Getter
@RequiredArgsConstructor
public class ItemCooldownStateEvent extends Event {
    private final Item item;
    private final State state;

    public enum State {
        ADD,
        REMOVE
    }
}
