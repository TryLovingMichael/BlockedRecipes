package dev.michaelh.blockedrecipes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class BlockedRecipes extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private HashMap<UUID, PermissionAttachment> attachments = new HashMap<>();

    @Override
    public void onEnable() {
        // Load config on plugin enable
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        config = getConfig();

        // Register listener
        getServer().getPluginManager().registerEvents(this, this);


        getLogger().info("BlockedRecipes has been enabled!");
        getLogger().info("Blocked Recipes: " + config.get("blockedRecipes"));
        getLogger().info("Created by MichaelH");
    }

    @Override
    public void onDisable() {
        getLogger().info("BlockedRecipes has been disabled!");
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        Material itemType = result.getType();
        String itemName = itemType.toString();

        // Check if crafting recipe is blocked in config
        if (config.getBoolean("blockedRecipes." + itemName)) {
            // Check if player has permission to craft blocked recipe
            if (!event.getWhoClicked().hasPermission("core.recipe." + itemName)) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage(ChatColor.RED + "You do not have permission to craft this item.");
            }
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the player is right-clicking on a piece of paper
        if (event.getAction().name().contains("RIGHT_CLICK") && item.getType() == Material.PAPER) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) {
                String recipeName = meta.getDisplayName().toLowerCase().replace(" ", "_");
                String permissionNode = "core.recipe." + recipeName;

                // Check if the player already has the permission node
                if (player.hasPermission(permissionNode)) {
                    player.sendMessage("You already have permission to craft " + recipeName);
                } else {
                    // Add the permission node to the player's permission attachments
                    PermissionAttachment attachment = getAttachment(player);
                    attachment.setPermission(permissionNode, true);
                    player.sendMessage("You can now craft " + recipeName);
                }
            }
        }
    }

    private PermissionAttachment getAttachment(Player player) {
        // Get the player's permission attachment from the HashMap, or create a new one if it doesn't exist
        UUID uuid = player.getUniqueId();
        if (attachments.containsKey(uuid)) {
            return attachments.get(uuid);
        } else {
            PermissionAttachment attachment = player.addAttachment(this);
            attachments.put(uuid, attachment);
            return attachment;
        }
    }
}


