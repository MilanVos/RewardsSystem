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
    private final Map<UUID, Set<Integer>> claimedRewards = new HashMap<>();
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

    public Set<Integer> getClaimedIndices(UUID uuid) {
        if (!claimedRewards.containsKey(uuid)) {
            loadPlayer(uuid);
        }
        return claimedRewards.get(uuid);
    }

    public boolean isClaimed(UUID uuid, int index) {
        return getClaimedIndices(uuid).contains(index);
    }

    public boolean claimReward(UUID uuid, int index) {
        if (isClaimed(uuid, index)) return false;
        getClaimedIndices(uuid).add(index);
        savePlayer(uuid);
        return true;
    }

    public void addReward(UUID uuid, ItemStack item) {
        getRewards(uuid).add(item.clone());
        savePlayer(uuid);
    }

    public boolean removeReward(UUID uuid, int index) {
        List<ItemStack> rewards = getRewards(uuid);
        if (index < 0 || index >= rewards.size()) return false;
        rewards.remove(index);

        Set<Integer> claimed = getClaimedIndices(uuid);
        Set<Integer> updated = new HashSet<>();
        for (int i : claimed) {
            if (i < index) updated.add(i);
            else if (i > index) updated.add(i - 1);
        }
        claimedRewards.put(uuid, updated);

        savePlayer(uuid);
        return true;
    }

    private void loadPlayer(UUID uuid) {
        File file = new File(playersFolder, uuid + ".yml");
        List<ItemStack> items = new ArrayList<>();
        Set<Integer> claimed = new HashSet<>();

        if (file.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            List<?> list = config.getList("rewards");
            if (list != null) {
                for (Object obj : list) {
                    if (obj instanceof ItemStack) items.add((ItemStack) obj);
                }
            }
            List<?> claimedList = config.getList("claimed");
            if (claimedList != null) {
                for (Object obj : claimedList) {
                    if (obj instanceof Integer) claimed.add((Integer) obj);
                }
            }
        }

        playerRewards.put(uuid, items);
        claimedRewards.put(uuid, claimed);
    }

    private void savePlayer(UUID uuid) {
        File file = new File(playersFolder, uuid + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("rewards", playerRewards.getOrDefault(uuid, new ArrayList<>()));
        config.set("claimed", new ArrayList<>(claimedRewards.getOrDefault(uuid, new HashSet<>())));
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save rewards for " + uuid);
        }
    }
}
