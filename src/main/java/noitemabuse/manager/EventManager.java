/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.manager;

import static noitemabuse.config.EventMessage.*;

import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

import reflectlib.bukkit.Plugin;
import reflectlib.manager.Manager;

public class EventManager extends Manager implements Listener {
    private CheckManager manager;

    public EventManager(Plugin parent) {
        super(parent);
    }

    public void checkInventory(Player player, Inventory inventory, Event event) {
        ItemStack[] items = inventory.getContents();
        for (ItemStack i : items) {
            manager.check(player, i, event, INVENTORY_CLICK);
        }
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
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        ItemStack i = p.getItemInHand();
        manager.check(p, i, e, BLOCK_PLACE);
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
        //if (e.getInventory().getType() != InventoryType.PLAYER) return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        checkInventory((Player) e.getWhoClicked(), e.getInventory(), e);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryCreative(InventoryCreativeEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        checkInventory((Player) e.getWhoClicked(), e.getInventory(), e);
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
            } catch (Exception e) {} // I think a Bukkit bug made me do this
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemSwitch(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        checkInventory(player, player.getInventory(), event);
    }

    private String locationToString(Location loc) {
        return "[" + loc.getWorld().getName() + "] " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }
}
