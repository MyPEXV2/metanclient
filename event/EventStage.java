package relake.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EventStage extends Event {
    public final Stage stage;

    public enum Stage {
        PRE, POST
    }
}
