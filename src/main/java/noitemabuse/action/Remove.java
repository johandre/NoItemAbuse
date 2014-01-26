/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import noitemabuse.config.Message;

import eu.icecraft_mc.frozenlib_R1.Plugin;

public class Remove extends Action {
    public Remove(Plugin plugin) {
        super(plugin);
    }

    @Override
    public Message getMessage() {
        return Message.REMOVE;
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
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
