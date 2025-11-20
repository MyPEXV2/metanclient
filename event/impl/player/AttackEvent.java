package relake.event.impl.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import relake.event.Event;

@Getter
@RequiredArgsConstructor
public class AttackEvent extends Event {
    private final Type type;
    private final Entity entity;

    public enum Type {
        PRE, POST
    }

    public boolean isPre() {
        return type.equals(Type.PRE);
    }

    public boolean isPost() {
        return type.equals(Type.POST);
    }
}
