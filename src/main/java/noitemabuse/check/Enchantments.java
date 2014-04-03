/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.check;

import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.config.Message;

public class Enchantments extends Check {
    public Enchantments(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean check(Player player, ItemStack item) {
        return getInvalidEnchantment(player, item) != null;
    }

    public Entry<Enchantment, Integer> getInvalidEnchantment(Player player, ItemStack item) {
        for (Entry<Enchantment, Integer> ent : item.getEnchantments().entrySet()) {
            if (ent.getValue() > ent.getKey().getMaxLevel() && !hasEnchantPermissions(player, item, ent)) return ent;
        }
        return null;
    }

    @Override
    public String getLogMessage(Player player, ItemStack item) {
        Entry<Enchantment, Integer> invalid = getInvalidEnchantment(player, item);
        Enchantment enchant = invalid.getKey();
        return Message.format(player, Message.REASON_OVERENCHANT, "$enchant:" + enchant.getName(), "$level:" + invalid.getValue(), "$max:" + enchant.getMaxLevel());
    }

    public boolean hasEnchantPermissions(Player player, ItemStack item, Entry<Enchantment, Integer> ent) {
        String itemName = item.getType().toString();
        String name = ent.getKey().getName();
        return player.hasPermission(getPermission() + "." + itemName + "." + name) || player.hasPermission(getPermission() + "." + name);
    }
}
