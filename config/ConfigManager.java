package relake.config;

import relake.config.implement.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ConfigManager {
    public final List<Config> configs = new ArrayList<>();

    public final ModuleConfig moduleConfig = new ModuleConfig();
    public final FriendConfig friendConfig = new FriendConfig();
    public final DraggableConfig draggableConfig = new DraggableConfig();
    public final AccountConfig accountConfig = new AccountConfig();
    public final PointConfig pointConfig = new PointConfig();
    public final MacrosConfig macrosConfig = new MacrosConfig();

    public ConfigManager() {
        File folder = new File("C:\\zhuk");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        registerConfigs(
                moduleConfig,
                friendConfig,
                draggableConfig,
                accountConfig,
                pointConfig,
                macrosConfig
        );
    }

    private void registerConfigs(Config... configs) {
        this.configs.addAll(Arrays.asList(configs));
    }

    public List<String> getConfigsInfo(String path) {
        List<String> configFiles = new ArrayList<>();
        File folder = new File("C:\\zhuk\\" + path);

        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (file.isFile() && file.getName().endsWith("." + path)) {
                    configFiles.add(file.getName());
                }
            }
        }
        return configFiles;
    }
}
