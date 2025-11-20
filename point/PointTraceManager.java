package relake.point;

import relake.Client;
import relake.common.InstanceAccess;

import java.util.concurrent.CopyOnWriteArrayList;


public class PointTraceManager implements InstanceAccess {
    public final CopyOnWriteArrayList<PointTrace> traces = new CopyOnWriteArrayList<>();

    public boolean addPoint(float x, float y, float z, String name) {
        if (traces.stream().noneMatch(pointTrace -> pointTrace.name.toLowerCase().equalsIgnoreCase(name.toLowerCase()))) {
            traces.add(new PointTrace(x, y, z, name));
            Client.instance.configManager.pointConfig.save();
            return true;
        }

        return false;
    }

    public boolean clearPoints() {
        if (traces.isEmpty()) return false;
        traces.clear();
        Client.instance.configManager.pointConfig.save();
        return true;
    }

    public boolean removePoint(String name) {
        PointTrace toRemove = traces.stream().filter(pointTrace -> pointTrace.name.toLowerCase().equalsIgnoreCase(name.toLowerCase())).findAny().orElse(null);
        if (toRemove != null) {
            traces.remove(toRemove);
            Client.instance.configManager.pointConfig.save();
            return true;
        }
        return false;
    }

    public boolean removeLastPoint() {
        PointTrace toRemove = traces.isEmpty() ? null : traces.get(traces.size() - 1);
        if (toRemove != null) {
            traces.remove(toRemove);
            Client.instance.configManager.pointConfig.save();
            return true;
        }
        return false;
    }
}
