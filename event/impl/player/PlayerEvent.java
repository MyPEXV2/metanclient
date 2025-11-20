package relake.event.impl.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import relake.event.Event;

@Getter
@Setter
@AllArgsConstructor
public class PlayerEvent extends Event {
    private Vector2f rotate;
    public void setYaw(float yaw) {
        rotate.x = yaw;
    }
    public void setPitch(float pitch) {
        rotate.y = pitch;
    }
    private Vector3d pos;
    private boolean onGround;
    private Runnable postMotion;
}
