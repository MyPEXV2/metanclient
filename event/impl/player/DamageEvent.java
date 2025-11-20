package relake.event.impl.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import relake.event.Event;

@Getter
@AllArgsConstructor
public class DamageEvent extends Event {
    private final DamageType damageType;

    public enum DamageType {
        ENDER_PEARL, ARROW, FALL
    }
}
