/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.config.Message;

public class Ban extends Action {
    public Ban(Plugin plugin) {
        super(plugin);
    }

    @Override
    public Message getMessage() {
        return Message.BAN;
    }

    @Override
    public String getName() {
        return "ban";
    }

    @Override
    public void perform(Player player, ItemStack item, Event event, String message) {
        plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), Message.format(player, Message.BAN_COMMAND, "$item:" + manager.getItemName(item)));
    }
}
