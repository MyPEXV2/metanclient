package relake.event.impl.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;
import relake.event.Event;

@Getter
@RequiredArgsConstructor
public class DeathEvent extends Event {
    private final PlayerEntity player;
}
