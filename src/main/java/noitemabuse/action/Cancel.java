/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.inventory.ItemStack;

import noitemabuse.config.Message;

import eu.icecraft_mc.frozenlib_R1.Plugin;

public class Cancel extends Action {
    public Cancel(Plugin plugin) {
        super(plugin);
    }

    @Override
    public Message getMessage() {
        return Message.CANCEL;
    }

    @Override
    public String getName() {
        return "cancel";
    }

    @Override
    public void perform(Player player, ItemStack item, Event event, String message) {
        if (event instanceof Cancellable) {
            ((Cancellable) event).setCancelled(true);
        }
    }
}
