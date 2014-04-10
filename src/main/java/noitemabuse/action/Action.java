/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.check.Check;
import noitemabuse.config.Message;
import noitemabuse.util.Executor;

public abstract class Action extends Executor {
    public Action(Plugin plugin) {
        super(plugin);
    }

    public Message getMessage() {
        return Message.valueOf(getName().toUpperCase());
    }

    public abstract void perform(Player player, ItemStack item, Event event, String message, List<Check> failedChecks);
}
