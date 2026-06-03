package nl.phoenixdev.rewardsSystem.manager;

import nl.phoenixdev.rewardsSystem.RewardsSystem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class StorageManager {

    private final RewardsSystem plugin;
    private final File storageFile;
    private final LinkedHashMap<String, ItemStack> items = new LinkedHashMap<>();

    public StorageManager(RewardsSystem plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "storage.yml");
        load();
    }

    public LinkedHashMap<String, ItemStack> getItems() {
        return items;
    }

    public ItemStack getItem(String name) {
        return items.get(name.toLowerCase());
    }

    public boolean hasItem(String name) {
        return items.containsKey(name.toLowerCase());
    }

    public void addItem(String name, ItemStack item) {
        items.put(name.toLowerCase(), item.clone());
        save();
    }

    public boolean removeItem(String name) {
        if (!items.containsKey(name.toLowerCase())) return false;
        items.remove(name.toLowerCase());
        save();
        return true;
    }

    private void load() {
        if (!storageFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(storageFile);
        ConfigurationSection section = config.getConfigurationSection("items");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            ItemStack item = section.getItemStack(key);
            if (item != null) {
                items.put(key, item);
            }
        }
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, ItemStack> entry : items.entrySet()) {
            config.set("items." + entry.getKey(), entry.getValue());
        }
        try {
            config.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save storage");
        }
    }
}
