package relake.macros;

import relake.Client;

import java.util.concurrent.CopyOnWriteArrayList;

public class MacrosManager {
    public final CopyOnWriteArrayList<Macros> macros = new CopyOnWriteArrayList<>();

    public void addMacros(String name, int key, String message) {
        macros.add(new Macros(name, key, message));
        Client.instance.configManager.macrosConfig.save();
    }

    public void removeMacros(String name) {
        macros.removeIf(macro -> macro.getName().equalsIgnoreCase(name));
        Client.instance.configManager.macrosConfig.save();
    }

    public boolean contains(String name) {
        return macros.stream().anyMatch(macros -> macros.getName().equalsIgnoreCase(name));
    }

    public void clearMacros() {
        macros.clear();
        Client.instance.configManager.macrosConfig.save();
    }
}
