package relake.config.implement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import relake.Client;
import relake.point.PointTrace;
import relake.config.Config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CopyOnWriteArrayList;

public class PointConfig extends Config {
    public PointConfig() {
        super("point");
    }

    @SneakyThrows
    @Override
    public boolean save() {
        CopyOnWriteArrayList<PointTrace> points = Client.instance.pointsManager.traces;
        JsonArray modulesArray = new JsonArray();

        for (PointTrace pointTrace : points) {
            JsonObject moduleJson = new JsonObject();

            moduleJson.addProperty("name", pointTrace.name);
            moduleJson.addProperty("x", pointTrace.x);
            moduleJson.addProperty("y", pointTrace.y);
            moduleJson.addProperty("z", pointTrace.z);

            modulesArray.add(moduleJson);
        }

        File file = new File(getFileName());
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(modulesArray, writer);
            writer.flush();
        }
        return true;
    }

    @SneakyThrows
    @Override
    public boolean load() {
        if (!Files.exists(Paths.get(getFileName()))) {
            return false;
        }

        FileReader reader = new FileReader(getFileName());
        JsonArray pointsArray = gson.fromJson(reader, JsonArray.class);

        CopyOnWriteArrayList<PointTrace> points = Client.instance.pointsManager.traces;

        for (int i = 0; i < pointsArray.size(); i++) {
            JsonObject pointJson = pointsArray.get(i).getAsJsonObject();

            points.add(new PointTrace(pointJson.get("x").getAsFloat(), pointJson.get("y").getAsFloat(), pointJson.get("z").getAsFloat(), pointJson.get("name").getAsString()));
        }
        return true;
    }
}
