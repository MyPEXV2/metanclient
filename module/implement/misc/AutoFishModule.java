package relake.module.implement.misc;

import net.minecraft.item.FishingRodItem;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.Hand;
import relake.event.EventHandler;
import relake.event.impl.misc.PacketEvent;
import relake.module.Module;
import relake.module.ModuleCategory;

import static net.minecraft.util.SoundCategory.NEUTRAL;
import static net.minecraft.util.SoundEvents.ENTITY_FISHING_BOBBER_SPLASH;

public class AutoFishModule extends Module {

    public AutoFishModule() {
        super("Auto Fish", "Автоматически подхватывает и вылавливает добычу удочкой, затем он забрасывает поплавок заново", "It automatically picks up and catches prey on the hook of the fishing rod, then it casts the float again", ModuleCategory.Misc);
    }

    @EventHandler
    public void packetEvent(PacketEvent.Receive packetEvent) {
        if (packetEvent.getPacket() instanceof SPlaySoundEffectPacket packet) {
            if (packet.getCategory() == NEUTRAL && packet.getSound() == ENTITY_FISHING_BOBBER_SPLASH && mc.player.getDistanceSq(packet.getX(), packet.getY(), packet.getZ()) < 6) {
                click(mc.player.getHeldItemMainhand().getItem() instanceof FishingRodItem ? Hand.MAIN_HAND : Hand.OFF_HAND);
            }
        }
    }

    private void click(Hand hand) {
        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(hand));
        mc.player.swingArm(hand);
        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(hand));
        mc.player.swingArm(hand);
    }
}
