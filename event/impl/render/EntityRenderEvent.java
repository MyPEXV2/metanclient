package relake.event.impl.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import relake.event.Event;
@AllArgsConstructor
@Getter
public class EntityRenderEvent extends Event {
    private Entity entity;
    private Runnable runnable;
}
