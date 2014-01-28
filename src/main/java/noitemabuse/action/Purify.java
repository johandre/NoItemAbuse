/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import noitemabuse.config.Message;

import eu.icecraft_mc.frozenlib_R1.Plugin;

public class Purify extends Action {
    private static final short NORMAL_DURABILITY = 0;

    public Purify(Plugin plugin) {
        super(plugin);
    }

    @Override
    public Message getMessage() {
        return Message.PURIFY;
    }

    @Override
    public String getName() {
        return "purify";
    }

    @Override
    public void perform(Player player, ItemStack item, Event event, String message) {
        if (item.getDurability() < 0) {
            item.setDurability(NORMAL_DURABILITY);
        }
        if (config.purifyAll) {
            item.getEnchantments().clear();
        } else {
            final Map<Enchantment, Integer> enchantments = item.getEnchantments();
            for (Enchantment enchant : enchantments.keySet()) {
                final int level = enchantments.get(enchant), max = enchant.getMaxLevel();
                if (level > max) {
                    enchantments.remove(enchant);
                    return;
                }
            }
        }
    }
}
