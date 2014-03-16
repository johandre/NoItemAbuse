/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.check;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;

public class PotionEffects extends Check {
    public PotionEffects(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean check(Player player, ItemStack item) {
        return config.values.remove_invalid_potions && manager.checkPotionAndEffects(player, item) != null;
    }
}
