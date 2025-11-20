package relake.event.impl.player;

public class ChatEvent {
    private String message;
    private EventType type;

    public String getMessage() {
        return this.message;
    }

    public EventType getType() {
        return this.type;
    }

    public ChatEvent(String message, EventType type) {
        this.message = message;
        this.type = type;
    }

    public static enum EventType {
        SEND,
        RECEIVE;

        // $FF: synthetic method
        private static EventType[] $values() {
            return new EventType[]{SEND, RECEIVE};
        }
    }
}

