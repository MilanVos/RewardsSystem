package nl.phoenixdev.rewardsSystem.manager;

import nl.phoenixdev.rewardsSystem.RewardsSystem;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RewardManager {

    private final RewardsSystem plugin;
    private final Map<UUID, List<ItemStack>> playerRewards = new HashMap<>();
    private final File playersFolder;

    public RewardManager(RewardsSystem plugin) {
        this.plugin = plugin;
        this.playersFolder = new File(plugin.getDataFolder(), "players");
        this.playersFolder.mkdirs();
    }

    public List<ItemStack> getRewards(UUID uuid) {
        if (!playerRewards.containsKey(uuid)) {
            loadPlayer(uuid);
        }
        return playerRewards.get(uuid);
    }

    public void addReward(UUID uuid, ItemStack item) {
        getRewards(uuid).add(item.clone());
        savePlayer(uuid);
    }

    public boolean removeReward(UUID uuid, int index) {
        List<ItemStack> rewards = getRewards(uuid);
        if (index < 0 || index >= rewards.size()) return false;
        rewards.remove(index);
        savePlayer(uuid);
        return true;
    }

    private void loadPlayer(UUID uuid) {
        File file = new File(playersFolder, uuid + ".yml");
        List<ItemStack> items = new ArrayList<>();
        if (file.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            List<?> list = config.getList("rewards");
            if (list != null) {
                for (Object obj : list) {
                    if (obj instanceof ItemStack) {
                        items.add((ItemStack) obj);
                    }
                }
            }
        }
        playerRewards.put(uuid, items);
    }

    private void savePlayer(UUID uuid) {
        File file = new File(playersFolder, uuid + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("rewards", playerRewards.getOrDefault(uuid, new ArrayList<>()));
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save rewards for " + uuid);
        }
    }
}
