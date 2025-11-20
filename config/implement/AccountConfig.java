package relake.config.implement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import relake.Client;
import relake.account.Account;
import relake.config.Config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class AccountConfig extends Config {

    public AccountConfig() {
        super("account");
    }

    @SneakyThrows
    @Override
    public boolean save() {
        List<Account> accounts = Client.instance.accountManager.accounts;
        JsonArray modulesArray = new JsonArray();

        for (Account account : accounts) {
            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("name", account.getName());
            modulesArray.add(moduleJson);
        }

        JsonObject last = new JsonObject();
        last.addProperty("last-login", Client.instance.accountManager.getLastLogin());
        modulesArray.add(last);

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
        JsonArray accountsArray = gson.fromJson(reader, JsonArray.class);
        List<Account> accounts = Client.instance.accountManager.accounts;

        for (int i = 0; i < accountsArray.size(); i++) {
            JsonObject accountJson = accountsArray.get(i).getAsJsonObject();
            if (accountJson.has("name")) accounts.add(new Account(accountJson.get("name").getAsString()));
            if (accountJson.has("last-login")) Minecraft.getInstance().session = new Session(accountJson.get("last-login").getAsString(), String.valueOf(UUID.randomUUID()), "", "");
        }
        return true;
    }
}
