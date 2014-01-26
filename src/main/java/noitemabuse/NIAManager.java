/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse;

import static noitemabuse.config.EventMessage.*;

import java.io.File;
import java.util.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;

import noitemabuse.action.Action;
import noitemabuse.config.*;

import eu.icecraft_mc.frozenlib_R1.Plugin;
import eu.icecraft_mc.frozenlib_R1.manager.Manager;

public class NIAManager extends Manager implements Listener {
    public FileLogger log;
    private ConfigManager config;

    public NIAManager(Plugin parent) {
        super(parent);
    }

    public void check(Player p, ItemStack i, Event e, EventMessage eventMessage, String... args) {
        if (i == null || p.hasPermission("noitemabuse.allow")) return;
        String message = Message.format(p, eventMessage, args);
        final int durability = i.getDurability();
        final Map<Enchantment, Integer> enchantments = i.getEnchantments();
        if (config.removeInvalidPotions) {
            Message reason = checkPotion(p, i);
            if (reason != null) {
                remove(p, i, e, message);
            }
        }
        if (durability < -1) {
            remove(p, i, e, message);
        } else {
            for (Enchantment enchant : enchantments.keySet()) {
                final int level = enchantments.get(enchant);
                final int max = enchant.getMaxLevel();
                if (level > max) {
                    remove(p, i, e, message);
                    return;
                }
            }
        }
    }

    public Message checkPotion(Player p, ItemStack i) {
        if (i.getType() != Material.POTION) return null;
        Potion pot;
        try {
            pot = Potion.fromItemStack(i);
        } catch (Throwable ex) {
            // Potion constants are private, and Potion has no true error handling.
            // Only String constants are available to depend on.
            // They aren't really part of the API, i.e: changing them would not
            // be considered "[BREAKING]". They are rather volatile in this regard.
            // See Bukkit commit ccc56c8 for an example of string volatility.
            int level = getPotionLevel(i);
            if (level < 0 || level > 1) return Message.REASON_POTION_INVALID_LEVEL;
            return null;
        }
        final Iterator<PotionEffect> effects = pot.getEffects().iterator();
        while (effects.hasNext()) {
            final PotionEffect effect = effects.next();
            final int level = effect.getAmplifier();
            final int duration = effect.getDuration();
            if (level > 2) return Message.REASON_POTION_INVALID_EFFECT_LEVEL;
            if (duration > 9600) return Message.REASON_POTION_INVALID_EFFECT_DURATION;
        }
        return null;
    }

    @Override
    public void deinit() {
        log.close();
    }

    @Override
    public void init() {
        config = plugin.getManager(ConfigManager.class);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        try {
            log = new FileLogger(new File(plugin.getDataFolder(), "NoItemAbuse.log"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean loadAfter(Manager manager) {
        return manager instanceof ConfigManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        ItemStack i = p.getItemInHand();
        check(p, i, e, BLOCK_BREAK);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            for (ItemStack i : p.getInventory().getArmorContents()) {
                check(p, i, e, RECEIVED_ATTACK);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            ItemStack i = p.getItemInHand();
            check(p, i, e, ATTACK);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        // Do the same as onInventoryOpen, if the inventory type is PLAYER (because clients don't tell the server when they open their inventory)
        if (event.getInventory().getType() != InventoryType.PLAYER) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player p = (Player) event.getWhoClicked();
        ItemStack[] items = event.getInventory().getContents();
        for (ItemStack i : items) {
            check(p, i, event, INVENTORY_CLICK);
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
            check(p, i, event, CONTAINER_OPEN, "$container:" + type, "$location:" + locationToString(loc));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack i = event.getItemDrop().getItemStack();
        Player p = event.getPlayer();
        check(p, i, event, ITEM_DROP, "$location:" + locationToString(p.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionThrow(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack i = p.getItemInHand();
        if (i.getType() == Material.POTION) {
            check(p, i, e, POTION_THROW);
        }
    }

    public void remove(Player player, ItemStack item, Event event, String message) {
        for (Action action : config.actions) {
            action.perform(player, item, event, message);
        }
    }

    public int getPotionLevel(ItemStack i) {
        return (i.getDurability() & 0x20) >> 5; // damage & TIER_BIT >> TIER_SHIFT;
    }

    private String locationToString(Location loc) {
        return "[" + loc.getWorld().getName() + "] " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }
}
