package nl.phoenixdev.rewardsSystem;

import nl.phoenixdev.rewardsSystem.command.RewardsCommand;
import nl.phoenixdev.rewardsSystem.gui.InventoryPickerGUI;
import nl.phoenixdev.rewardsSystem.gui.RewardGUI;
import nl.phoenixdev.rewardsSystem.gui.StorageGUI;
import nl.phoenixdev.rewardsSystem.listener.GUIListener;
import nl.phoenixdev.rewardsSystem.manager.RewardManager;
import nl.phoenixdev.rewardsSystem.manager.StorageManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RewardsSystem extends JavaPlugin {

    private RewardManager rewardManager;
    private StorageManager storageManager;

    private final Map<UUID, RewardGUI> openRewardGUIs = new HashMap<>();
    private final Map<UUID, StorageGUI> openStorageGUIs = new HashMap<>();
    private final Map<UUID, InventoryPickerGUI> openPickerGUIs = new HashMap<>();

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();
        rewardManager = new RewardManager(this);
        storageManager = new StorageManager(this);
        getCommand("rewards").setExecutor(new RewardsCommand(this));
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
    }

    @Override
    public void onDisable() {
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public Map<UUID, RewardGUI> getOpenRewardGUIs() {
        return openRewardGUIs;
    }

    public Map<UUID, StorageGUI> getOpenStorageGUIs() {
        return openStorageGUIs;
    }

    public Map<UUID, InventoryPickerGUI> getOpenPickerGUIs() {
        return openPickerGUIs;
    }
}
