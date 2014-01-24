/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse;

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

import eu.icecraft_mc.frozenlib_R1.Plugin;
import eu.icecraft_mc.frozenlib_R1.manager.Manager;

public class NIAManager extends Manager implements Listener {
    private FileLogger log;

    public NIAManager(Plugin parent) {
        super(parent);
    }

    public void check(HumanEntity p, ItemStack i, Event event, String info) {
        if (i == null || p.hasPermission("noitemabuse.allow")) return;
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
        check(p, i, e, "from player " + p.getName() + " on block break");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            String info = "worn by player " + p.getName() + " on received attack";
            for (ItemStack i : p.getInventory().getArmorContents()) {
                check(p, i, e, info);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            ItemStack i = p.getItemInHand();
            check(p, i, e, "wielded by player " + p.getName() + " on attack");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        // Do the same as onInventoryOpen, if the inventory type is PLAYER (because clients don't tell the server when they open their inventory)
        if (event.getInventory().getType() != InventoryType.PLAYER) return;
        HumanEntity p = event.getWhoClicked();
        ItemStack[] items = event.getInventory().getContents();
        for (ItemStack i : items) {
            check(p, i, event, null);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        HumanEntity p = event.getPlayer();
        // if (((CraftInventory) event.getInventory()).getInventory().getContents() == null) return; // bukkit used to throw a NPE on inventory.getContents() if it was a beacon
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
            String info = getInfoFromLocation(loc);
            String type = event.getInventory().getType().name().toLowerCase().replace("_", " ");
            info = "from inventory type (" + type + ") at {" + info + "} triggered by " + p.getName();
            check(p, i, event, info);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack i = event.getItemDrop().getItemStack();
        Player p = event.getPlayer();
        check(p, i, event, "dropped by " + p.getName());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionThrow(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack i = p.getItemInHand();
        if (i.getType() == Material.POTION) {
            check(p, i, e, "from player " + p.getName() + " on potion throw");
        }
    }

    public void remove(HumanEntity p, ItemStack item, int durability, Enchantment enchant, int level, Event event, String info) {
        removeItem(p, item);
        if (event instanceof Cancellable) {
            ((Cancellable) event).setCancelled(true);
        }
        //
        final String name = p.getName();
        final String itemName = item.getType().toString().toLowerCase().replaceAll("_", " ");
        if (info == null) {
            info = "from player " + name + "'s inventory";
        }
        String message = "Removed " + itemName + " " + info;
        message += " [durability " + durability;
        if (enchant != null) {
            message += "; " + enchant.getName() + " enchantment level " + level + " > " + enchant.getMaxLevel();
        }
        message += "]";
        log(p, message);
        return;
    }

    private String getInfoFromLocation(Location loc) {
        return "[" + loc.getWorld().getName() + "] " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private void log(HumanEntity p, String message) {
        if (p.hasPermission("noitemabuse.log")) {
            log.log(message);
        }
        message = RED + "[" + DARK_RED + "NoItemAbuse" + RED + "] " + ChatColor.GOLD + message;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("noitemabuse.notify")) {
                player.sendMessage(message);
            }
        }
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
