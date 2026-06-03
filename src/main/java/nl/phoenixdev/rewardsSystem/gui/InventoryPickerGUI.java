package nl.phoenixdev.rewardsSystem.gui;

import nl.phoenixdev.rewardsSystem.RewardsSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class InventoryPickerGUI {

    public static final int SLOT_CLOSE = 49;

    private final RewardsSystem plugin;
    private final UUID targetUUID;
    private final String targetName;
    private final Inventory inventory;

    public InventoryPickerGUI(RewardsSystem plugin, Player staff, UUID targetUUID, String targetName) {
        this.plugin = plugin;
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Kies item voor: " + ChatColor.WHITE + targetName);
        populate(staff);
    }

    private void populate(Player staff) {
        ItemStack glass = createGlassPane();

        for (int guiSlot = 0; guiSlot < 36; guiSlot++) {
            int invSlot = guiSlotToInvSlot(guiSlot);
            ItemStack item = staff.getInventory().getItem(invSlot);
            if (item != null && item.getType() != Material.AIR) {
                inventory.setItem(guiSlot, item.clone());
            } else {
                inventory.setItem(guiSlot, glass);
            }
        }

        for (int i = 36; i < 54; i++) {
            inventory.setItem(i, glass);
        }

        inventory.setItem(SLOT_CLOSE, createItem(Material.BARRIER, ChatColor.RED + "Sluiten"));
    }

    public int guiSlotToInvSlot(int guiSlot) {
        if (guiSlot >= 0 && guiSlot <= 26) return guiSlot + 9;
        if (guiSlot >= 27 && guiSlot <= 35) return guiSlot - 27;
        return -1;
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
