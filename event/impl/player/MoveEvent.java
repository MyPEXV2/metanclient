package relake.event.impl.player;

import lombok.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import relake.event.Event;

@Data
@EqualsAndHashCode(callSuper = true)
public class MoveEvent extends Event {
    private Vector3d from, to, motion;
    private boolean toGround;
    private AxisAlignedBB aabbFrom;
    private boolean ignoreHorizontal, ignoreVertical, collidedHorizontal, collidedVertical;

    public MoveEvent(Vector3d from, Vector3d to, Vector3d motion, boolean toGround, boolean isCollidedHorizontal, boolean isCollidedVertical, AxisAlignedBB aabbFrom) {
        this.from = from;
        this.to = to;
        this.motion = motion;
        this.toGround = toGround;
        this.collidedHorizontal = isCollidedHorizontal;
        this.collidedVertical = isCollidedVertical;
        this.aabbFrom = aabbFrom;
    }
}
