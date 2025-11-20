package relake.config.implement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import relake.Client;
import relake.config.Config;
import relake.macros.Macros;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CopyOnWriteArrayList;

public class MacrosConfig extends Config {

    public MacrosConfig() {
        super("macros");
    }

    @SneakyThrows
    @Override
    public boolean save() {
        CopyOnWriteArrayList<Macros> macros = Client.instance.macrosManager.macros;
        JsonArray modulesArray = new JsonArray();

        for (Macros macro : macros) {
            JsonObject moduleJson = new JsonObject();

            moduleJson.addProperty("name", macro.getName());
            moduleJson.addProperty("key", macro.getKey());
            moduleJson.addProperty("message", macro.getMessage());

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
        JsonArray macrosArray = gson.fromJson(reader, JsonArray.class);

        CopyOnWriteArrayList<Macros> macros = Client.instance.macrosManager.macros;

        for (int i = 0; i < macrosArray.size(); i++) {
            JsonObject macrosJson = macrosArray.get(i).getAsJsonObject();

            macros.add(new Macros(macrosJson.get("name").getAsString(), macrosJson.get("key").getAsInt(), macrosJson.get("message").getAsString()));
        }
        return true;
    }
}
