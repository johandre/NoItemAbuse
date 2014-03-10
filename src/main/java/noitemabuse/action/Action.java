/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.CheckManager;
import noitemabuse.config.*;

public abstract class Action {
    protected Plugin plugin;
    protected CheckManager manager;
    protected ConfigManager config;

    public Action(Plugin plugin) {
        this.plugin = plugin;
        config = plugin.getManager(ConfigManager.class);
        manager = plugin.getManager(CheckManager.class);
    }

    public abstract Message getMessage();

    public abstract String getName();

    public abstract void perform(Player player, ItemStack item, Event event, String message);
}
