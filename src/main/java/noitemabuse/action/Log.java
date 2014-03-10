/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import java.util.*;
import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.*;

import reflectlib.bukkit.Plugin;
import noitemabuse.config.Message;

public class Log extends Action {
    public Log(Plugin plugin) {
        super(plugin);
    }

    @Override
    public Message getMessage() {
        return Message.LOG;
    }

    public String getMessage(Player player, ItemStack item, Event event, String eventMessage) {
        final String itemName;
        if (item == null) {
            itemName = Message.ITEM_POTION_EFFECT.getMessage();
        } else {
            itemName = manager.getItemName(item);
        }
        Enchantment enchant = null;
        int level = 0, max = 0;
        for (Entry<Enchantment, Integer> ent : item.getEnchantments().entrySet()) {
            Enchantment key = ent.getKey();
            level = ent.getValue();
            max = key.getMaxLevel();
            if (level > max) {
                enchant = key;
                break;
            }
        }
        int durability = item.getDurability();
        List<String> reasons = new ArrayList<String>(5);
        if (durability < config.values.min_durability) {
            reasons.add(Message.format(player, Message.REASON_OVERDURABLE, "$durability:" + durability));
        }
        if (enchant != null) {
            reasons.add(Message.format(player, Message.REASON_OVERENCHANT, "$enchant:" + enchant.getName(), "$level:" + level, "$max:" + max));
        }
        if (config.values.remove_invalid_potions) {
            Message reason = manager.checkPotionAndEffects(player, item);
            if (reason != null) {
                reasons.add(Message.format(player, reason, "$type:" + getInvalidType(item), "$level:" + manager.getPotionLevel(item), "$effectlevel:" + getInvalidEffectLevel(item), "$duration:"
                        + getInvalidDuration(item)));
            }
        }
        StringBuilder reasonbuilder = new StringBuilder();
        for (String r : reasons) {
            reasonbuilder.append(r).append(", ");
        }
        String reason = reasonbuilder.toString();
        if (!reasons.isEmpty()) {
            reason = reason.substring(0, reason.length() - 2);
        }
        String message = config.getActionMessage(player, "$item:" + itemName, "$reason:" + reason, "$event:" + eventMessage);
        return message;
    }

    @Override
    public String getName() {
        return "log";
    }

    @Override
    public void perform(Player player, ItemStack item, Event event, String message) {
        log(player, getMessage(player, item, event, message));
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
                if (manager.checkEffect(effect) != null) return effect;
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

    private void log(Player p, String message) {
        if (config.values.log_all_players || p.hasPermission("noitemabuse.log")) {
            manager.log.log(message);
        }
    }
}
