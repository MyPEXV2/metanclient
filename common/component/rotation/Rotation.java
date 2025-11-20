package relake.common.component.rotation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rotation {
    private float yaw, pitch;

    public Rotation(Entity entity) {
        yaw = entity.rotationYaw;
        pitch = entity.rotationPitch;
    }

    public double getDelta(Rotation targetRotation) {
        double yawDelta = MathHelper.wrapDegrees(targetRotation.getYaw() - yaw);
        double pitchDelta = MathHelper.wrapDegrees(targetRotation.getPitch() - pitch);

        return Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
    }

}