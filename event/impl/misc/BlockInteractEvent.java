package relake.event.impl.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import relake.event.Event;

@Getter
@Setter
@AllArgsConstructor
public class BlockInteractEvent extends Event {
    private BlockPos pos;
    private Direction direction;
}
