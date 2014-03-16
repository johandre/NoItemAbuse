/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;

public class Remove extends Action {
    public Remove(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void perform(Player player, ItemStack item, Event event, String message) {
        player.getInventory().remove(item);
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            if (item.equals(armor[i])) {
                armor[i] = null;
            }
        }
        player.getInventory().setArmorContents(armor);
    }
}
