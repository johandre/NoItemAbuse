/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.check;

import java.util.Map.Entry;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;

public class Enchantments extends Check {
    public Enchantments(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean check(Player player, ItemStack item) {
        for (Entry<Enchantment, Integer> ent : item.getEnchantments().entrySet()) {
            if (ent.getValue() > ent.getKey().getMaxLevel()) return true;
        }
        return false;
    }
}
