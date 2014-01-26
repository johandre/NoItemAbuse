/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import eu.icecraft_mc.frozenlib_R1.Plugin;

import noitemabuse.config.*;

public class Remove extends Action {
    public Remove(Plugin plugin) {
        super(plugin);
    }

    public Message getMessage() {
        return Message.REMOVE;
    }

    public String getName() {
        return "remove";
    }

    public void perform(Player player, ItemStack item, Event event, String message) {
        player.getInventory().remove(item);
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] == item) {
                armor[i] = null;
            }
        }
        player.getInventory().setArmorContents(armor);
    }
}
