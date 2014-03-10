/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse;

import java.io.File;
import java.util.*;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;

import reflectlib.bukkit.Plugin;
import reflectlib.manager.Manager;
import noitemabuse.action.Action;
import noitemabuse.config.*;
import noitemabuse.task.CheckPotionEffectsTask;

public class CheckManager extends Manager {
    public FileLogger log;
    ConfigManager config;

    public CheckManager(Plugin parent) {
        super(parent);
    }

    public void check(Player player, ItemStack item, Event event, EventMessage eventMessage, String... args) {
        if (item == null || item.getTypeId() == 0 || player.hasPermission("noitemabuse.allow")) return;
        if (config.values.remove_invalid_potions) {
            Message reason = checkPotionAndEffects(player, item);
            if (reason != null) {
                performActions(player, item, event, Message.format(player, eventMessage, args));
            }
        }
        if (item.getDurability() < config.values.min_durability) {
            performActions(player, item, event, Message.format(player, eventMessage, args));
        } else {
            try {
                final Map<Enchantment, Integer> enchantments = item.getEnchantments();
                for (Enchantment enchant : enchantments.keySet()) {
                    final int level = enchantments.get(enchant), max = enchant.getMaxLevel();
                    if (level > max) {
                        performActions(player, item, event, Message.format(player, eventMessage, args));
                        return;
                    }
                }
            } catch (NullPointerException ex) {
                // thanks Bukkit. CraftItemStack:278 NPE
            }
        }
    }

    public Message checkActiveEffects(Player player) {
        Message invalidActiveEffect = null;
        for (PotionEffect effect : player.getActivePotionEffects()) {
            Message reason = checkEffect(effect);
            if (reason != null) {
                invalidActiveEffect = reason;
                player.removePotionEffect(effect.getType());
            }
        }
        return invalidActiveEffect;
    }

    public Message checkEffect(PotionEffect effect) {
        final int level = effect.getAmplifier(), duration = effect.getDuration();
        if (level > 2) return Message.REASON_POTION_INVALID_EFFECT_LEVEL;
        if (duration > config.values.max_potion_effect_duration_ticks || duration < 0) return Message.REASON_POTION_INVALID_EFFECT_DURATION;
        return null;
    }

    public Message checkPotionAndEffects(Player player, ItemStack item) {
        Message invalidActiveEffect = checkActiveEffects(player);
        if (invalidActiveEffect != null) return invalidActiveEffect;
        if (item.getType() != Material.POTION) return null;
        Potion pot;
        try {
            pot = Potion.fromItemStack(item);
        } catch (Throwable ex) {
            // Potion constants are private, and Potion has no true error handling.
            // Only String constants are available to depend on.
            // They aren't really part of the API, i.e: changing them would not
            // be considered "[BREAKING]". They are rather volatile in this regard.
            // See Bukkit commit ccc56c8 for an example of string volatility.
            int level = getPotionLevel(item);
            if (level < 0 || level > 1) return Message.REASON_POTION_INVALID_LEVEL;
            return null;
        }
        return checkEffects(pot.getEffects());
    }

    @Override
    public void deinit() {
        log.close();
    }

    public String getItemName(ItemStack item) {
        return item.getType().toString().toLowerCase().replace("_", " ");
    }

    public int getPotionLevel(ItemStack i) {
        return (i.getDurability() & 0x20) >> 5; // damage & TIER_BIT >> TIER_SHIFT;
    }

    @Override
    public void init() {
        config = plugin.getManager(ConfigManager.class);
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

    public void performActions(Player player, ItemStack item, Event event, String message) {
        for (Action action : config.getActions()) {
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

    public void checkEffectsAfterEvent(PlayerEvent event, ItemStack item) {
        new CheckPotionEffectsTask(this, item, event).schedule(plugin);
    }
}
