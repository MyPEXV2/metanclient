package relake.draggable;

import relake.draggable.implement.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DraggableManager {
    public final List<Draggable> draggables = new CopyOnWriteArrayList<>();

    public final PotionDraggable potionDraggable = new PotionDraggable();
    public final KeyBindDraggable keyBindDraggable = new KeyBindDraggable();
    public final TargetHudDraggable targetHudDraggable = new TargetHudDraggable();;
    public final InventoryDraggable inventoryDraggable = new InventoryDraggable();
    public final TimerHudDraggable timerHudDraggable = new TimerHudDraggable();
    public final CooldownsDraggable cooldownsDraggable = new CooldownsDraggable();

    public final SpeedGraphDraggable speedGraphDraggable = new SpeedGraphDraggable();
    public final EventTimerDraggable eventTimerDraggable = new EventTimerDraggable();

    public DraggableManager() {
        registerDraggables(
                potionDraggable,
                keyBindDraggable,
                targetHudDraggable,
                inventoryDraggable,
                timerHudDraggable,
                cooldownsDraggable,
                
                speedGraphDraggable,
                eventTimerDraggable
        );
    }

    private void registerDraggables(Draggable... draggables) {
        this.draggables.addAll(Arrays.asList(draggables));
    }
}
