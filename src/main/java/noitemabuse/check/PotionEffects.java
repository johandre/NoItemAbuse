/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.check;

import static noitemabuse.config.EventMessage.POTION_THROW;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;

import reflectlib.bukkit.Plugin;
import noitemabuse.config.*;
import noitemabuse.task.CheckPotionEffectsTask;
import noitemabuse.util.Executor;

public class PotionEffects extends Check implements Listener {
    private PotionEffectsOptions options = new PotionEffectsOptions(this);

    public PotionEffects(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean check(Player player, ItemStack item) {
        return options.remove_invalid_potions && checkPotionAndEffects(player, item) != null;
    }

    public void checkActiveEffects(Player player) {
        // Message invalidActiveEffect = null;
        for (PotionEffect effect : player.getActivePotionEffects()) {
            Message reason = checkEffect(effect);
            if (reason != null) {
                // invalidActiveEffect = reason;
                player.removePotionEffect(effect.getType());
            }
        }
        // return invalidActiveEffect;
    }

    public Message checkEffect(PotionEffect effect) {
        final int level = effect.getAmplifier(), duration = effect.getDuration();
        if (level > options.max_potion_effect_level) return Message.REASON_POTION_INVALID_EFFECT_LEVEL;
        if (duration > options.max_potion_effect_duration_ticks || duration < 0 && !options.allow_infinite_duration) return Message.REASON_POTION_INVALID_EFFECT_DURATION;
        return null;
    }

    public Message checkPotionAndEffects(Player player, ItemStack item) {
        // Message invalidActiveEffect =
        checkActiveEffects(player);
        // if (invalidActiveEffect != null) return invalidActiveEffect;
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
    public String getLogMessage(Player player, ItemStack item) {
        Message reason = checkPotionAndEffects(player, item);
        String type = getInvalidType(item);
        int level = getPotionLevel(item);
        int effectlevel = getInvalidEffectLevel(item);
        int duration = getInvalidDuration(item);
        return Message.format(player, reason, "$type:" + type, "$level:" + level, "$effectlevel:" + effectlevel, "$duration:" + duration);
    }

    @Override
    public Options getOptions() {
        return options;
    }

    public int getPotionLevel(ItemStack i) {
        return (i.getDurability() & 0x20) >> 5; // damage & TIER_BIT >> TIER_SHIFT;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (!options.remove_invalid_potions || item.getType() != Material.POTION) return;
        new CheckPotionEffectsTask(manager, item, event).schedule(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent e) {
        for (Entity entity : e.getAffectedEntities()) {
            if (entity instanceof Player) {
                new CheckPotionEffectsTask(manager, null, (Player) entity, e).schedule(plugin);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionThrow(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack i = p.getItemInHand();
        if (i.getType() == Material.POTION) {
            manager.check(p, i, e, POTION_THROW);
        }
    }

    private Message checkEffects(Collection<PotionEffect> collection) {
        for (PotionEffect effect : collection) {
            Message message = checkEffect(effect);
            if (message != null) return message;
        }
        return null;
    }

    private int getInvalidDuration(ItemStack potion) {
        PotionEffect effect = getInvalidEffect(potion);
        if (effect != null) return effect.getDuration();
        return -1;
    }

    private PotionEffect getInvalidEffect(ItemStack potion) {
        try {
            Potion pot = Potion.fromItemStack(potion);
            for (PotionEffect effect : pot.getEffects()) {
                if (checkEffect(effect) != null) return effect;
            }
        } catch (Throwable ex) {}
        return null;
    }

    private int getInvalidEffectLevel(ItemStack potion) {
        PotionEffect effect = getInvalidEffect(potion);
        if (effect != null) return effect.getAmplifier();
        return -1;
    }

    private String getInvalidType(ItemStack potion) {
        PotionEffect effect = getInvalidEffect(potion);
        if (effect != null) return effect.getType().toString();
        return "undefined";
    }

    public class PotionEffectsOptions extends Options {
        public boolean remove_invalid_potions = true;
        public boolean allow_infinite_duration = false;
        public int max_potion_effect_level = 2;
        public int max_potion_effect_duration_ticks = 9600;

        public PotionEffectsOptions(Executor executor) {
            super(executor);
        }
    }
}
