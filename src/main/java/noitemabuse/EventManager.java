/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse;

import static noitemabuse.config.EventMessage.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import reflectlib.manager.Manager;

public class EventManager extends Manager implements Listener {
    private CheckManager manager;

    public EventManager(Plugin parent) {
        super(parent);
    }

    @Override
    public void init() {
        manager = plugin.getManager(CheckManager.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        ItemStack i = p.getItemInHand();
        manager.check(p, i, e, BLOCK_BREAK);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            for (ItemStack i : p.getInventory().getArmorContents()) {
                manager.check(p, i, e, RECEIVED_ATTACK);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            ItemStack i = p.getItemInHand();
            manager.check(p, i, e, ATTACK);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent e) {
        // Do the same as onInventoryOpen, if the inventory type is PLAYER (because clients don't tell the server when they open their inventory)
        if (e.getInventory().getType() != InventoryType.PLAYER) return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        ItemStack[] items = e.getInventory().getContents();
        for (ItemStack i : items) {
            manager.check(p, i, e, INVENTORY_CLICK);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player p = (Player) event.getPlayer();
        ItemStack[] items = event.getInventory().getContents();
        Location loc = p.getLocation();
        for (ItemStack i : items) {
            try {
                if (event.getInventory().getHolder() instanceof BlockState) {
                    loc = ((BlockState) event.getInventory().getHolder()).getBlock().getLocation();
                } else if (event.getInventory().getHolder() instanceof DoubleChest) {
                    loc = ((DoubleChest) event.getInventory().getHolder()).getLocation();
                }
            } catch (Exception e) {}
            String type = event.getInventory().getType().name().toLowerCase().replace("_", " ");
            manager.check(p, i, event, CONTAINER_OPEN, "$container:" + type, "$location:" + locationToString(loc));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack i = event.getItemDrop().getItemStack();
        Player p = event.getPlayer();
        manager.check(p, i, event, ITEM_DROP, "$location:" + locationToString(p.getLocation()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (!manager.config.values.remove_invalid_potions || item.getType() != Material.POTION) return;
        manager.checkEffectsAfterEvent(event, item);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionThrow(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack i = p.getItemInHand();
        if (i.getType() == Material.POTION) {
            manager.check(p, i, e, POTION_THROW);
        }
    }

    private String locationToString(Location loc) {
        return "[" + loc.getWorld().getName() + "] " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }
}
