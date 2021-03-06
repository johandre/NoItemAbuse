/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.check.Check;
import noitemabuse.config.Message;

public class Ban extends Action {
    public Ban(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean defaultEnabled() {
        return false;
    }

    @Override
    public void perform(Player player, ItemStack item, Event event, String message, List<Check> failedChecks) {
        plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), Message.format(player, Message.BAN_COMMAND, "$item:" + manager.getItemName(item)));
    }
}
