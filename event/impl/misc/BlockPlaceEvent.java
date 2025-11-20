package relake.event.impl.misc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import relake.event.Event;

@Getter
@RequiredArgsConstructor
public class BlockPlaceEvent extends Event {
    private final BlockPos pos;
    private final Block block;
}

