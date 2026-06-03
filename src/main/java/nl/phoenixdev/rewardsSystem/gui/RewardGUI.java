package nl.phoenixdev.rewardsSystem.gui;

import nl.phoenixdev.rewardsSystem.RewardsSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class RewardGUI {

    public static final int SLOT_PREV = 45;
    public static final int SLOT_CLOSE = 49;
    public static final int SLOT_NEXT = 53;
    private static final int PAGE_SIZE = 45;

    private final RewardsSystem plugin;
    private final UUID targetUUID;
    private final String targetName;
    private int page = 0;
    private Inventory inventory;

    public RewardGUI(RewardsSystem plugin, UUID targetUUID, String targetName) {
        this.plugin = plugin;
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        build();
    }

    private void build() {
        inventory = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Rewards: " + ChatColor.WHITE + targetName);
        refresh();
    }

    public void refresh() {
        inventory.clear();

        List<ItemStack> rewards = plugin.getRewardManager().getRewards(targetUUID);
        int start = page * PAGE_SIZE;

        for (int i = 0; i < PAGE_SIZE && start + i < rewards.size(); i++) {
            inventory.setItem(i, rewards.get(start + i));
        }

        ItemStack glass = createGlassPane();
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, glass);
        }

        if (page > 0) {
            inventory.setItem(SLOT_PREV, createItem(Material.ARROW, ChatColor.YELLOW + "Vorige pagina"));
        }

        inventory.setItem(SLOT_CLOSE, createItem(Material.BARRIER, ChatColor.RED + "Sluiten"));

        if (start + PAGE_SIZE < rewards.size()) {
            inventory.setItem(SLOT_NEXT, createItem(Material.ARROW, ChatColor.YELLOW + "Volgende pagina"));
        }
    }

    public void nextPage() {
        List<ItemStack> rewards = plugin.getRewardManager().getRewards(targetUUID);
        if ((page + 1) * PAGE_SIZE < rewards.size()) {
            page++;
            refresh();
        }
    }

    public void prevPage() {
        if (page > 0) {
            page--;
            refresh();
        }
    }

    public void open(Player viewer) {
        viewer.openInventory(inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    public String getTargetName() {
        return targetName;
    }

    public int getPage() {
        return page;
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createGlassPane() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }
}
