/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.check;

import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.Executor;
import noitemabuse.config.*;

public class Enchantments extends Check {
    EnchantmentsOptions options = new EnchantmentsOptions(this);

    public Enchantments(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean check(Player player, ItemStack item) {
        return getInvalidEnchantment(player, item) != null || getEnchantmentCount(item) > options.max_enchantments;
    }

    private int getEnchantmentCount(ItemStack item) {
        return item.getEnchantments().size();
    }

    public Entry<Enchantment, Integer> getInvalidEnchantment(Player player, ItemStack item) {
        for (Entry<Enchantment, Integer> ent : item.getEnchantments().entrySet()) {
            if (ent.getValue() > ent.getKey().getMaxLevel() && !hasEnchantPermissions(player, item, ent)) return ent;
        }
        return null;
    }

    @Override
    public Options getOptions() {
        return options;
    }

    @Override
    public String getLogMessage(Player player, ItemStack item) {
        Entry<Enchantment, Integer> invalid = getInvalidEnchantment(player, item);
        if (invalid == null) return Message.format(player, Message.REASON_OVERENCHANT_COUNT, "$enchantments:" + getEnchantmentCount(item), "$max:" + options.max_enchantments);
        Enchantment enchant = invalid.getKey();
        return Message.format(player, Message.REASON_OVERENCHANT, "$enchant:" + enchant.getName(), "$level:" + invalid.getValue(), "$max:" + enchant.getMaxLevel());
    }

    public boolean hasEnchantPermissions(Player player, ItemStack item, Entry<Enchantment, Integer> ent) {
        String itemName = item.getType().toString();
        String name = ent.getKey().getName();
        return player.hasPermission(getPermission() + "." + itemName + "." + name) || player.hasPermission(getPermission() + "." + name);
    }

    public class EnchantmentsOptions extends Options {
        public int max_enchantments = 3;

        public EnchantmentsOptions(Executor executor) {
            super(executor);
        }
    }
}
