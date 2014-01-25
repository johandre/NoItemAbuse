/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse;

import static noitemabuse.config.EventMessage.*;
import static org.bukkit.ChatColor.*;

import java.io.File;
import java.util.*;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;

import noitemabuse.config.*;

import eu.icecraft_mc.frozenlib_R1.Plugin;
import eu.icecraft_mc.frozenlib_R1.manager.Manager;

public class NIAManager extends Manager implements Listener {
    private FileLogger log;
    private ConfigManager config;

    public NIAManager(Plugin parent) {
        super(parent);
    }

    @Override
    public boolean loadAfter(Manager manager) {
        return manager instanceof ConfigManager;
    }

    public void check(Player p, ItemStack i, Event event, EventMessage message, String... args) {
        if (i == null || p.hasPermission("noitemabuse.allow")) return;
        final String info = Message.format(p, message, args);
        final int durability = i.getDurability();
        final Map<Enchantment, Integer> enchantments = i.getEnchantments();
        if (i.getType() == Material.POTION) {
            Potion pot;
            try {
                pot = Potion.fromItemStack(i);
            } catch (Throwable ex) {
                int level = (i.getDurability() & 0x20) >> 5;
                if (level < 0 || level > 2) {
                    remove(p, i, durability, null, -1, event, info + " (invalid potion level " + level + ")");
                }
                return;
            }
            final Iterator<PotionEffect> effects = pot.getEffects().iterator();
            while (effects.hasNext()) {
                final PotionEffect effect = effects.next();
                final int level = effect.getAmplifier();
                final int duration = effect.getDuration();
                if (level > 2) {
                    remove(p, i, durability, null, -1, event, info + " (potion effect " + effect.getType().getName() + " duration " + duration + ", level " + level + " > 2)");
                    return;
                }
                if (duration > 9600) {
                    remove(p, i, durability, null, -1, event, info + " (potion effect " + effect.getType().getName() + " duration " + duration + " > 9600, level " + level + ")");
                    return;
                }
            }
        }
        if (durability < -1) {
            remove(p, i, durability, null, -1, event, info);
        } else {
            for (Enchantment enchant : enchantments.keySet()) {
                final int level = enchantments.get(enchant);
                final int max = enchant.getMaxLevel();
                if (level > max) {
                    remove(p, i, durability, enchant, level, event, info);
                    return;
                }
            }
        }
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

    public void remove(Player p, ItemStack item, int durability, Enchantment enchant, int level, Event event, String info) {
        removeItem(p, item);
        if (event instanceof Cancellable) {
            ((Cancellable) event).setCancelled(true);
        }
        //
        final String itemName = item.getType().toString().toLowerCase().replace("_", " ");
        final String enchantName = enchant.getName();
        final int max = enchant.getMaxLevel();
        List<String> reasons = new ArrayList<String>(3);
        if(durability != 0) reasons.add(Message.format(p, Message.REASON_OVERDURABLE, "$durability:" + durability));
        if(level > max) reasons.add(Message.format(p, Message.REASON_OVERENCHANT, "$enchant:" + enchantName, "$level:" + level, "$max:" + max));
        String reason = "undefined"; // TODO reason enum
        String message = config.getActionMessage(p, "$item:" + itemName, "$reason:" + reason);
        log(p, message);
        return;
    }

    private String locationToString(Location loc) {
        return "[" + loc.getWorld().getName() + "] " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private void log(HumanEntity p, String message) {
        if (p.hasPermission("noitemabuse.log")) {
            log.log(message);
        }
        message = RED + "[" + DARK_RED + "NoItemAbuse" + RED + "] " + ChatColor.GOLD + message;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("noitemabuse.notify") && shouldNotify(player)) {
                player.sendMessage(message);
            }
        }
    }

    private boolean shouldNotify(Player player) {
        return config.toggled.contains(player.getName().toLowerCase()) != config.defaultNotify;
    }

    private void removeItem(HumanEntity p, ItemStack item) {
        p.getInventory().remove(item);
        ItemStack[] armor = p.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] == item) {
                armor[i] = null;
            }
        }
        p.getInventory().setArmorContents(armor);
    }
}
