package relake.module.implement.render;

import net.minecraft.network.play.server.SUpdateTimePacket;
import relake.event.EventHandler;
import relake.event.impl.misc.PacketEvent;
import relake.event.impl.misc.TickEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.SelectSetting;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.Timer;
import java.util.TimerTask;

public class AmbienceModule extends Module {

    private final SelectSetting mode = new SelectSetting("Время суток")
            .setValue("День",
                    "Ночь",
                    "Вечер",
                    "Рассвет",
                    "Реальное время");


    private final Timer timer = new Timer();

    public AmbienceModule() {
        super("Ambience", "Изменяет внутриигровое течение времени", "Changes the in-game flow of time", ModuleCategory.Render);
        registerComponent(mode);
        mode.setSelected("Ночь");

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mode.isSelected("Реальное время")) {
                    calculateLocalTime();
                }
            }
        }, 0, 1000);
    }

    @EventHandler
    public void tick(TickEvent event) {
        if (mode.isSelected("День")) {
            mc.world.setDayTime(1000);
        } else if (mode.isSelected("Ночь")) {
            mc.world.setDayTime(20000);
        } else if (mode.isSelected("Закат")) {
            mc.world.setDayTime(13000);
        } else if (mode.isSelected("Рассвет")) {
            mc.world.setDayTime(23500);
        }
    }

    private long calculateLocalTime() {
        LocalTime currentTime = LocalTime.now();
        int hours = currentTime.getHour();
        int minutes = currentTime.getMinute();
        int seconds = currentTime.getSecond();

        // Определяем текущее время года
        Month currentMonth = LocalDate.now().getMonth();
        double dayMultiplier = getDayMultiplierBySeason(currentMonth);

        // Преобразуем локальное время в игровые тики
        long baseGameTime = (hours * 1000) + (minutes * 1000 / 60) + (seconds * 1000 / 3600);
        long adjustedGameTime = (long) (baseGameTime * dayMultiplier);

        return adjustedGameTime;
    }

    private double getDayMultiplierBySeason(Month month) {
        // Усредненные значения времени рассвета и заката
        int sunriseHour, sunsetHour;

        switch (month) {
            case DECEMBER:
            case JANUARY:
            case FEBRUARY:
                sunriseHour = 8;  // Зимой рассвет около 08:00
                sunsetHour = 16;  // Закат около 16:00
                break;

            case MARCH:
            case APRIL:
            case MAY:
                sunriseHour = 6;  // Весной рассвет около 06:00
                sunsetHour = 19;  // Закат около 19:00
                break;

            case JUNE:
            case JULY:
            case AUGUST:
                sunriseHour = 5;  // Летом рассвет около 05:00
                sunsetHour = 21;  // Закат около 21:00
                break;

            case SEPTEMBER:
            case OCTOBER:
            case NOVEMBER:
                sunriseHour = 7;  // Осенью рассвет около 07:00
                sunsetHour = 18;  // Закат около 18:00
                break;

            default:
                sunriseHour = 6;  // По умолчанию
                sunsetHour = 18;
        }

        // Рассчитываем длительность дня и ночи
        int dayDuration = sunsetHour - sunriseHour; // Длительность дня в часах
        int nightDuration = 24 - dayDuration;       // Длительность ночи в часах

        // Определяем множитель для преобразования локального времени в игровые тики
        return (double) dayDuration / 12; // 12 — это стандартная длина дня в Minecraft (с 6:00 до 18:00)
    }

    @EventHandler
    public void packet(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SUpdateTimePacket) {
            event.cancel();
        }
    }
    public void onDisable() {
        timer.cancel();
    }
}
