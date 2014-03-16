/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;

public class Cancel extends Action {
    public Cancel(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void perform(Player player, ItemStack item, Event event, String message) {
        if (event instanceof Cancellable) {
            ((Cancellable) event).setCancelled(true);
        }
    }
}
