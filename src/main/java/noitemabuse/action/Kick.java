/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.config.Message;

public class Kick extends Action {
    public Kick(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void perform(Player player, ItemStack item, Event event, String message) {
        player.kickPlayer(Message.format(player, Message.KICK_MESSAGE, "$item:" + manager.getItemName(item)));
    }
}
