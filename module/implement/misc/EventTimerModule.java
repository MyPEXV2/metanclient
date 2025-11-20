package relake.module.implement.misc;

import net.minecraft.network.play.server.SChatPacket;
import relake.event.EventHandler;
import relake.event.impl.misc.PacketEvent;
import relake.module.Module;
import relake.module.ModuleCategory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventTimerModule extends Module {
    private static final Pattern SPOOKYTIME_PATTERN = Pattern.compile("\\[1\\] До следующего ивента (\\d+) сек");
    private static final Pattern FUNTIME_PATTERN = Pattern.compile("\\[1\\] До следующего ивента: (\\d+) сек");
    
    private int eventTimeSeconds = 0;
    private long lastUpdateTime = 0;
    
    public EventTimerModule() {
        super("Event Timer", "Корректно отображает время до следующего события", "Correctly displays time until next event", ModuleCategory.Misc);
    }
    
    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null) return;
        
        if (event.getPacket() instanceof SChatPacket packet) {
            String message = packet.getChatComponent().getString();
            
            Matcher spookytimematcher = SPOOKYTIME_PATTERN.matcher(message);
            Matcher funtimematcher = FUNTIME_PATTERN.matcher(message);
            
            if (spookytimematcher.find()) {
                processEventTime(Integer.parseInt(spookytimematcher.group(1)));
            } else if (funtimematcher.find()) {
                processEventTime(Integer.parseInt(funtimematcher.group(1)));
            }
        }
    }
    
    private void processEventTime(int seconds) {
        eventTimeSeconds = seconds;
        lastUpdateTime = System.currentTimeMillis();
    }
    
    public int getEventTimeSeconds() {
        if (lastUpdateTime == 0) return 0;
        long elapsed = (System.currentTimeMillis() - lastUpdateTime) / 1000;
        int remaining = eventTimeSeconds - (int) elapsed;
        return Math.max(0, remaining);
    }
    
    public String getFormattedTime() {
        int seconds = getEventTimeSeconds();
        if (seconds <= 0) return "До следующего ивента: -";
        
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        
        String minuteText = getDeclension(minutes, "", "", "");
        String secondText = getDeclension(remainingSeconds, "", "", "");
        
        if (minutes > 0 && remainingSeconds > 0) {
            return String.format("До следующего ивента: Минут: %d %s Секунд: %d %s", minutes, minuteText, remainingSeconds, secondText);
        } else if (minutes > 0) {
            return String.format("До следующего ивента: Минут: %d %s Секунд: -", minutes, minuteText);
        } else {
            return String.format("До следующего ивента: Минут: - Секунд: %d %s", remainingSeconds, secondText);
        }
    }
    
    private String getDeclension(int number, String singular, String pluralFew, String pluralMany) {
        int lastTwoDigits = number % 100;
        int lastDigit = number % 10;
        
        if (lastTwoDigits >= 11 && lastTwoDigits <= 14) {
            return pluralMany;
        }
        
        switch (lastDigit) {
            case 1:
                return singular;
            case 2:
            case 3:
            case 4:
                return pluralFew;
            default:
                return pluralMany;
        }
    }
    
    @Override
    public void disable() {
        super.disable();
        eventTimeSeconds = 0;
        lastUpdateTime = 0;
    }
}

