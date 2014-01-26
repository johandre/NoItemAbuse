/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import eu.icecraft_mc.frozenlib_R1.Plugin;

import noitemabuse.config.*;

public abstract class Action {
    protected Plugin plugin;
    public Action(Plugin plugin) {
        this.plugin = plugin;
    }
    public abstract void perform(Player player, ItemStack item, Event event, String message);
    public abstract String getName();
    public abstract Message getMessage();
}
