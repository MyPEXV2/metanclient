package relake.notification;

import com.mojang.blaze3d.matrix.MatrixStack;
import relake.Client;

import java.util.ArrayList;

public final class NotificationManager extends ArrayList<Notification> {
    private static NotificationManager instance;

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
            instance.init();
        }
        return instance;
    }

    private void init() {
        Client.instance.eventManager.register(this);
    }

    public static void send(final String title, final String content, final NotificationType type, long delay) {
        int index = getInstance().size();
        Notification notification = new Notification(title, content, delay, index, type);

        getInstance().add(notification);
    }

    public static void send(final String title, final String content, final boolean circle, final int color, final NotificationType type, long delay) {
        int index = getInstance().size();
        Notification notification = new Notification(title, content, circle, color, delay, index, type);

        getInstance().add(notification);
    }

    public void render(MatrixStack matrix) {
        if (this.isEmpty()) return;
        this.removeIf(Notification::hasExpired);
        int i = 0;
        for (Notification notification : this) {
            notification.render(matrix, i);
            i++;
        }
    }
}
