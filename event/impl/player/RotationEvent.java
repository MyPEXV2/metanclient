package relake.event.impl.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.vector.Vector2f;
import relake.event.Event;

@Getter
@AllArgsConstructor
public class RotationEvent extends Event {
    @Setter
    private Vector2f rotate;
    private float partialTicks;
}
