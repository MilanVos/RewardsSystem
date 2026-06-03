package nl.phoenixdev.rewardsSystem.listener;

import nl.phoenixdev.rewardsSystem.RewardsSystem;
import nl.phoenixdev.rewardsSystem.gui.InventoryPickerGUI;
import nl.phoenixdev.rewardsSystem.gui.RewardGUI;
import nl.phoenixdev.rewardsSystem.gui.StorageGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GUIListener implements Listener {

    private final RewardsSystem plugin;

    public GUIListener(RewardsSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();

        RewardGUI rewardGUI = plugin.getOpenRewardGUIs().get(uuid);
        if (rewardGUI != null && rewardGUI.getInventory().equals(event.getView().getTopInventory())) {
            handleRewardGUIClick(event, player, rewardGUI);
            return;
        }

        StorageGUI storageGUI = plugin.getOpenStorageGUIs().get(uuid);
        if (storageGUI != null && storageGUI.getInventory().equals(event.getView().getTopInventory())) {
            handleStorageGUIClick(event, player, storageGUI);
            return;
        }

        InventoryPickerGUI pickerGUI = plugin.getOpenPickerGUIs().get(uuid);
        if (pickerGUI != null && pickerGUI.getInventory().equals(event.getView().getTopInventory())) {
            handlePickerGUIClick(event, player, pickerGUI);
        }
    }

    private void handleRewardGUIClick(InventoryClickEvent event, Player player, RewardGUI gui) {
        event.setCancelled(true);

        if (!event.getView().getTopInventory().equals(event.getClickedInventory())) return;

        int slot = event.getSlot();

        if (slot == RewardGUI.SLOT_CLOSE) {
            player.closeInventory();
        } else if (slot == RewardGUI.SLOT_PREV) {
            gui.prevPage();
        } else if (slot == RewardGUI.SLOT_NEXT) {
            gui.nextPage();
        } else if (slot >= 0 && slot < 45 && player.getUniqueId().equals(gui.getTargetUUID())) {
            int rewardIndex = gui.getPage() * 45 + slot;
            List<ItemStack> rewards = plugin.getRewardManager().getRewards(gui.getTargetUUID());
            if (rewardIndex >= rewards.size()) return;
            if (plugin.getRewardManager().isClaimed(gui.getTargetUUID(), rewardIndex)) return;

            ItemStack reward = rewards.get(rewardIndex).clone();
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(reward);
            if (!leftover.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Je inventory is vol! Maak ruimte en probeer opnieuw.");
                return;
            }
            plugin.getRewardManager().claimReward(gui.getTargetUUID(), rewardIndex);
            gui.refresh();
            player.sendMessage(ChatColor.GREEN + "Je hebt " + reward.getType().name() + " geclaimd!");
        }
    }

    private void handleStorageGUIClick(InventoryClickEvent event, Player player, StorageGUI gui) {
        event.setCancelled(true);

        if (!event.getView().getTopInventory().equals(event.getClickedInventory())) return;

        int slot = event.getSlot();

        if (slot == StorageGUI.SLOT_CLOSE) {
            player.closeInventory();
        } else if (slot == StorageGUI.SLOT_PREV) {
            gui.prevPage();
        } else if (slot == StorageGUI.SLOT_NEXT) {
            gui.nextPage();
        }
    }

    private void handlePickerGUIClick(InventoryClickEvent event, Player player, InventoryPickerGUI gui) {
        event.setCancelled(true);

        if (!event.getView().getTopInventory().equals(event.getClickedInventory())) return;

        int slot = event.getSlot();

        if (slot == InventoryPickerGUI.SLOT_CLOSE) {
            player.closeInventory();
            return;
        }

        if (slot >= 0 && slot <= 35) {
            ItemStack item = event.getCurrentItem();
            if (item == null || item.getType() == Material.AIR || item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

            plugin.getRewardManager().addReward(gui.getTargetUUID(), item);
            player.sendMessage(ChatColor.GREEN + "Item toegevoegd aan de rewards van " + gui.getTargetName() + ".");
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();

        if (plugin.getOpenRewardGUIs().containsKey(uuid) || plugin.getOpenStorageGUIs().containsKey(uuid) || plugin.getOpenPickerGUIs().containsKey(uuid)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        UUID uuid = player.getUniqueId();
        plugin.getOpenRewardGUIs().remove(uuid);
        plugin.getOpenStorageGUIs().remove(uuid);
        plugin.getOpenPickerGUIs().remove(uuid);
    }
}
