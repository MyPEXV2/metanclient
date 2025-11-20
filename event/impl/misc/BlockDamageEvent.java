package relake.event.impl.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import relake.event.Event;

@Getter
@Setter
@AllArgsConstructor
public class BlockDamageEvent extends Event {
    private BlockState blockState;
    private BlockPos pos;
    private State state;

    public enum State {
        START, STOP
    }
}
