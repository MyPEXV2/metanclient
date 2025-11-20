package relake.config.implement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import relake.Client;
import relake.config.Config;
import relake.draggable.Draggable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class DraggableConfig extends Config {

    public DraggableConfig() {
        super("draggable");
    }

    @SneakyThrows
    @Override
    public boolean save() {
        List<Draggable> draggables = Client.instance.getDraggableManager().draggables;
        JsonArray modulesArray = new JsonArray();

        for (Draggable draggable : draggables) {
            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("name", draggable.name);
            moduleJson.addProperty("x", draggable.x);
            moduleJson.addProperty("y", draggable.y);

            modulesArray.add(moduleJson);
        }

        File file = new File(getFileName());
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(modulesArray, writer);
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
        JsonArray draggablesArray = gson.fromJson(reader, JsonArray.class);
        List<Draggable> draggables = Client.instance.getDraggableManager().draggables;

        for (int i = 0; i < draggablesArray.size(); i++) {
            JsonObject draggableJson = draggablesArray.get(i).getAsJsonObject();

            for (Draggable draggable : draggables) {
                if (draggable.name.equalsIgnoreCase(draggableJson.get("name").getAsString())) {
                    draggable.animatedX = draggable.x = draggableJson.get("x").getAsFloat();
                    draggable.animatedY = draggable.y = draggableJson.get("y").getAsFloat();
                }
            }
        }
        return true;
    }
}
