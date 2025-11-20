package relake.config.implement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import relake.Client;
import relake.config.Config;
import relake.friend.Friend;
import relake.module.Module;
import relake.settings.Setting;
import relake.settings.implement.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FriendConfig extends Config {

    public FriendConfig() {
        super("friend");
    }

    @SneakyThrows
    @Override
    public boolean save() {
        CopyOnWriteArrayList<Friend> friends = Client.instance.friendManager.friends;
        JsonArray modulesArray = new JsonArray();

        for (Friend friend : friends) {
            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("name", friend.getName());
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
        JsonArray friendsArray = gson.fromJson(reader, JsonArray.class);
        CopyOnWriteArrayList<Friend> friends = Client.instance.friendManager.friends;

        for (int i = 0; i < friendsArray.size(); i++) {
            JsonObject friendJson = friendsArray.get(i).getAsJsonObject();

            friends.add(new Friend(friendJson.get("name").getAsString()));
        }
        return true;
    }
}
