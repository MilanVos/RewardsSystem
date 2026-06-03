package nl.phoenixdev.rewardsSystem.manager;

import nl.phoenixdev.rewardsSystem.RewardsSystem;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {

    private final RewardsSystem plugin;
    private final File storageFile;
    private final List<ItemStack> items = new ArrayList<>();

    public StorageManager(RewardsSystem plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "storage.yml");
        load();
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public void addItem(ItemStack item) {
        items.add(item.clone());
        save();
    }

    public boolean removeItem(int index) {
        if (index < 0 || index >= items.size()) return false;
        items.remove(index);
        save();
        return true;
    }

    private void load() {
        if (!storageFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(storageFile);
        List<?> list = config.getList("items");
        if (list != null) {
            for (Object obj : list) {
                if (obj instanceof ItemStack) {
                    items.add((ItemStack) obj);
                }
            }
        }
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("items", items);
        try {
            config.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save storage");
        }
    }
}
