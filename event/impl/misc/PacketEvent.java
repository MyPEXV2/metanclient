package relake.event.impl.misc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.IPacket;
import relake.event.Event;

@Getter
@RequiredArgsConstructor
public class PacketEvent extends Event {
    private final IPacket<?> packet;

    public static class Send extends PacketEvent {
        public Send(IPacket<?> packet) {
            super(packet);
        }
    }

    public static class Receive extends PacketEvent {
        public Receive(IPacket<?> packet) {
            super(packet);
        }
    }
}
