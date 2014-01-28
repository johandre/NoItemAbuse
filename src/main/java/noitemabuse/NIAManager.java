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
import noitemabuse.task.CheckPotionEffectsTask;

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
        if (config.removeInvalidPotions) {
            Message reason = checkPotionAndEffects(p, i);
            if (reason != null) {
                remove(p, i, e, message);
            }
        }
        if (i.getDurability() < config.minDurability) {
            remove(p, i, e, message);
        } else {
            final Map<Enchantment, Integer> enchantments = i.getEnchantments();
            for (Enchantment enchant : enchantments.keySet()) {
                final int level = enchantments.get(enchant), max = enchant.getMaxLevel();
                if (level > max) {
                    remove(p, i, e, message);
                    return;
                }
            }
        }
    }

    public Message checkActiveEffects(Player p) {
        Message invalidActiveEffect = null;
        for (PotionEffect effect : p.getActivePotionEffects()) {
            Message reason = checkEffect(effect);
            if (reason != null) {
                invalidActiveEffect = reason;
                p.removePotionEffect(effect.getType());
            }
        }
        return invalidActiveEffect;
    }

    public Message checkEffect(PotionEffect effect) {
        final int level = effect.getAmplifier();
        final int duration = effect.getDuration();
        if (level > 2) return Message.REASON_POTION_INVALID_EFFECT_LEVEL;
        if (duration > config.maxEffectDurationTicks || duration < 0) return Message.REASON_POTION_INVALID_EFFECT_DURATION;
        return null;
    }

    public Message checkPotionAndEffects(Player p, ItemStack i) {
        Message invalidActiveEffect = checkActiveEffects(p);
        if (invalidActiveEffect != null) return invalidActiveEffect;
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
        return checkEffects(pot.getEffects());
    }

    @Override
    public void deinit() {
        log.close();
    }

    public int getPotionLevel(ItemStack i) {
        return (i.getDurability() & 0x20) >> 5; // damage & TIER_BIT >> TIER_SHIFT;
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
    public void onInventoryClick(InventoryClickEvent e) {
        // Do the same as onInventoryOpen, if the inventory type is PLAYER (because clients don't tell the server when they open their inventory)
        if (e.getInventory().getType() != InventoryType.PLAYER) return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        ItemStack[] items = e.getInventory().getContents();
        for (ItemStack i : items) {
            check(p, i, e, INVENTORY_CLICK);
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

    @EventHandler(ignoreCancelled = true)
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (!config.removeInvalidPotions || item.getType() != Material.POTION) return;
        new CheckPotionEffectsTask(this, item, event).schedule(plugin);
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

    private Message checkEffects(Collection<PotionEffect> collection) {
        for (PotionEffect effect : collection) {
            Message message = checkEffect(effect);
            if (message != null) return message;
        }
        return null;
    }

    private String locationToString(Location loc) {
        return "[" + loc.getWorld().getName() + "] " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    public String getItemName(ItemStack item) {
        return item.getType().toString().toLowerCase().replace("_", " ");
    }
}
