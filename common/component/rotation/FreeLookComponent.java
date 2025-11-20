package relake.common.component.rotation;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import relake.Client;
import relake.common.InstanceAccess;
import relake.common.util.MathUtil;
import relake.event.EventHandler;
import relake.event.impl.player.LookEvent;
import relake.event.impl.player.RotationEvent;
import relake.event.impl.render.WorldRenderEvent;

public class FreeLookComponent implements InstanceAccess {

    @Getter
    private static boolean active;
    @Getter @Setter
    private static float freeYaw, freePitch;

    public FreeLookComponent() {
        Client.instance.eventManager.register(this);
    }

    @EventHandler
    public void look(LookEvent event) {
        if (clientLooktestRotates()) {
            rotateTowards(mc.player.rotationYaw - freeYaw, mc.player.rotationPitch - freePitch);
            return;
        }
        if (active) {
            rotateTowards(event.getRotate().x, event.getRotate().y);
            event.cancel();
        }
    }
    private boolean clientLooktestRotates() {
        boolean testMoment = false;
        return Client.instance.moduleManager.attackAuraModule.isViewLockMoment() || testMoment;
    }
    @EventHandler
    public void rotation(RotationEvent event) {
        if (active) {
            event.setRotate(new Vector2f(freeYaw, freePitch));
        } else {
            freeYaw = event.getRotate().x;
            freePitch = event.getRotate().y;
        }
    }

    @EventHandler
    public void onRender(WorldRenderEvent event) {
    }

    public static void setActive(boolean state) {
        if (active != state) {
            active = state;
            resetRotation();
        }
    }

    private void rotateTowards(double yaw, double pitch) {
        double d0 = pitch * 0.15D;
        double d1 = yaw * 0.15D;
        freePitch = (float) ((double) freePitch + d0);
        freeYaw = (float) ((double) freeYaw + d1);
        freePitch = MathHelper.clamp(freePitch, -90.0F, 90.0F);
    }

    private static void resetRotation() {
        mc.player.rotationYaw += MathHelper.wrapDegrees(freeYaw - mc.player.rotationYaw);
        mc.player.rotationPitch = freePitch;
    }
}
