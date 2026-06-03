package nl.phoenixdev.rewardsSystem.command;

import nl.phoenixdev.rewardsSystem.RewardsSystem;
import nl.phoenixdev.rewardsSystem.gui.InventoryPickerGUI;
import nl.phoenixdev.rewardsSystem.gui.RewardGUI;
import nl.phoenixdev.rewardsSystem.gui.StorageGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class RewardsCommand implements CommandExecutor {

    private final RewardsSystem plugin;

    public RewardsCommand(RewardsSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Dit commando kan alleen door spelers worden uitgevoerd.");
            return true;
        }

        if (!player.hasPermission("rewardssystem.admin")) {
            player.sendMessage(ChatColor.RED + "Je hebt geen toestemming voor dit commando.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> handleAdd(player, args);
            case "remove" -> handleRemove(player, args);
            case "view" -> handleView(player, args);
            case "storage" -> handleStorage(player, args);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleAdd(Player sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Gebruik: /rewards add <speler> <hand|inventory|storage> [index]");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Speler niet gevonden: " + args[1]);
            return;
        }

        switch (args[2].toLowerCase()) {
            case "hand" -> {
                ItemStack item = sender.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR) {
                    sender.sendMessage(ChatColor.RED + "Je houdt niets vast.");
                    return;
                }
                plugin.getRewardManager().addReward(target.getUniqueId(), item);
                sender.sendMessage(ChatColor.GREEN + item.getType().name() + " toegevoegd aan de rewards van " + target.getName() + ".");
            }
            case "inventory" -> {
                InventoryPickerGUI gui = new InventoryPickerGUI(plugin, sender, target.getUniqueId(), target.getName());
                plugin.getOpenPickerGUIs().put(sender.getUniqueId(), gui);
                gui.open(sender);
            }
            case "storage" -> {
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Gebruik: /rewards add <speler> storage <index>");
                    return;
                }
                try {
                    int index = Integer.parseInt(args[3]) - 1;
                    List<ItemStack> storageItems = plugin.getStorageManager().getItems();
                    if (index < 0 || index >= storageItems.size()) {
                        sender.sendMessage(ChatColor.RED + "Ongeldig index. Opslag heeft " + storageItems.size() + " item(s).");
                        return;
                    }
                    ItemStack item = storageItems.get(index);
                    plugin.getRewardManager().addReward(target.getUniqueId(), item);
                    sender.sendMessage(ChatColor.GREEN + item.getType().name() + " (opslag #" + args[3] + ") toegevoegd aan de rewards van " + target.getName() + ".");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Index moet een getal zijn.");
                }
            }
            default -> sender.sendMessage(ChatColor.RED + "Gebruik: /rewards add <speler> <hand|inventory|storage> [index]");
        }
    }

    private void handleRemove(Player sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Gebruik: /rewards remove <speler> <index>");
            return;
        }

        UUID targetUUID;
        String targetName;

        Player online = Bukkit.getPlayer(args[1]);
        if (online != null) {
            targetUUID = online.getUniqueId();
            targetName = online.getName();
        } else {
            @SuppressWarnings("deprecation")
            OfflinePlayer offline = Bukkit.getOfflinePlayer(args[1]);
            if (!offline.hasPlayedBefore()) {
                sender.sendMessage(ChatColor.RED + "Speler niet gevonden: " + args[1]);
                return;
            }
            targetUUID = offline.getUniqueId();
            targetName = offline.getName() != null ? offline.getName() : args[1];
        }

        try {
            int index = Integer.parseInt(args[2]) - 1;
            if (plugin.getRewardManager().removeReward(targetUUID, index)) {
                sender.sendMessage(ChatColor.GREEN + "Reward #" + args[2] + " verwijderd van " + targetName + ".");
            } else {
                int total = plugin.getRewardManager().getRewards(targetUUID).size();
                sender.sendMessage(ChatColor.RED + "Ongeldig index. " + targetName + " heeft " + total + " reward(s).");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Index moet een getal zijn.");
        }
    }

    private void handleView(Player sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Gebruik: /rewards view <speler>");
            return;
        }

        UUID targetUUID;
        String targetName;

        Player online = Bukkit.getPlayer(args[1]);
        if (online != null) {
            targetUUID = online.getUniqueId();
            targetName = online.getName();
        } else {
            @SuppressWarnings("deprecation")
            OfflinePlayer offline = Bukkit.getOfflinePlayer(args[1]);
            if (!offline.hasPlayedBefore()) {
                sender.sendMessage(ChatColor.RED + "Speler niet gevonden: " + args[1]);
                return;
            }
            targetUUID = offline.getUniqueId();
            targetName = offline.getName() != null ? offline.getName() : args[1];
        }

        RewardGUI gui = new RewardGUI(plugin, targetUUID, targetName);
        plugin.getOpenRewardGUIs().put(sender.getUniqueId(), gui);
        gui.open(sender);
    }

    private void handleStorage(Player sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Gebruik: /rewards storage <add|remove|view> [index]");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "add" -> {
                ItemStack item = sender.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR) {
                    sender.sendMessage(ChatColor.RED + "Je houdt niets vast.");
                    return;
                }
                plugin.getStorageManager().addItem(item);
                int newIndex = plugin.getStorageManager().getItems().size();
                sender.sendMessage(ChatColor.GREEN + item.getType().name() + " toegevoegd aan de opslag. (Index: " + newIndex + ")");
            }
            case "remove" -> {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Gebruik: /rewards storage remove <index>");
                    return;
                }
                try {
                    int index = Integer.parseInt(args[2]) - 1;
                    if (plugin.getStorageManager().removeItem(index)) {
                        sender.sendMessage(ChatColor.GREEN + "Item #" + args[2] + " verwijderd uit de opslag.");
                    } else {
                        int total = plugin.getStorageManager().getItems().size();
                        sender.sendMessage(ChatColor.RED + "Ongeldig index. Opslag heeft " + total + " item(s).");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Index moet een getal zijn.");
                }
            }
            case "view" -> {
                StorageGUI gui = new StorageGUI(plugin);
                plugin.getOpenStorageGUIs().put(sender.getUniqueId(), gui);
                gui.open(sender);
            }
            default -> sender.sendMessage(ChatColor.RED + "Gebruik: /rewards storage <add|remove|view> [index]");
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Rewards Systeem ===");
        player.sendMessage(ChatColor.YELLOW + "/rewards add <speler> hand" + ChatColor.WHITE + " - Voeg item in hand toe");
        player.sendMessage(ChatColor.YELLOW + "/rewards add <speler> inventory" + ChatColor.WHITE + " - Kies item uit inventaris");
        player.sendMessage(ChatColor.YELLOW + "/rewards add <speler> storage <index>" + ChatColor.WHITE + " - Voeg opslag item toe");
        player.sendMessage(ChatColor.YELLOW + "/rewards remove <speler> <index>" + ChatColor.WHITE + " - Verwijder een reward");
        player.sendMessage(ChatColor.YELLOW + "/rewards view <speler>" + ChatColor.WHITE + " - Bekijk rewards van speler");
        player.sendMessage(ChatColor.YELLOW + "/rewards storage add" + ChatColor.WHITE + " - Voeg item toe aan opslag");
        player.sendMessage(ChatColor.YELLOW + "/rewards storage remove <index>" + ChatColor.WHITE + " - Verwijder item uit opslag");
        player.sendMessage(ChatColor.YELLOW + "/rewards storage view" + ChatColor.WHITE + " - Bekijk de opslag");
    }
}
