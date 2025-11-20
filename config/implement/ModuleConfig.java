package relake.config.implement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import relake.Client;
import relake.config.Config;
import relake.menu.ui.components.Component;
import relake.menu.ui.components.module.setting.MultiSelectComponent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.settings.Setting;
import relake.settings.implement.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ModuleConfig extends Config {

    public ModuleConfig() {
        super("module");
    }

    @SneakyThrows
    @Override
    public boolean save() {
        List<Module> modules = Client.instance.moduleManager.modules;
        JsonArray modulesArray = new JsonArray();

        for (Module module : modules) {
            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("name", module.getName());
            moduleJson.addProperty("enabled", module.isEnabled());
            moduleJson.addProperty("key", module.getKey());

            JsonArray settingsArray = new JsonArray();
            for (Setting<?> setting : module.settings) {
                JsonObject settingJson = new JsonObject();
                settingJson.addProperty("name", setting.getName());

                if (setting instanceof SelectSetting selectSetting) {
                    settingJson.addProperty("value", selectSetting.getValue());
                } else if (setting instanceof MultiSelectSetting multiSelectSetting) {
                    JsonArray selectedValues = new JsonArray();
                    for (String value : multiSelectSetting.getValue()) {
                        selectedValues.add(value);
                    }
                    settingJson.add("value", selectedValues);
                } else if (setting instanceof ColorSetting colorSetting) {
                    int colorValue = colorSetting.getValue().getRGB();
                    settingJson.addProperty("value", colorValue);
                } else {
                    settingJson.add("value", gson.toJsonTree(setting.getValue()));
                }
                settingsArray.add(settingJson);
            }

            moduleJson.add("settings", settingsArray);
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
        JsonArray modulesArray = gson.fromJson(reader, JsonArray.class);
        List<Module> modules = Client.instance.moduleManager.modules;

        for (int i = 0; i < modulesArray.size(); i++) {
            JsonObject moduleJson = modulesArray.get(i).getAsJsonObject();
            String moduleName = moduleJson.get("name").getAsString();
            Module module = modules.stream()
                    .filter(m -> m.getName().equals(moduleName))
                    .findFirst()
                    .orElse(null);

            if (module != null) {
                try {
                    module.switchState(moduleJson.get("enabled").getAsBoolean(), false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                module.setKey(moduleJson.get("key").getAsInt());

                JsonArray settingsArray = moduleJson.getAsJsonArray("settings");

                for (int j = 0; j < settingsArray.size(); j++) {
                    if (!settingsArray.get(j).isJsonObject()) continue;
                    JsonObject settingJson = settingsArray.get(j).getAsJsonObject();
                    String settingName = settingJson.get("name").getAsString();

                    Setting<?> setting = module.settings.stream()
                            .filter(s -> s.getName().equals(settingName))
                            .findFirst()
                            .orElse(null);

                    if (setting != null) {
                        if (setting instanceof BooleanSetting booleanSetting) {
                            booleanSetting.setValue(settingJson.get("value").getAsBoolean());
                        } else if (setting instanceof FloatSetting floatSetting) {
                            floatSetting.setValue(settingJson.get("value").getAsFloat());
                        } else if (setting instanceof StringSetting stringSetting) {
                            if (settingJson.get("value") != null) {
                                String value = settingJson.get("value").getAsString();
                                stringSetting.setValue(value);
                            }
                        } else if (setting instanceof KeySetting keySetting) {
                            keySetting.setValue(settingJson.get("value").getAsInt());
                        } else if (setting instanceof ColorSetting colorSetting) {
                            int colorValue = settingJson.get("value").getAsInt();
                            colorSetting.setValue(colorValue);
                        } else if (setting instanceof SelectSetting selectSetting) {
                            String value = settingJson.get("value").getAsString();
                            if (selectSetting.getList().contains(value)) {
                                selectSetting.setValue(selectSetting.getList().toArray(new String[0]));
                                selectSetting.setSelected(value);
                            }
                        } else if (setting instanceof MultiSelectSetting multiSelectSetting) {
                            JsonArray selectedValues = settingJson.getAsJsonArray("value");
                            List<String> values = new ArrayList<>();
                            for (JsonElement element : selectedValues) {
                                String value = element.getAsString();
                                if (multiSelectSetting.getList().contains(value)) {
                                    values.add(value);
                                }
                            }
                            multiSelectSetting.setValue(multiSelectSetting.getList().toArray(new String[0]));
                            multiSelectSetting.getSelected().clear();
                            multiSelectSetting.getSelected().addAll(values);
                        }
                    }
                }
            }
        }
        return true;
    }
}
