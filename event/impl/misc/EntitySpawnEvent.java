package relake.event.impl.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import relake.event.Event;

@AllArgsConstructor
@Getter
public class EntitySpawnEvent extends Event {
    private final Entity entity;
}
