package relake.event.impl.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.vector.Vector2f;
import relake.event.Event;

@Getter
@Setter
@AllArgsConstructor
public class LookEvent extends Event {
    private Vector2f rotate;
}
